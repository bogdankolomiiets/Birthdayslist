package com.bogdan.kolomiiets.birthdayreminder.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.widget.Toast
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.SWITCH
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.PENDING_INTENT_REQUEST_CODE
import com.bogdan.kolomiiets.birthdayreminder.utils.WhoCelebrateService
import java.util.*

class CelebrationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            //reading ic_settings
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            val isAlarmOn: Boolean = preferences.getBoolean(SWITCH, true)
            val alarmHour: Int = preferences.getInt(REMINDER_HOUR, 9)
            val alarmMinute: Int = preferences.getInt(REMINDER_MINUTE, 0)

            //setup or update alarm
            try {
                val mIntent = Intent(context, WhoCelebrateService::class.java)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pendingIntent = PendingIntent.getService(context, PENDING_INTENT_REQUEST_CODE, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                if (!isAlarmOn) {
                    alarmManager.cancel(pendingIntent)
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar[Calendar.HOUR] = alarmHour
                    calendar[Calendar.MINUTE] = alarmMinute
                    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}