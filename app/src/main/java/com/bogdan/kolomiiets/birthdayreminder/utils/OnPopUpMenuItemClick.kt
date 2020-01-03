package com.bogdan.kolomiiets.birthdayreminder.utils

import com.bogdan.kolomiiets.birthdayreminder.models.Event

interface OnPopUpMenuItemClick {
    fun deleteItemClick(eventId: Int)
    fun changeItemClick(event: Event)
}