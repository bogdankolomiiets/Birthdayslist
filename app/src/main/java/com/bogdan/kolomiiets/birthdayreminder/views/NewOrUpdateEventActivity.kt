package com.bogdan.kolomiiets.birthdayreminder.views

import android.Manifest.permission.READ_CONTACTS
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.provider.ContactsContract
import android.transition.TransitionManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.PICK_CONTACT
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_ANNIVERSARY
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_BIRTHDAY
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_HOLIDAY
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider
import com.bogdan.kolomiiets.birthdayreminder.utils.showToast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import java.util.*


class NewOrUpdateEventActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var saveBtn: Button
    private lateinit var pickContactBtn: ImageButton
    private lateinit var radioButtonsContainer: RadioGroup
    private lateinit var radioButtonHoliday: RadioButton
    private lateinit var personName: TextView
    private lateinit var phone: TextView
    private lateinit var datePicker: DatePicker
    private lateinit var datePickerYearField: View
    private lateinit var constraintLayout: ConstraintLayout
    private var event: Event? = null
    private var existsInDatabase = false
    private val year = "year"
    private val month = "month"
    private val dayOfMonth = "dayOfMonth"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_or_update_event)

        //init interstitial ad
        MobileAds.initialize(this) {}
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.admob_test_interstitialAd_id)

        if (countOfVisits >= countOfVisitsToDisplayAds) {
            interstitialAd.loadAd(AdRequest.Builder().build())
            countOfVisits = 0
        } else {
            countOfVisits++
        }

        interstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                interstitialAd.show()
            }
        }

        //init UI components
        constraintLayout = findViewById(R.id.constraint_layout)
        saveBtn = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener(this)
        pickContactBtn = findViewById(R.id.pick_contact_btn)
        pickContactBtn.setOnClickListener(this)

        personName = findViewById(R.id.person_name)
        phone = findViewById(R.id.phone)
        datePicker = findViewById(R.id.date_picker)
        datePickerYearField = datePicker.findViewById<View>(Resources.getSystem().getIdentifier("year", "id", "android"))

        savedInstanceState?.let {
            val calendar = Calendar.getInstance()
            datePicker.updateDate(it.getInt(year, calendar.get(Calendar.YEAR)), it.getInt(month, calendar.get(Calendar.MONTH)), it.getInt(dayOfMonth, calendar.get(Calendar.DAY_OF_MONTH)))
        }

        radioButtonsContainer = findViewById(R.id.radio_buttons_container)
        radioButtonHoliday = findViewById(R.id.radio_holiday)
        radioButtonHoliday.setOnCheckedChangeListener { _, b ->
            //slow transformation
            TransitionManager.beginDelayedTransition(constraintLayout)
            datePickerYearField.isVisible = !b
            phone.isVisible = !b
        }

        event = intent.getParcelableExtra(EVENT)
        event?.let {
            existsInDatabase = true
            personName.text = it.name
            phone.text = it.phone

            when (it.type) {
                TYPE_BIRTHDAY -> radioButtonsContainer.check(R.id.radio_birthday)
                TYPE_ANNIVERSARY -> radioButtonsContainer.check(R.id.radio_anniversary)
                TYPE_HOLIDAY -> radioButtonsContainer.check(R.id.radio_holiday)
            }

            datePicker.updateDate(if (it.celebration_year <= 1900) 1900 else it.celebration_year,
                    it.celebration_month.dec(), it.celebration_day)
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
                R.id.radio_birthday -> TYPE_BIRTHDAY
                R.id.radio_anniversary -> TYPE_ANNIVERSARY
                R.id.radio_holiday -> TYPE_HOLIDAY
                else -> 0
            }
            if (personName.text.isNotEmpty()) {
                if (event == null) {
                    event = Event()
                }

                event?.let {
                    it.name = personName.text.toString()
                    it.phone = if (type == TYPE_HOLIDAY) "" else phone.text.toString()
                    it.type = type
                    it.celebration_year = datePicker.year
                    it.celebration_month = datePicker.month.inc()
                    it.celebration_day = datePicker.dayOfMonth

                    if (existsInDatabase) StorageProvider().updateEvent(it) else StorageProvider().insertEvent(it)
                }
                finish()
            } else personName.error = getString(R.string.enter_name)
        } catch (e: Exception) {
            showToast(e.message ?: e.toString())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(year, datePicker.year)
        outState.putInt(month, datePicker.month)
        outState.putInt(dayOfMonth, datePicker.dayOfMonth)

        //prevent negative value -> if (countOfVisits > 0)
        if (countOfVisits > 0) countOfVisits--
    }

    companion object {
        const val countOfVisitsToDisplayAds = 1
        var countOfVisits = 0
    }
}