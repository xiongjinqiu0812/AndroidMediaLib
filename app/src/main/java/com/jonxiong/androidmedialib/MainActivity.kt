package com.jonxiong.androidmedialib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.jonxiong.player.VideoPlayActivity
import com.jonxiong.player.record.CameraActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.camera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<Button>(R.id.mediaExtractor).setOnClickListener {
            startActivity(Intent(this, VideoPlayActivity::class.java))
        }
    }
}