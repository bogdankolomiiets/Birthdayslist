package com.bogdan.kolomiiets.birthdayreminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_HOLIDAY
import com.bogdan.kolomiiets.birthdayreminder.utils.OnPopUpMenuItemClick
import com.bogdan.kolomiiets.birthdayreminder.utils.calculateAge
import com.bogdan.kolomiiets.birthdayreminder.utils.convertEventDateToString
import com.bogdan.kolomiiets.birthdayreminder.utils.convertIntTypeToString

class EventsRecyclerViewAdapter(private val onPopUpMenuItemClick: OnPopUpMenuItemClick): RecyclerView.Adapter<EventsRecyclerViewAdapter.EventsViewHolder>() {
    private val events: MutableList<Event> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        return EventsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_event_item, parent, false))
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val event = events[position]
        holder.name.text = event.name
        holder.phone.text = event.phone
        holder.phone.isVisible = event.phone.isNotEmpty()
        holder.type.convertIntTypeToString(event.type)

        if (event.type != TYPE_HOLIDAY) {
            holder.age.calculateAge(event.celebration_year, event.celebration_month, event.celebration_day)
        }

        holder.ageContainer.isVisible = event.type != TYPE_HOLIDAY
        holder.date.convertEventDateToString(event.celebration_year, event.celebration_month, event.celebration_day, event.type == TYPE_HOLIDAY)

        holder.verticalMenu.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, it)
            popupMenu.inflate(R.menu.popup_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteItem -> onPopUpMenuItemClick.deleteItemClick(event._id)
                    R.id.changeItem -> onPopUpMenuItemClick.changeItemClick(event)
                }
                return@setOnMenuItemClickListener true
            }

            /*
            *useful code for showing icons
            *from https://resocoder.com/2018/02/02/popup-menu-with-icons-android-kotlin-tutorial-code/
            */
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup[popupMenu]
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
            } catch (ex: Exception) { /* stub */ }

            popupMenu.show()
        }
    }

    fun setData(eventsList: List<Event>) {
        events.clear()
        events.addAll(eventsList)
        notifyDataSetChanged()
    }

    inner class EventsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val phone: TextView = view.findViewById(R.id.phone)
        val type: TextView = view.findViewById(R.id.type)
        val date: TextView = view.findViewById(R.id.date)
        val ageContainer: LinearLayout = view.findViewById(R.id.age_container)
        val age: TextView = view.findViewById(R.id.age)
        val verticalMenu: TextView = view.findViewById(R.id.vertical_menu)
    }
}