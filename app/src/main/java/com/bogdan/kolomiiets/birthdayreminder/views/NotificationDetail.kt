package com.bogdan.kolomiiets.birthdayreminder.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.utils.calculateAge
import com.bogdan.kolomiiets.birthdayreminder.utils.convertIntTypeToString

class NotificationDetail: AppCompatActivity(), View.OnClickListener {
    private lateinit var personName: TextView
    private lateinit var typeOfCelebration: TextView
    private lateinit var age: TextView
    private lateinit var callBtn: Button
    private lateinit var smsBtn: Button
    private lateinit var closeBtn: Button
    private var event: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        //init components
        personName = findViewById(R.id.person_name)
        typeOfCelebration = findViewById(R.id.type_of_celebration)
        age = findViewById(R.id.age)
        callBtn = findViewById(R.id.call_btn)
        smsBtn = findViewById(R.id.sms_btn)
        closeBtn = findViewById(R.id.close_btn)

        //setup onClickListener for components
        callBtn.setOnClickListener(this)
        smsBtn.setOnClickListener(this)
        closeBtn.setOnClickListener(this)

        event = intent.getParcelableExtra(EVENT)

        event?.let {
            personName.text = it.name
            typeOfCelebration.convertIntTypeToString(it.type)
            age.calculateAge(it.celebration_year, it.celebration_month, it.celebration_day)
        }

        //if event doesn't have phone
        smsBtn.isVisible = event?.phone?.isNotEmpty() ?: false
        callBtn.isVisible = event?.phone?.isNotEmpty() ?: false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.call_btn -> {
                val intentCall = Intent(Intent.ACTION_DIAL)
                intentCall.data = Uri.parse("tel:" + event?.phone)
                startActivity(intentCall)
            }
            R.id.sms_btn -> {
                val intentSMS = Intent(Intent.ACTION_SENDTO)
                intentSMS.data = Uri.parse("sms:" + event?.phone)
                startActivity(intentSMS)
            }
            R.id.close_btn -> finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}