package com.bogdan.kolomiiets.birthdayreminder.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.adapters.EventsRecyclerViewAdapter
import com.bogdan.kolomiiets.birthdayreminder.viewmodels.EventsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity: AppCompatActivity(), View.OnClickListener{
    private lateinit var newEventFab: FloatingActionButton
    private lateinit var noEvents: TextView
    private lateinit var eventViewModel: EventsViewModel
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventsRecyclerViewAdapter: EventsRecyclerViewAdapter
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init UI components
        newEventFab = findViewById(R.id.new_event_fab)
        newEventFab.setOnClickListener(this)
        noEvents = findViewById(R.id.no_events)
        eventRecyclerView = findViewById(R.id.event_recycler)

        //init viewModel
        eventViewModel = ViewModelProviders.of(this).get(EventsViewModel::class.java)
        eventViewModel.events.observe(this, Observer {
            noEvents.isVisible = it.isNullOrEmpty()
            eventsRecyclerViewAdapter.setData(it)
        })

        //setup recyclerView
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerViewAdapter = EventsRecyclerViewAdapter()
        eventRecyclerView.adapter = eventsRecyclerViewAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        searchView = (menu.findItem(R.id.item_search)).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                eventViewModel.filter.postValue(newText ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_about -> {
                showAboutDialog()
            }
            R.id.item_export_to_file -> {}
            R.id.item_import_into_database -> {}
            R.id.item_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return true
    }

    private fun showAboutDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(R.string.about_message)
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        dialogBuilder.show()
    }

    override fun onClick(viev: View) {
        when (viev.id) {
            R.id.new_event_fab -> startActivity(Intent(this, NewOrUpdateEventActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (!searchView.isIconified) {
            searchView.onActionViewCollapsed()
        } else {
            super.onBackPressed()
        }
    }
}