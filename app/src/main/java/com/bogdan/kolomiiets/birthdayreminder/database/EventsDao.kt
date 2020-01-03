package com.bogdan.kolomiiets.birthdayreminder.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bogdan.kolomiiets.birthdayreminder.Constants
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_MONTH
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_NAME
import com.bogdan.kolomiiets.birthdayreminder.database.DatabaseConstants.Companion.TABLE_NAME
import com.bogdan.kolomiiets.birthdayreminder.models.Event

@Dao
interface EventsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event)

    @Delete
    fun deleteEvent(event: Event)

    @Update
    fun updateEvent(event: Event)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $EVENT_NAME LIKE :eventName")
    fun getEvents(eventName: String): LiveData<List<Event>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $EVENT_MONTH == :month and $EVENT_DAY == :day")
    fun getEventsOnToday(month: Int, day: Int): List<Event>
}