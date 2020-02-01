package com.bogdan.kolomiiets.birthdayreminder.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
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
    private lateinit var callBtn: ImageButton
    private lateinit var smsBtn: ImageButton
    private lateinit var closeBtn: ImageButton
    private lateinit var singleCloseBtn: Button
    private lateinit var buttonsContainer: LinearLayout
    private var event: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        //init components
        personName = findViewById(R.id.person_name)
        typeOfCelebration = findViewById(R.id.type_of_celebration)
        age = findViewById(R.id.age)
        buttonsContainer = findViewById(R.id.buttons_container)
        callBtn = findViewById(R.id.call_btn)
        smsBtn = findViewById(R.id.sms_btn)
        closeBtn = findViewById(R.id.close_btn)
        singleCloseBtn = findViewById(R.id.single_close_btn)

        //setup onClickListener for components
        callBtn.setOnClickListener(this)
        smsBtn.setOnClickListener(this)
        closeBtn.setOnClickListener(this)
        singleCloseBtn.setOnClickListener(this)

        event = intent.getParcelableExtra(EVENT)

        event?.let {
            personName.text = it.name
            typeOfCelebration.convertIntTypeToString(it.type)
            age.calculateAge(it.celebration_year, it.celebration_month, it.celebration_day)

            //if event doesn't have phone we show only single Close button
            buttonsContainer.isVisible = it.phone.isNotEmpty()
            singleCloseBtn.isVisible = it.phone.isEmpty()
        }
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
            R.id.close_btn,
            R.id.single_close_btn -> finishActivityAndRemoveTask()
        }
    }

    //home button
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        finishActivityAndRemoveTask()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishActivityAndRemoveTask()
    }

    private fun finishActivityAndRemoveTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else finish()
    }
}