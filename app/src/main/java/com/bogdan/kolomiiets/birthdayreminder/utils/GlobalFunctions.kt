package com.bogdan.kolomiiets.birthdayreminder.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.bogdan.kolomiiets.birthdayreminder.EventReminderApp
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.PENDING_INTENT_REQUEST_CODE
import java.text.SimpleDateFormat
import java.util.*

private val calendar = Calendar.getInstance()

fun isExternalStorageReadOnly(): Boolean {
    return Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()
}

fun isExternalStorageAvailable(): Boolean {
    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
}

fun getStringDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.getDefault())
    return sdf.format(calendar.time)
}

fun showToast(message: String) {
    Toast.makeText(EventReminderApp.context, message, Toast.LENGTH_SHORT).show()
}

fun showToast(resId: Int) {
    Toast.makeText(EventReminderApp.context, resId, Toast.LENGTH_SHORT).show()
}

fun setEveryDayAlarm(context: Context, alarmHour: Int, alarmMinute: Int, isAlarmOn: Boolean) {
    try {
        val intent = Intent(context, WhoCelebrateService::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        if (!isAlarmOn) {
            alarmManager.cancel(pendingIntent)
        } else {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, alarmHour)
            calendar.set(Calendar.MINUTE, alarmMinute)

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    } catch (e: Exception) {
        showToast(e.message ?: e.toString())
    }
}