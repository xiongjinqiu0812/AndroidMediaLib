package com.jonxiong.opengllib.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import java.io.*
import java.util.*

/**
 * Created by w00501804 on 2018/2/28.
 */
object FileUtil {
    private val TAG = FileUtil::class.java.simpleName

    /**
     * 从InputStream读取字符串
     *
     * @param inputStream
     * @return
     */
    fun readStringFromInputStream(inputStream: InputStream): String {
        val reader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(reader)
        val builder = StringBuilder("")
        var str: String?
        try {
            while (bufferedReader.readLine().also { str = it } != null) {
                builder.append(str)
                builder.append("\n")
            }
        } catch (e: IOException) {
            GLLog.e(TAG, "readStringFromInputStream exception : ", e)
            e.printStackTrace()
        } finally {
            try {
                bufferedReader.close()
                reader.close()
                inputStream.close()
            } catch (ex: Exception) {
                GLLog.e(TAG, "input stream close exception : ", ex)
            }
        }
        return builder.toString()
    }

    fun readStringFromAssets(context: Context, fileName: String?): String? {
        return try {
            readStringFromInputStream(context.assets.open(fileName!!))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun readStringFromFilePath(filePath: String?): String? {
        if (filePath == null) return null
        val file = File(filePath)
        return if (file.exists()) {
            try {
                val inputStream: InputStream = FileInputStream(file)
                readStringFromInputStream(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                GLLog.e(TAG, "readStringFromFilePath exception : ", e)
                null
            }
        } else {
            GLLog.i(TAG, "%s file is not found", filePath)
            null
        }
    }

    /**
     * 保存Bitmap到sdcard
     *
     * @param bitmap
     * @param filePath
     */
    fun saveBitmap(bitmap: Bitmap, filePath: String?) {
        if (filePath == null) {
            GLLog.e(TAG, "saveBitmap exception with empty filePath")
            return
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(filePath)
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            GLLog.e(TAG, "saveBitmap exception : ", e)
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 保存InputStream到sdcard
     *
     * @param inputStream
     * @param filePath
     */
    fun saveInputStream(inputStream: InputStream?, filePath: String?) {
        if (filePath == null) {
            GLLog.e(TAG, "saveInputStream exception with empty filePath")
            return
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(filePath)
            fileOutputStream = FileOutputStream(file)
            val b = ByteArray(1024)
            while (inputStream!!.read(b) != -1) {
                fileOutputStream.write(b) // 写入数据
            }
            fileOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            GLLog.e(TAG, "saveInputStream exception : ", e)
        } finally {
            try {
                fileOutputStream?.close()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveString(string: String, filePath: String?) {
        if (filePath == null) {
            GLLog.e(TAG, "saveString exception with empty filePath")
            return
        }
        val file = File(filePath)
        val parentFile = file.parentFile
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs()
        }
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(string.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            GLLog.e(TAG, "save string to file fail : ", e)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 文件复制，当目的文件不存在时会创建
     *
     * @param context 当srcUri为file时，context可为空
     * @param srcUri
     * @param dstFile
     */
    fun copyFile(context: Context, srcUri: Uri, dstFile: Any) {
        var dst: File? = null
        if (dstFile is File) {
            dst = dstFile
        } else if (dstFile is String) {
            dst = File(dstFile.toString())
        }
        if (dst == null) {
            return
        }
        val dstParent = dst.parentFile
        if (dstParent != null && !dstParent.exists()) {
            dstParent.mkdirs()
        }
        if (dst.exists()) {
            dst.delete()
        }
        var fos: FileOutputStream? = null
        var inputStream: InputStream? = null
        try {
            val scheme = srcUri.scheme
            var path = srcUri.authority
            if (!TextUtils.isEmpty(srcUri.path)) {
                path += srcUri.path
            }
            GLLog.i(TAG, "file util copy file path : %s", path)
            if ("file".equals(scheme, ignoreCase = true)) {
                inputStream = FileInputStream(File(path))
            } else if ("assets".equals(scheme, ignoreCase = true)) {
                inputStream = context.assets.open(path!!)
            }
            if (inputStream != null) {
                fos = FileOutputStream(dst)
                val buffer = ByteArray(1024)
                var count: Int
                while (inputStream.read(buffer).also { count = it } > 0) {
                    fos.write(buffer, 0, count)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GLLog.e(TAG, "copy file exception : ", e)
        } finally {
            try {
                fos?.close()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 检查是否有指定后缀名的文件存在
     *
     * @param path
     * @param suffix
     * @return
     */
    fun hasSuffixFile(path: String?, suffix: String?): Boolean {
        val file = File(path)
        val suffixPath = file.list { dir, name -> name.endsWith(suffix!!) }
        return suffixPath != null && suffixPath.size > 0
    }

    /**
     * 获取目录下的所有文件名
     *
     * @param path
     * @return
     */
    fun getFileList(path: String?): Array<String>? {
        val file = File(path)
        return if (file.exists()) {
            val modelNames = file.list()
            if (modelNames == null || modelNames.size == 0) {
                null
            } else modelNames
        } else {
            null
        }
    }

    /**
     * 获取路径中的文件名
     *
     * @param path
     * @return
     */
    fun getFileNameWithSuffix(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index == -1) path else path.substring(index + 1)
    }

    fun getFileNameWithoutSuffix(path: String): String {
        val index = path.lastIndexOf("/")
        val dotIndex = path.lastIndexOf(".")
        return if (dotIndex > index) {
            path.substring(index + 1, dotIndex)
        } else path.substring(index + 1)
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName
     * @return
     */
    fun getSuffix(fileName: String): String {
        val dotIndex = fileName.lastIndexOf(".")
        return if (dotIndex > 0) {
            fileName.substring(dotIndex + 1)
        } else {
            ""
        }
    }

    /**
     * 获取指定前缀名的首个文件名
     *
     * @param path
     * @param prefix
     * @return
     */
    fun getPrefixFileName(path: String?, prefix: String?): String? {
        val file = File(path)
        if (file.exists()) {
            val fileNames = file.list()
            if (fileNames == null || fileNames.size == 0) {
                return null
            }
            for (fileName in fileNames) {
                if (fileName.startsWith(prefix!!)) {
                    return fileName
                }
            }
        }
        return null
    }

    /**
     * 获取指定前缀名的文件名列表
     *
     * @param path
     * @param prefix
     * @return
     */
    fun getPrefixFileNameList(path: String?, prefix: String?): List<String>? {
        val file = File(path)
        if (file.exists()) {
            val names: MutableList<String> = ArrayList()
            val fileNames = file.list()
            if (fileNames == null || fileNames.size == 0) {
                return null
            }
            for (fileName in fileNames) {
                if (fileName.startsWith(prefix!!)) {
                    names.add(fileName)
                }
            }
            return names
        }
        return null
    }

    fun deleteFile(absolutePath: String?) {
        val file = File(absolutePath)
        if (file.exists()) {
            file.delete()
        }
    }
}