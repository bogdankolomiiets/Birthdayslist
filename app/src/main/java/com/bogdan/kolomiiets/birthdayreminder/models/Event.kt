package com.bogdan.kolomiiets.birthdayreminder.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_NAME
import com.bogdan.kolomiiets.birthdayreminder.database.DatabaseConstants.Companion.TABLE_NAME
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = TABLE_NAME, indices = [Index(EVENT_NAME, unique = false)])
data class Event(
        @PrimaryKey(autoGenerate = true)
        val _id: Int = 0,
        var name: String = "",
        var phone: String = "",
        var type: Int = 0,
        var celebration_year: Int = 0,
        var celebration_month: Int = 0,
        var celebration_day: Int = 0) : Parcelable {

        companion object {
                const val TYPE_BIRTHDAY = 1
                const val TYPE_ANNIVERSARY = 2
                const val TYPE_HOLIDAY = 3
        }
}