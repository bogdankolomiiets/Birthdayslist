package com.bogdan.kolomiiets.birthdayreminder

import android.app.Application
import android.content.Context

class BirthdayApp: Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
    }
}