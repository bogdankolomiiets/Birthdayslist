package com.bogdan.kolomiiets.birthdayreminder.utils

import android.text.format.DateUtils
import android.widget.TextView
import com.bogdan.kolomiiets.birthdayreminder.EventReminderApp
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_ANNIVERSARY
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_BIRTHDAY
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_HOLIDAY
import java.util.Calendar.*

private const val dateInstanceWithYear = DateUtils.FORMAT_NUMERIC_DATE
private const val dateInstanceWithoutYear = DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_NO_YEAR)
private val calendar = getInstance()
private val calendarNow = getInstance()

fun TextView.convertIntTypeToString(value: Int) {
    setText(when (value) {
        TYPE_BIRTHDAY -> R.string.birthday
        TYPE_ANNIVERSARY -> R.string.anniversary
        TYPE_HOLIDAY -> R.string.holiday
        else -> R.string.undefined
    })
}

fun TextView.convertEventDateToString(eventYear: Int, eventMonth: Int, eventDay: Int, withoutYear: Boolean) {
    calendar.set(eventYear, eventMonth.dec(), eventDay)
    text = DateUtils.formatDateTime(EventReminderApp.context, calendar.timeInMillis, if (withoutYear) dateInstanceWithoutYear else dateInstanceWithYear)
}

fun TextView.calculateAge(eventYear: Int, eventMonth: Int, eventDate: Int) {
    var yearsOld = 0

    if (eventYear != 0) {
        if (eventYear > 1) {
            yearsOld = calendarNow.get(YEAR) - eventYear
            if (calendarNow.get(MONTH).inc() < eventMonth || calendarNow.get(MONTH).inc() == eventMonth && calendarNow.get(DAY_OF_MONTH) < eventDate) {
                yearsOld.dec()
            }
        }
    }
    text = yearsOld.toString()
}