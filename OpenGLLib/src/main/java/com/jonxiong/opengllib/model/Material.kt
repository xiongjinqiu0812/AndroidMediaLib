package com.jonxiong.opengllib.model

import com.jonxiong.opengllib.gl_extra.mGL

class Material {
    var diffuseLoc: Int = 0
    var specularLoc: Int = 0
    var shininessLoc: Int = 0

    var diffuseValue = 0
    var specularValue = 0
    var shininessValue = 0f

    fun initLoc(program: Int) {
        diffuseLoc = mGL.glGetUniformLocation(program, "material.diffuse")
        specularLoc = mGL.glGetUniformLocation(program, "material.specular")
        shininessLoc = mGL.glGetUniformLocation(program, "material.shininess")
    }

    fun useValue() {
        mGL.glUniform1i(diffuseLoc, diffuseValue)
        mGL.glUniform1i(specularLoc, specularValue)
        mGL.glUniform1f(shininessLoc, shininessValue)
    }
}