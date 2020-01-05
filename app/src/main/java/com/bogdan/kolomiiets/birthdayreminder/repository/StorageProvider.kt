package com.bogdan.kolomiiets.birthdayreminder.repository

import androidx.lifecycle.LiveData
import com.bogdan.kolomiiets.birthdayreminder.BirthdayApp
import com.bogdan.kolomiiets.birthdayreminder.database.EventsDao
import com.bogdan.kolomiiets.birthdayreminder.database.EventsDatabase
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.Executors

class StorageProvider {
    private val eventsDao: EventsDao by lazy { EventsDatabase.getInstance(BirthdayApp.context).eventsDao() }

    fun insertEvent(event: Event) {
        Executors.newSingleThreadExecutor().execute {eventsDao.insertEvent(event)}
    }

    fun deleteEvent(eventId: Int){
        Executors.newSingleThreadExecutor().execute {eventsDao.deleteEvent(eventId)}
    }

    fun updateEvent(event: Event){
        Executors.newSingleThreadExecutor().execute {eventsDao.updateEvent(event)}
    }

    fun getEvents(): Observable<List<Event>> {
        return Observable.fromPublisher<List<Event>> { eventsDao.getEvents() }
    }

    fun getEvents(eventName: String): LiveData<List<Event>> {
        return eventsDao.getEvents("%$eventName%")
    }

    fun getEventsOnToday(): List<Event> {
        return eventsDao.getEventsOnToday(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }
}