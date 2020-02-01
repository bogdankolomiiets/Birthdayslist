package com.bogdan.kolomiiets.birthdayreminder.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.CHECKED_RADIO
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.FOREGROUND_SERVICE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_EVENTS_NOTIFIER
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_HOLIDAY
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider
import com.bogdan.kolomiiets.birthdayreminder.views.NotificationDetail
import java.util.*

class WhoCelebrateService : IntentService(Context.ALARM_SERVICE) {
    private val month = "month"
    private val dayOfMonth = "dayOfMonth"

    override fun onCreate() {
        super.onCreate()
        val foregroundNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.event_search_in_progress))
                .setSmallIcon(R.drawable.ic_info_outline)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_event_reminder_for_notification))
        startForeground(FOREGROUND_SERVICE, foregroundNotification.build())
    }

    override fun onHandleIntent(p0: Intent?) {

        val calendar = Calendar.getInstance()

        val eventsNotifierPreferences = getSharedPreferences(PREFERENCES_EVENTS_NOTIFIER, Context.MODE_PRIVATE)

        if ((eventsNotifierPreferences.getInt(month, -1) != calendar.get(Calendar.MONTH))
                        .and(eventsNotifierPreferences.getInt(dayOfMonth, -1) != calendar.get(Calendar.DAY_OF_MONTH))) {

            var whenCelebrate = ""
            when (applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(CHECKED_RADIO, 0)) {
                0 -> whenCelebrate = applicationContext.getString(R.string.today_celebrate)
                1 -> {
                    whenCelebrate = applicationContext.getString(R.string.tomorrow_will_celebrate)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                2 -> {
                    whenCelebrate = applicationContext.getString(R.string.after_tomorrow_will_celebrate)
                    calendar.add(Calendar.DAY_OF_MONTH, 2)
                }
            }

            val events = StorageProvider().getEventsOnDate(calendar.get(Calendar.MONTH).inc(), calendar.get(Calendar.DAY_OF_MONTH))
            events.forEach {
                val eventDetailIntent = Intent(applicationContext, NotificationDetail::class.java)
                eventDetailIntent.putExtra(EVENT, it)

                val pendingIntent = PendingIntent.getActivity(applicationContext, it._id, eventDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                builder.setSmallIcon(R.drawable.ic_info_outline)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_event_reminder_for_notification))
                        .setTicker(whenCelebrate)
                        .setContentTitle(whenCelebrate)
                        .setContentText(it.name)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)

                if (it.type != TYPE_HOLIDAY) {
                    builder.setContentIntent(pendingIntent)
                }

                val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                    builder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    manager.createNotificationChannel(channel)
                }
                manager.notify(it._id, builder.build())
            }

            //If user was notified about events on today
            //then we save current month and day of month to shared preferences
            //In case user will reboot phone he won't notified second time
            val tempCalendar = Calendar.getInstance()
            eventsNotifierPreferences.edit()
                    .putInt(month, tempCalendar.get(Calendar.MONTH))
                    .putInt(dayOfMonth, tempCalendar.get(Calendar.DAY_OF_MONTH))
                    .apply()

            stopForeground(true)
        }
    }
}