package com.bogdan.kolomiiets.birthdayreminder.views

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.bogdan.kolomiiets.birthdayreminder.Constants
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.CHECKED_RADIO
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_SAME_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_THE_DAY_BEFORE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_TWO_DAYS_BEFORE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_ON_OFF
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes
import com.bogdan.kolomiiets.birthdayreminder.utils.WhoCelebrateService
import java.lang.Exception
import java.util.*

class SettingsActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var reminderOnOff: Switch
    private lateinit var radioGroupContainer: RadioGroup
    private lateinit var reminderTimePicker: TimePicker
    private lateinit var preferences: SharedPreferences
    private lateinit var saveBtn: Button
    private val calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //init UI components
        reminderOnOff = findViewById(R.id.reminder_on_off)
        radioGroupContainer = findViewById(R.id.radio_group_container)
        reminderTimePicker = findViewById(R.id.reminder_time_picker)
        saveBtn = findViewById(R.id.save_btn)

        //set onClickListeners for UI components
        saveBtn.setOnClickListener(this)
        reminderOnOff.setOnCheckedChangeListener { _, isChecked -> enableUiComponents(isChecked) }

        preferences = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

        reminderOnOff.isChecked = preferences.getBoolean(REMINDER_ON_OFF, false)
        if (!reminderOnOff.isChecked) {
            enableUiComponents(false)
        }
        radioGroupContainer.check(
                when (preferences.getInt(CHECKED_RADIO, RADIO_SAME_DAY)) {
                    RADIO_THE_DAY_BEFORE -> R.id.radio_the_day_before
                    RADIO_TWO_DAYS_BEFORE -> R.id.radio_two_days_before
                    else -> R.id.radio_same_day
                })
        reminderTimePicker.setIs24HourView(DateFormat.is24HourFormat(this))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            reminderTimePicker.hour = preferences.getInt(REMINDER_HOUR, calendar.get(Calendar.HOUR))
            reminderTimePicker.minute = preferences.getInt(REMINDER_MINUTE, calendar.get(Calendar.MINUTE))
        } else {
            reminderTimePicker.currentHour = preferences.getInt(REMINDER_HOUR, calendar.get(Calendar.HOUR))
            reminderTimePicker.currentMinute = preferences.getInt(REMINDER_MINUTE, calendar.get(Calendar.MINUTE))
        }
    }

    override fun onClick(p0: View?) {
        try {
            //Save settings into SharedPreferences
            val editor = preferences.edit()
            editor.putBoolean(REMINDER_ON_OFF, reminderOnOff.isChecked)
            editor.putInt(CHECKED_RADIO, when (radioGroupContainer.checkedRadioButtonId) {
                R.id.radio_the_day_before -> RADIO_THE_DAY_BEFORE
                R.id.radio_two_days_before -> RADIO_TWO_DAYS_BEFORE
                else -> RADIO_SAME_DAY
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                editor.putInt(REMINDER_HOUR, reminderTimePicker.hour)
                editor.putInt(REMINDER_MINUTE, reminderTimePicker.minute)
            } else {
                editor.putInt(REMINDER_HOUR, reminderTimePicker.currentHour)
                editor.putInt(REMINDER_MINUTE, reminderTimePicker.currentMinute)
            }

            editor.apply()

            setAlarm(reminderOnOff.isChecked)
            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setAlarm(needToRemind: Boolean) {
        try {
            val intent = Intent(applicationContext, WhoCelebrateService::class.java)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(this, RequestCodes.PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getService(this, RequestCodes.PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            if (!needToRemind) {
                alarmManager.cancel(pendingIntent)
            } else {
                val calendar = Calendar.getInstance()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    calendar.set(Calendar.HOUR, reminderTimePicker.hour)
                    calendar.set(Calendar.MINUTE, reminderTimePicker.minute)
                } else {
                    calendar.set(Calendar.HOUR, reminderTimePicker.currentHour)
                    calendar.set(Calendar.MINUTE, reminderTimePicker.currentMinute)
                }
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun enableUiComponents(isChecked: Boolean){
        (0 until radioGroupContainer.childCount).forEach { i ->
            radioGroupContainer[i].isEnabled = isChecked
        }
        reminderTimePicker.isEnabled = isChecked
    }
}