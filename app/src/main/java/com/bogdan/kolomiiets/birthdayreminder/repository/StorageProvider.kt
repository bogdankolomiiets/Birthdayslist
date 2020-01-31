package com.bogdan.kolomiiets.birthdayreminder.repository

import androidx.lifecycle.LiveData
import com.bogdan.kolomiiets.birthdayreminder.EventReminderApp
import com.bogdan.kolomiiets.birthdayreminder.database.EventsDao
import com.bogdan.kolomiiets.birthdayreminder.database.EventsDatabase
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import io.reactivex.Single
import java.util.concurrent.Executors

class StorageProvider {
    private val eventsDao: EventsDao by lazy { EventsDatabase.getInstance(EventReminderApp.context).eventsDao() }

    fun insertEvent(event: Event) {
        Executors.newSingleThreadExecutor().execute {eventsDao.insertEvent(event)}
    }

    fun insertEvents(events: List<Event>) {
        events.forEach { eventsDao.insertEvent(it) }
    }

    fun deleteEvent(eventId: Int){
        Executors.newSingleThreadExecutor().execute {eventsDao.deleteEvent(eventId)}
    }

    fun updateEvent(event: Event){
        Executors.newSingleThreadExecutor().execute {eventsDao.updateEvent(event)}
    }

    fun getEvents(): Single<List<Event>> {
        return eventsDao.getEvents()
    }

    fun getEvents(eventName: String): LiveData<List<Event>> {
        return eventsDao.getEvents("%$eventName%")
    }

    fun getEventsOnDate(month: Int, day: Int): List<Event> {
        return eventsDao.getEventsOnDate(month, day)
    }
}