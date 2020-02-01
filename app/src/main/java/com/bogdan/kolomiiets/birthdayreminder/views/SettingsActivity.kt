package com.bogdan.kolomiiets.birthdayreminder.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.bogdan.kolomiiets.birthdayreminder.Constants
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.CHECKED_RADIO
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.IS_REMINDER_ON
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_SAME_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_THE_DAY_BEFORE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_TWO_DAYS_BEFORE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.utils.setEveryDayAlarm
import com.bogdan.kolomiiets.birthdayreminder.utils.showToast
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

        reminderOnOff.isChecked = preferences.getBoolean(IS_REMINDER_ON, false)
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

    override fun onClick(view: View?) {
        try {
            //Save settings into SharedPreferences
            val editor = preferences.edit()
            editor.putBoolean(IS_REMINDER_ON, reminderOnOff.isChecked)

            editor.putInt(CHECKED_RADIO, when (radioGroupContainer.checkedRadioButtonId) {
                R.id.radio_the_day_before -> RADIO_THE_DAY_BEFORE
                R.id.radio_two_days_before -> RADIO_TWO_DAYS_BEFORE
                else -> RADIO_SAME_DAY
            })

            val tempHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                reminderTimePicker.hour else reminderTimePicker.currentHour
            editor.putInt(REMINDER_HOUR, tempHour)

            val tempMinute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                reminderTimePicker.minute else reminderTimePicker.currentMinute
            editor.putInt(REMINDER_MINUTE, tempMinute)

            editor.apply()

            setEveryDayAlarm(this, tempHour, tempMinute, reminderOnOff.isChecked)

            getSharedPreferences(Constants.PREFERENCES_EVENTS_NOTIFIER, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

            showToast(R.string.saved)
            finish()
        } catch (e: Exception) {
            showToast(e.message ?: e.toString())
        }
    }

    private fun enableUiComponents(isChecked: Boolean){
        (0 until radioGroupContainer.childCount).forEach { i ->
            radioGroupContainer[i].isEnabled = isChecked
        }
        reminderTimePicker.isEnabled = isChecked
    }
}