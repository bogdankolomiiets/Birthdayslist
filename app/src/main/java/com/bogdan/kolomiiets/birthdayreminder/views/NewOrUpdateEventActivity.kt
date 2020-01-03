package com.bogdan.kolomiiets.birthdayreminder.views

import android.Manifest.permission.READ_CONTACTS
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.PICK_CONTACT
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider


class NewOrUpdateEventActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var saveBtn: Button
    private lateinit var pickContactBtn: ImageButton
    private lateinit var radioButtonsContainer: RadioGroup
    private lateinit var personName: TextView
    private lateinit var phone: TextView
    private lateinit var datePicker: DatePicker
    private var event: Event? = null
    private var existsInDatabase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_or_update_event)

        //init UI components
        saveBtn = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener(this)
        pickContactBtn = findViewById(R.id.pick_contact_btn)
        pickContactBtn.setOnClickListener(this)

        personName = findViewById(R.id.person_name)
        phone = findViewById(R.id.phone)
        datePicker = findViewById(R.id.date_picker)
        radioButtonsContainer = findViewById(R.id.radio_buttons_container)

        event = intent.getParcelableExtra(EVENT)
        event?.let {
            existsInDatabase = true
            personName.text = it.name
            phone.text = it.phone
            when (it.type) {
                1 -> radioButtonsContainer.check(R.id.radio_birthday)
                2 -> radioButtonsContainer.check(R.id.radio_anniversary)
                3 -> radioButtonsContainer.check(R.id.radio_holiday)
            }
            datePicker.updateDate(it.year, it.month, it.day)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.save_btn -> saveEvent()
            R.id.pick_contact_btn -> pickContact()
        }
    }

    private fun pickContact() {
        if (PermissionChecker.checkSelfPermission(this, READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            startActivityForResult(intent, PICK_CONTACT)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), RequestCodes.REQUEST_CONTACTS_PERMISSION)
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
                if (event == null) {
                    event = Event()
                }
                event?.let {
                    it.name = personName.text.toString()
                    it.phone = phone.text.toString()
                    it.type = type
                    it.year = datePicker.year
                    it.month = datePicker.month
                    it.day = datePicker.dayOfMonth

                    if (existsInDatabase) StorageProvider().updateEvent(it) else StorageProvider().insertEvent(it)
                }
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_CONTACT -> {
                    val contactUri = data?.data
                    val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
                    contactUri?.let {
                        val cursor = contentResolver.query(it, projection, null, null, null)
                        cursor?.moveToFirst()
                        personName.text = cursor?.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        phone.text = cursor?.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        cursor?.close()
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RequestCodes.REQUEST_CONTACTS_PERMISSION -> {
                if (grantResults.isNotEmpty() and (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    pickContact()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}