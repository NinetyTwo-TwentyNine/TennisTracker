package com.example.tennistracker.data

import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_RADIAN
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_SPEED
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_STRENGTH

data class TennisHit(private val strength: Float, private val speed: Float, private val radian: Float) {
    init {
        if (strength > APP_TENNIS_MAX_STRENGTH || strength < 0) {
            throw(RuntimeException("Illegal Strength value ($strength) in a data class."))
        }
        if (speed > APP_TENNIS_MAX_SPEED || speed < 0) {
            throw(RuntimeException("Illegal Speed value ($speed) in a data class."))
        }
        if (radian > APP_TENNIS_MAX_RADIAN || radian < 0) {
            throw(RuntimeException("Illegal Radian value ($radian) in a data class."))
        }
    }

    fun getStrength(): Float {
        return strength
    }
    fun getSpeed(): Float {
        return speed
    }
    fun getRadian(): Float {
        return radian
    }
}
