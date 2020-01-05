package com.bogdan.kolomiiets.birthdayreminder.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_ON_OFF
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.PENDING_INTENT_REQUEST_CODE
import com.bogdan.kolomiiets.birthdayreminder.utils.WhoCelebrateService
import java.util.*

class CelebrationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            //reading settings
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            val isAlarmOn: Boolean = preferences.getBoolean(REMINDER_ON_OFF, true)
            val alarmHour: Int = preferences.getInt(REMINDER_HOUR, BASE_REMINDER_HOUR)
            val alarmMinute: Int = preferences.getInt(REMINDER_MINUTE, BASE_REMINDER_MINUTE)

            //setup or update alarm
            try {
                val mIntent = Intent(context, WhoCelebrateService::class.java)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(context, PENDING_INTENT_REQUEST_CODE, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                } else {
                    PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                if (!isAlarmOn) {
                    alarmManager.cancel(pendingIntent)
                } else {
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.HOUR] = alarmHour
                    calendar[Calendar.MINUTE] = alarmMinute
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}