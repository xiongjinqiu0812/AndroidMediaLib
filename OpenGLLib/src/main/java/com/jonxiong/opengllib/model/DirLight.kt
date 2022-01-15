package com.jonxiong.opengllib.model

import com.jonxiong.opengllib.gl_extra.mGL

class DirLight {
    var directionLoc = 0
    var ambientLoc = 0
    var diffuseLoc = 0
    var specularLoc = 0

    var directionValue = FloatArray(3)
    var ambientValue = FloatArray(3)
    var diffuseValue = FloatArray(3)
    var specularValue = FloatArray(3)

    fun initLoc(program: Int) {
        directionLoc = mGL.glGetUniformLocation(program, "dirLight.direction")
        ambientLoc = mGL.glGetUniformLocation(program, "dirLight.ambient")
        diffuseLoc = mGL.glGetUniformLocation(program, "dirLight.diffuse")
        specularLoc = mGL.glGetUniformLocation(program, "dirLight.specular")
    }

    fun useLoc() {
        mGL.glUniform3f(directionLoc, directionValue[0], directionValue[1], directionValue[2])
        mGL.glUniform3f(ambientLoc, ambientValue[0], ambientValue[1], ambientValue[2])
        mGL.glUniform3f(diffuseLoc, diffuseValue[0], diffuseValue[1], diffuseValue[2])
        mGL.glUniform3f(specularLoc, specularValue[0], specularValue[1], specularValue[2])
    }
}
