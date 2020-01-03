package com.bogdan.kolomiiets.birthdayreminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.repository.StorageProvider

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
}