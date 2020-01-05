package com.bogdan.kolomiiets.birthdayreminder.views

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.BASE_REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.CHECKED_RADIO
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.EVENT
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.IS_FIRST_RUN
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_SAME_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_ON_OFF
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT
import com.bogdan.kolomiiets.birthdayreminder.adapters.EventsRecyclerViewAdapter
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.utils.OnPopUpMenuItemClick
import com.bogdan.kolomiiets.birthdayreminder.utils.WhoCelebrateService
import com.bogdan.kolomiiets.birthdayreminder.utils.isExternalStorageAvailable
import com.bogdan.kolomiiets.birthdayreminder.utils.isExternalStorageReadOnly
import com.bogdan.kolomiiets.birthdayreminder.viewmodels.EventsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.*

class MainActivity: AppCompatActivity(), View.OnClickListener, OnPopUpMenuItemClick{
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
        eventsRecyclerViewAdapter = EventsRecyclerViewAdapter(this)
        eventRecyclerView.adapter = eventsRecyclerViewAdapter

        //if it's first run
        val preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (preferences.getBoolean(IS_FIRST_RUN, true)) {
            preferences
                    .edit()
                    .putBoolean(IS_FIRST_RUN, false)
                    .putBoolean(REMINDER_ON_OFF, true)
                    .putInt(CHECKED_RADIO, RADIO_SAME_DAY)
                    .putInt(REMINDER_HOUR, BASE_REMINDER_HOUR)
                    .putInt(REMINDER_MINUTE, BASE_REMINDER_MINUTE)
                    .apply()

            setAlarm()
        }
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
            R.id.item_about -> showAboutDialog()
            R.id.item_export_to_file -> exportToFile()
            R.id.item_import_into_database -> importDataIntoDatabase()
            R.id.item_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    private fun exportToFile() {
        if (PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            if (isExternalStorageAvailable() and !isExternalStorageReadOnly()) {
                val single = Single.fromCallable { eventViewModel.getEvents() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show() }
                        .doOnSuccess {
                            println("doOnSuccess")
                            val eventJsonArray = Gson().toJson(it, Event::class.java)
                            println(eventJsonArray)
                        }
            } else Toast.makeText(this, R.string.external_storage_not_available, Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT)
        }
    }

    private fun importDataIntoDatabase() {
        if (PermissionChecker.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Toast.makeText(this, "Import", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT)
        }
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

    override fun deleteItemClick(eventId: Int) {
        eventViewModel.deleteEvent(eventId)
    }

    override fun changeItemClick(event: Event) {
        startActivity(Intent(this, NewOrUpdateEventActivity::class.java).putExtra(EVENT, event))
    }

    private fun setAlarm() {
        try {
            val intent = Intent(applicationContext, WhoCelebrateService::class.java)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(this, RequestCodes.PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getService(this, RequestCodes.PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR, BASE_REMINDER_HOUR)
            calendar.set(Calendar.MINUTE, BASE_REMINDER_MINUTE)

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT -> {
                if (grantResults.isNotEmpty() and (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    importDataIntoDatabase()
                }
            }
            REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT -> {
                if (grantResults.isNotEmpty() and (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    exportToFile()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}