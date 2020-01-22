package com.bogdan.kolomiiets.birthdayreminder.utils

import android.os.Environment
import android.widget.TextView
import com.bogdan.kolomiiets.birthdayreminder.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*


private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val calendar = getInstance()
private val calendarNow = getInstance()

fun TextView.convertIntTypeToString(value: Int) {
    setText(when (value) {
        1 -> R.string.birthday
        2 -> R.string.anniversary
        3 -> R.string.holiday
        else -> R.string.undefined
    })
}

fun TextView.convertEventDateToString(eventYear: Int, eventMonth: Int, eventDay: Int) {
    calendar.set(eventYear, eventMonth, eventDay)
    text = dateFormat.format(calendar.time)
}

fun TextView.calculateAge(eventYear: Int, eventMonth: Int, eventDate: Int) {
    var yearsOld = 0

    if (eventYear != 0) {
        if (eventYear > 1) {
            yearsOld = calendarNow.get(YEAR) - eventYear
            if (calendarNow.get(MONTH) + 1 < eventMonth || calendarNow.get(MONTH) + 1 == eventMonth && calendarNow.get(DAY_OF_MONTH) < eventDate) {
                yearsOld.dec()
            }
        }
    }
    text = yearsOld.toString()
}

fun isExternalStorageReadOnly(): Boolean {
    return Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()
}

fun isExternalStorageAvailable(): Boolean {
    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
}

fun getStringDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(calendarNow.time)
}