package com.bogdan.kolomiiets.birthdayreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bogdan.kolomiiets.birthdayreminder.utils.WhoCelebrateService

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.startForegroundService(Intent(it, WhoCelebrateService::class.java))
            } else it.startService(Intent(it, WhoCelebrateService::class.java))
        }
    }
}