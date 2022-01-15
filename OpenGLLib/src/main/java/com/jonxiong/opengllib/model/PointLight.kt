package com.jonxiong.opengllib.model

import com.jonxiong.opengllib.gl_extra.mGL

class PointLight {
    var positionLoc = 0
    var constantLoc = 0
    var linearLoc = 0
    var quadraticLoc = 0
    var ambientLoc = 0
    var diffuseLoc = 0
    var specularLoc = 0

    fun initLoc(program: Int, index: Int = 0) {
        positionLoc = mGL.glGetUniformLocation(program, "dirLight[$index].position")
        constantLoc = mGL.glGetUniformLocation(program, "dirLight[$index].constant")
        linearLoc = mGL.glGetUniformLocation(program, "dirLight[$index].linear")
        quadraticLoc = mGL.glGetUniformLocation(program, "dirLight[$index].quadratic")
        ambientLoc = mGL.glGetUniformLocation(program, "dirLight[$index].ambient")
        diffuseLoc = mGL.glGetUniformLocation(program, "dirLight[$index].diffuse")
        specularLoc = mGL.glGetUniformLocation(program, "dirLight[$index].specular")
    }

}