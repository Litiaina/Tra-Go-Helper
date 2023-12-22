package com.teamlitiaina.tragohelper.utility

class NumberUtils {
    companion object {
        fun generateRandom10DigitNumber(): Long {
            val random = kotlin.random.Random
            return (1000000000L until 10000000000L).random(random)
        }
    }
}