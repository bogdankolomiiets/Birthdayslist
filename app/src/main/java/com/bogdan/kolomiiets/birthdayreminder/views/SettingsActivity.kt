package com.bogdan.kolomiiets.birthdayreminder.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.bogdan.kolomiiets.birthdayreminder.Constants
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.CHECKED_RADIO
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_TYPE_ANNIVERSARY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_TYPE_BIRTHDAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_TYPE_HOLIDAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_ON_OFF
import com.bogdan.kolomiiets.birthdayreminder.R
import java.util.*

class SettingsActivity: AppCompatActivity() {
    private lateinit var reminderOnOff: Switch
    private lateinit var radioGroupContainer: RadioGroup
    private lateinit var reminderTimePicker: TimePicker
    private lateinit var preferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //init UI components
        reminderOnOff = findViewById(R.id.reminder_on_off)
        radioGroupContainer = findViewById(R.id.radio_group_container)
        reminderTimePicker = findViewById(R.id.reminder_time_picker)

        //set onClickListeners for UI components
        reminderOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            (0 until radioGroupContainer.childCount).forEach { i ->
                radioGroupContainer[i].isEnabled = isChecked
            }
            reminderTimePicker.isEnabled = isChecked
        }

        preferences = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun onPause() {
        //Save settings into SharedPreferences
        val editor = preferences.edit()
        editor.putBoolean(REMINDER_ON_OFF, reminderOnOff.isChecked)
        editor.putInt(CHECKED_RADIO, when (radioGroupContainer.checkedRadioButtonId) {
            R.id.radio_same_day -> EVENT_TYPE_BIRTHDAY
            R.id.radio_the_day_before -> EVENT_TYPE_ANNIVERSARY
            R.id.radio_two_days_before -> EVENT_TYPE_HOLIDAY
            else -> 0
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editor.putInt(REMINDER_HOUR, reminderTimePicker.hour)
            editor.putInt(REMINDER_MINUTE, reminderTimePicker.minute)
        } else {
            editor.putInt(REMINDER_HOUR, reminderTimePicker.currentHour)
            editor.putInt(REMINDER_MINUTE, reminderTimePicker.currentMinute)
        }

        editor.apply()

        //Set alarm
//            SetAlarm setAlarm = new SetAlarm();
//            setAlarm.onAlarm(this, checkedSwitch, hour, minute)
//
        super.onPause()
    }
}