package com.teamlitiaina.tragohelper.datetime

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateTime {
    companion object {
        fun getCurrentTime(): Int {
            val calendar = Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY)
        }
        fun getCurrentDateTime(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = Calendar.getInstance().time
            return dateFormat.format(currentDateTime)
        }
    }
}