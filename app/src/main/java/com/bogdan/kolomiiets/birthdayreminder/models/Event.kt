package com.bogdan.kolomiiets.birthdayreminder.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_ID
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_MONTH
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT_YEAR
import com.bogdan.kolomiiets.birthdayreminder.database.DatabaseConstants.Companion.TABLE_NAME
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = TABLE_NAME)
data class Event(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = EVENT_ID) val id: Int = 0,
        @ColumnInfo(name = EVENT_NAME) var name: String = "",
        var phone: String = "",
        var type: Int = 0,
        @ColumnInfo(name = EVENT_YEAR) var year: Int = 0,
        @ColumnInfo(name = EVENT_MONTH) var month: Int = 0,
        @ColumnInfo(name = EVENT_DAY) var day: Int = 0) : Parcelable