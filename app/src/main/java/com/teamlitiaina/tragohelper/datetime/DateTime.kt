package com.teamlitiaina.tragohelper.datetime

import java.util.Calendar

class DateTime {
    companion object {
        fun getCurrentTime(): Int {
            val calendar = Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY)
        }
    }
}