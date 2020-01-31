package com.bogdan.kolomiiets.birthdayreminder

import android.content.Context
import androidx.multidex.MultiDexApplication

class EventReminderApp: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
    }

    companion object {
        lateinit var context: Context
    }
}