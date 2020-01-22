package com.bogdan.kolomiiets.birthdayreminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider
import io.reactivex.Single

class EventsViewModel : ViewModel() {
    private val storageProvider = StorageProvider()
    val events: LiveData<List<Event>>
    val filter = MutableLiveData<String>("")

    init {
        events = Transformations.switchMap(filter) { storageProvider.getEvents(it) }
    }

    fun deleteEvent(eventId: Int) {
        storageProvider.deleteEvent(eventId)
    }

    fun getEvents(): Single<List<Event>> {
        return storageProvider.getEvents()
    }

    fun insertEvents(events: List<Event>) {
        storageProvider.insertEvents(events)
    }
}