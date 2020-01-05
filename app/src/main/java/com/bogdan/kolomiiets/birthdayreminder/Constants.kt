package com.bogdan.kolomiiets.birthdayreminder

class Constants {
    companion object {
        const val PREFERENCES_NAME = "Settings"
        const val IS_FIRST_RUN = "IS_FIRST_RUN"
        const val CHECKED_RADIO = "CHECKED_RADIO"
        const val BASE_REMINDER_HOUR = 9
        const val BASE_REMINDER_MINUTE = 0
        const val REMINDER_HOUR = "REMINDER_HOUR"
        const val REMINDER_MINUTE = "REMINDER_MINUTE"
        const val NOTIFICATION_CHANNEL_ID = "BirthdayChannel"
        const val NOTIFICATION_CHANNEL_NAME = "BirthdayChannelName"
        const val EVENT = "event"
        const val EVENT_NAME = "name"
        const val EVENT_YEAR = "celebration_year"
        const val EVENT_MONTH = "celebration_month"
        const val EVENT_DAY = "celebration_day"
        const val EVENT_ID = "_id"
        const val EVENT_TYPE_BIRTHDAY = 1
        const val EVENT_TYPE_ANNIVERSARY = 2
        const val EVENT_TYPE_HOLIDAY = 3
        const val REMINDER_ON_OFF = "REMINDER_ON_OFF"
        const val RADIO_SAME_DAY = 0
        const val RADIO_THE_DAY_BEFORE = 1
        const val RADIO_TWO_DAYS_BEFORE = 2
        const val FOREGROUND_SERVICE = 1000
    }
}