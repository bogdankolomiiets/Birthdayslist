package com.bogdan.kolomiiets.birthdayreminder.views

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider
import java.lang.Exception

class NewOrUpdateEventActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var saveBtn: Button
    private lateinit var radioButtonsContainer: RadioGroup
    private lateinit var personName: TextView
    private lateinit var phone: TextView
    private lateinit var datePicker: DatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_or_update_event)

        //init UI components
        saveBtn = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener(this)

        personName = findViewById(R.id.person_name)
        phone = findViewById(R.id.phone)
        datePicker = findViewById(R.id.date_picker)
        radioButtonsContainer = findViewById(R.id.radio_buttons_container)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.save_btn -> {
                saveEvent()
            }
        }
    }

    private fun saveEvent() {
        try {
            val type = when (radioButtonsContainer.checkedRadioButtonId) {
                R.id.radio_birthday -> 1
                R.id.radio_anniversary -> 2
                R.id.radio_holiday -> 3
                else -> 0
            }
            if (personName.text.isNotEmpty()) {
                val event = Event(name = personName.text.toString(), phone = phone.text.toString(), type = type, year = datePicker.year, month = datePicker.month, day = datePicker.dayOfMonth)
                StorageProvider().insertEvent(event)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }
}