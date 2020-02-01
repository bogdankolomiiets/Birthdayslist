package com.bogdan.kolomiiets.birthdayreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.IS_REMINDER_ON
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.utils.setEveryDayAlarm

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            //reading settings
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            val isAlarmOn: Boolean = preferences.getBoolean(IS_REMINDER_ON, true)
            val alarmHour: Int = preferences.getInt(REMINDER_HOUR, BASE_REMINDER_HOUR)
            val alarmMinute: Int = preferences.getInt(REMINDER_MINUTE, BASE_REMINDER_MINUTE)

            //setup or update alarm
            setEveryDayAlarm(context, alarmHour, alarmMinute, isAlarmOn)
        }
    }
}