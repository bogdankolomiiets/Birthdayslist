package com.bogdan.kolomiiets.birthdayreminder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bogdan.kolomiiets.birthdayreminder.models.Event

@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class EventsDatabase: RoomDatabase() {
    abstract fun eventsDao(): EventsDao

    companion object {
        private lateinit var INSTANCE: EventsDatabase

        fun getInstance(context: Context): EventsDatabase {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = Room.databaseBuilder(context, EventsDatabase::class.java, DatabaseConstants.DATABASE_NAME).build()
            }
            return INSTANCE
        }
    }
}