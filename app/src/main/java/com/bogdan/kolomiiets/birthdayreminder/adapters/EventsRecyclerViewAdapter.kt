package com.bogdan.kolomiiets.birthdayreminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.databinding.SingleEventItemBinding
import com.bogdan.kolomiiets.birthdayreminder.models.Event

class EventsRecyclerViewAdapter: RecyclerView.Adapter<EventsRecyclerViewAdapter.EventsViewHolder>() {
    private val events: MutableList<Event> = mutableListOf()
    private lateinit var binding: SingleEventItemBinding
    private lateinit var phoneContainer: TextView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.single_event_item, parent, false)
        phoneContainer = binding.phoneContainer
        return EventsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.bind(events[position])
    }

    fun setData(eventsList: List<Event>) {
        events.clear()
        events.addAll(eventsList)
        notifyDataSetChanged()
    }

    inner class EventsViewHolder(binding: SingleEventItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            phoneContainer.isVisible = event.phone.isNotEmpty()
            binding.event = event
            binding.executePendingBindings()
        }
    }
}