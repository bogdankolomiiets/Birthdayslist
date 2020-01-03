package com.bogdan.kolomiiets.birthdayreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;

//public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
//    private Switch switchReminder;
//    private Button btnReminderOK;
//    private RadioGroup radioGroupDay;
//    private RadioButton radioSameDay, radioTheDayBefore, radioTwoDaysBefore;
//    private TimePicker reminderTimePicker;
//    private SharedPreferences preferences;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings);
//
//        switchReminder = (Switch) findViewById(R.id.reminder_on_off);
//        btnReminderOK = (Button) findViewById(R.id.ok_btn);
//        btnReminderOK.setOnClickListener(this);
//        radioGroupDay = (RadioGroup) findViewById(R.id.radio_group_container);
//        radioSameDay = (RadioButton) findViewById(R.id.radio_same_day);
//        radioTheDayBefore = (RadioButton) findViewById(R.id.radio_the_day_before);
//        radioTwoDaysBefore = (RadioButton) findViewById(R.id.radio_two_days_before);
//        reminderTimePicker = (TimePicker) findViewById(R.id.reminderTimePicker);
//        reminderTimePicker.setIs24HourView(DateFormat.is24HourFormat(this));
//
//        switchReminder.setOnClickListener(this);
//        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//
//        boolean enableSwitch = preferences.getBoolean(SWITCH, true);
//        if (enableSwitch){
//            switchReminder.setChecked(true);
//            radioTrue();
//            reminderTimePicker.setEnabled(true);
//        } else {
//            switchReminder.setChecked(false);
//            radioFalse();
//            reminderTimePicker.setEnabled(false);
//        }
//
//        //Set active radio button
//        int i = preferences.getInt(CHECKED_RADIO, 0);
//        switch (i){
//            case 0: radioSameDay.setChecked(true);
//            break;
//            case 1: radioTheDayBefore.setChecked(true);
//                break;
//            case 2: radioTwoDaysBefore.setChecked(true);
//                break;
//        }
//
//        //Set text from preferences to etReminderTime
//        reminderTimePicker.setCurrentHour(preferences.getInt(REMINDER_HOUR, 9));
//        reminderTimePicker.setCurrentMinute(preferences.getInt(REMINDER_MINUTE, 0));
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.reminder_on_off:
//                if (switchReminder.isChecked()){
//                    radioTrue();
//                    reminderTimePicker.setEnabled(true);
//                } else {
//                    radioFalse();
//                    reminderTimePicker.setEnabled(false);
//                }
//                break;
//            case R.id.ok_btn:
//                finish();
//        }
//    }
//
//    private void radioTrue(){
//        radioSameDay.setEnabled(true);
//        radioTheDayBefore.setEnabled(true);
//        radioTwoDaysBefore.setEnabled(true);
//    }
//
//    private void radioFalse(){
//        radioSameDay.setEnabled(false);
//        radioTheDayBefore.setEnabled(false);
//        radioTwoDaysBefore.setEnabled(false);
//    }
//
//    @Override
//    protected void onStop() {
//        //Save ic_settings to SharedPreferences
//        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        boolean checkedSwitch = switchReminder.isChecked() ? true : false;
//        editor.putBoolean(SWITCH, checkedSwitch);
//        switch (radioGroupDay.getCheckedRadioButtonId()) {
//            case R.id.radio_same_day:
//                editor.putInt(CHECKED_RADIO, 0);
//                break;
//            case R.id.radio_the_day_before:
//                editor.putInt(CHECKED_RADIO, 1);
//                break;
//            case R.id.radio_two_days_before:
//                editor.putInt(CHECKED_RADIO, 2);
//                break;
//        }
//        int hour = reminderTimePicker.getCurrentHour();
//        int minute = reminderTimePicker.getCurrentMinute();
//        editor.putInt(REMINDER_HOUR, hour);
//        editor.putInt(REMINDER_MINUTE, minute);
//        editor.commit();
//
//        //Set alarm
//            SetAlarm setAlarm = new SetAlarm();
//            setAlarm.onAlarm(this, checkedSwitch, hour, minute);
//
//        super.onStop();
//    }
//}
