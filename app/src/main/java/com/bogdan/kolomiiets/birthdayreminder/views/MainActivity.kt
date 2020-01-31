package com.bogdan.kolomiiets.birthdayreminder.views

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.IS_REMINDER_ON
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.PREFERENCES_NAME
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.RADIO_SAME_DAY
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_HOUR
import com.bogdan.kolomiiets.birthdayreminder.Constants.Companion.REMINDER_MINUTE
import com.bogdan.kolomiiets.birthdayreminder.R
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.INTENT_ACTION_CREATE_DOCUMENT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.INTENT_ACTION_GET_CONTENT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT
import com.bogdan.kolomiiets.birthdayreminder.adapters.EventsRecyclerViewAdapter
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.models.Event.Companion.TYPE_HOLIDAY
import com.bogdan.kolomiiets.birthdayreminder.utils.*
import com.bogdan.kolomiiets.birthdayreminder.viewmodels.EventsViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), View.OnClickListener, OnPopUpMenuItemClick {
    private lateinit var newEventFab: FloatingActionButton
    private lateinit var noEvents: TextView
    private lateinit var eventViewModel: EventsViewModel
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventsRecyclerViewAdapter: EventsRecyclerViewAdapter
    private lateinit var searchView: SearchView
    private lateinit var disposable: Disposable
    private lateinit var adView: AdView
    private lateinit var jsonElement: JsonElement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init adView and adListener
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView = findViewById(R.id.adView)
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {

            override fun onAdClosed() {
                adView.loadAd(adRequest)
            }
        }

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

        //checking if it's first run
        val preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (!preferences.contains(CHECKED_RADIO)) {
            preferences
                    .edit()
                    .putBoolean(IS_REMINDER_ON, true)
                    .putInt(CHECKED_RADIO, RADIO_SAME_DAY)
                    .putInt(REMINDER_HOUR, BASE_REMINDER_HOUR)
                    .putInt(REMINDER_MINUTE, BASE_REMINDER_MINUTE)
                    .apply()

            setEveryDayAlarm(this, BASE_REMINDER_HOUR, BASE_REMINDER_MINUTE, true)
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
            R.id.item_export_to_file -> onMenuItemExportToFile()
            R.id.item_import_into_database -> onMenuItemImportIntoDatabase()
            R.id.item_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    private fun onMenuItemExportToFile() =
            if (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)) {
                if (isExternalStorageAvailable() and !isExternalStorageReadOnly()) {
                    disposable = eventViewModel.getEvents()
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess { eventsList ->
                                if (eventsList.isNotEmpty()) {
                                    jsonElement = GsonBuilder().create().toJsonTree(eventsList)
                                    val filename = getString(R.string.file_name, getStringDate())
                                        intentCreateFile("*/*", filename)
                                } else {
                                    runOnUiThread { showToast(R.string.nothing_to_import) }
                                }
                            }
                            .doOnError { error: Throwable? -> showToast(error?.message ?: error.toString()) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()
                } else showToast(R.string.external_storage_not_available)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT)
            }

    private fun intentCreateFile(mimeType: String, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, INTENT_ACTION_CREATE_DOCUMENT)
    }

    private fun onMenuItemImportIntoDatabase() {
        if (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(this, READ_EXTERNAL_STORAGE)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.resolveActivity(packageManager)?.let {
                showToast(getString(R.string.select_a_file_for_import))
                startActivityForResult(intent, INTENT_ACTION_GET_CONTENT)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT)
        }
    }

    private fun executeExportDataToFile(resultIntent: Intent?) {
        resultIntent?.data?.let {
            contentResolver.openOutputStream(it).use { outputStream: OutputStream? -> outputStream?.write(jsonElement.toString().toByteArray())
            outputStream?.flush()
            }
        }
    }

        private fun executeImportDataIntoDatabase(resultIntent: Intent?) {
        resultIntent?.data?.let {
            Executors.newSingleThreadExecutor().execute {
                val stringBuilder = StringBuilder()
                contentResolver.openInputStream(it).use { inputStream: InputStream? -> inputStream?.bufferedReader()?.forEachLine { line -> stringBuilder.append(line) } }

                /**
                 * working code
                 * version 1
                 */
                /*val jsonArray = JSONArray(stringBuilder.toString())
                val events = mutableListOf<Event>()
                for (i in 0 until jsonArray.length()) {
                    events.add(Gson().fromJson(jsonArray[i].toString(), Event::class.java))
                }*/

                /**
                 * working code
                 * version 2
                 */
                val events = Gson().fromJson<List<Event>>(stringBuilder.toString(), object : TypeToken<List<Event>>() {}.type)

                if (!events.isNullOrEmpty()) {
                    //preparing for import
                    events.forEach { if (it.type == TYPE_HOLIDAY) it.phone = "" }
                    //execute import
                    eventViewModel.insertEvents(events)
                } else runOnUiThread { showToast(R.string.nothing_to_import) }
            }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() and (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
            when (requestCode) {
                REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT -> onMenuItemImportIntoDatabase()
                REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT -> onMenuItemExportToFile()
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                INTENT_ACTION_GET_CONTENT -> executeImportDataIntoDatabase(data)
                INTENT_ACTION_CREATE_DOCUMENT -> executeExportDataToFile(data)
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::disposable.isInitialized) {
            disposable.dispose()
        }
    }
}