package com.bogdan.kolomiiets.birthdayreminder.views

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.INTENT_ACTION_GET_CONTENT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT
import com.bogdan.kolomiiets.birthdayreminder.RequestCodes.Companion.REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT
import com.bogdan.kolomiiets.birthdayreminder.adapters.EventsRecyclerViewAdapter
import com.bogdan.kolomiiets.birthdayreminder.models.Event
import com.bogdan.kolomiiets.birthdayreminder.utils.*
import com.bogdan.kolomiiets.birthdayreminder.viewmodels.EventsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), View.OnClickListener, OnPopUpMenuItemClick {
    private lateinit var newEventFab: FloatingActionButton
    private lateinit var noEvents: TextView
    private lateinit var eventViewModel: EventsViewModel
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventsRecyclerViewAdapter: EventsRecyclerViewAdapter
    private lateinit var searchView: SearchView
    private lateinit var disposable: Disposable

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
            R.id.item_import_into_database -> pickContentForImport()
            R.id.item_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    private fun exportToFile() =
            if (PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                if (isExternalStorageAvailable() and !isExternalStorageReadOnly()) {
                    disposable = eventViewModel.getEvents()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ eventsList ->
                                try {
                                    val jsonElement = GsonBuilder().create().toJsonTree(eventsList)
                                    val filename = getString(R.string.file_name, getStringDate())

                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        val path = File(Environment.getExternalStorageDirectory().absolutePath.plus(File.separator).plus(getString(R.string.app_name)))
                                        path.mkdir()
                                        val file = File(path, filename)
                                        file.createNewFile()
                                        FileOutputStream(file).use { fileOutputStream ->
                                            fileOutputStream.write(jsonElement.toString().toByteArray())
                                            fileOutputStream.flush()
                                            fileOutputStream.close()
                                            showToast(getString(R.string.exported_successfully, file))
                                        }
                                    } else {
                                        val volume = MediaStore.VOLUME_EXTERNAL_PRIMARY
                                        val uriExternal = MediaStore.Files.getContentUri(volume)
                                        val contentResolver = contentResolver
                                        val contentValues = ContentValues()
                                        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "json")
                                        contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, getString(R.string.app_name))
                                        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename)
                                        val result = contentResolver.insert(uriExternal, contentValues)
                                        result?.path?.let { showToast(it) }
                                    }
                                } catch (ex: Exception) {
                                    showToast(ex.message ?: ex.toString())
                                }
                            }, { error: Throwable? -> showToast(error?.message ?: error.toString()) })
                } else Toast.makeText(this, R.string.external_storage_not_available, Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT)
            }

    private fun pickContentForImport() {
        if (PermissionChecker.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            if (intent.resolveActivity(packageManager) != null) {
                showToast(getString(R.string.select_a_file_for_import))
                startActivityForResult(intent, INTENT_ACTION_GET_CONTENT)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT)
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
                if (events.isNotEmpty()) {
                    eventViewModel.insertEvents(events)
                }
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
        if (grantResults.isNotEmpty() and (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
            when (requestCode) {
                REQUEST_READ_EXTERNAL_STORAGE_FOR_IMPORT -> pickContentForImport()
                REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT -> exportToFile()
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            INTENT_ACTION_GET_CONTENT -> if (resultCode == Activity.RESULT_OK) executeImportDataIntoDatabase(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!::disposable.isInitialized.and(!disposable.isDisposed)) {
            disposable.dispose()
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}