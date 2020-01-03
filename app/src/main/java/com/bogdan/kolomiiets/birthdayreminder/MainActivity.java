package com.bogdan.kolomiiets.birthdayreminder;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
/*
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SQLiteDBHelper dbHelper;
    private SQLiteDatabase db;
    private FloatingActionButton fab;
    private RelativeLayout relativeLayout;
    private ListView lv_dynamic;
    private int mItemId = 0;
    private String mItemName = "";
    private String mItemPhone = "";
    private AdView mAdView;
    public static InterstitialAd mInterstitialAd;
    private SharedPreferences preferences;
    public static int countForShowAd;
    public static final int clickCountForShowAd = 5;
    private ListAdapter adapter;
    private String selection = SQLiteDBHelper.KEY_NAME + " like ?";
    private String[] selectionArgs = {"%%"};
    private String searchQuery;
    private MenuItem item;
    private SearchView searchView;
    private TextView noData;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fab.requestFocus();
                return false;
            }
        });


        //Setup default sharedPreferences
        initSharedPreferences();

        //init searchQuery
        if (savedInstanceState != null)
        {
            searchQuery = savedInstanceState.getString("searchQuery");
        }

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        //create TextView noData for empty ListView
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar = new ProgressBar(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setLayoutParams(progressParams);

        RelativeLayout.LayoutParams noDataParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        noData = new TextView(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
        noData.setText(R.string.nothing_to_show);
        noData.setGravity(Gravity.CENTER);
        noData.setTextSize(getResources().getDimension(R.dimen.size12));
        noDataParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noData.setLayoutParams(noDataParams);

        relativeLayout.addView(noData);
        relativeLayout.addView(progressBar);

        lv_dynamic = (ListView) findViewById(R.id.lv_dynamic);

        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this, getResources().getString(R.string.app_id));

        //BannerAd
        AdRequest adRequest = new AdRequest.Builder().build();
        //adRequest.isTestDevice(this);
        mAdView.loadAd(adRequest);

        //InterstitialAd
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8123334718162410/4114864477");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdOpened() {
                countForShowAd = 0;
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("selectionArgs", selectionArgs);
        outState.putString("searchQuery", searchView.getQuery().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectionArgs = savedInstanceState.getStringArray("selectionArgs");
    }

    @Override
    protected void onResume() {
        super.onResume();
        QueryToDB();
    }

    //Setup default sharedPreferences
    private void initSharedPreferences() {
        preferences = getSharedPreferences(SettingsActivity.PREFERENCES_NAME, MODE_PRIVATE);
        if (!preferences.contains(SettingsActivity.SWITCH)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(SettingsActivity.SWITCH, true);
            editor.putInt(SettingsActivity.CHECKED_RADIO, 0);
            editor.putInt(SettingsActivity.REMINDER_HOUR, 9);
            editor.putInt(SettingsActivity.REMINDER_MINUTE, 0);
            editor.commit();

            //Set alarm
            SetAlarm setAlarm = new SetAlarm();
            setAlarm.onAlarm(this,true, 9, 0);
        }
    }

    public void onAboutMenuItem(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
        builder.setTitle(R.string.about_the_program);
        builder.setIcon(R.mipmap.ic_launcher);
        View view = getLayoutInflater().inflate(R.layout.about_layout, null);
        builder.setView(view);
        TextView feedback = view.findViewById(R.id.email);
        feedback.setOnClickListener(this);

        builder.setPositiveButton("FEEDBACK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: bogdan.kolomiiets@gmail.com"));
                startActivity(Intent.createChooser(intent, "Send feedback"));
            }
        });
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }

    public void onSettingsMenuItem(MenuItem item) {
        Intent intent = new Intent(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onExportData(MenuItem item){
        ExportDataFromDatabase data_export = new ExportDataFromDatabase(this);
        data_export.execute();
    }

    public void onImportData(MenuItem item){
        Intent selectFileIntent = null;
        /*if (Build.VERSION.SDK_INT >= 19){
            selectFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            selectFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            selectFileIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else {*//*
            selectFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //}
        //selectFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //selectFileIntent.setType("");
        selectFileIntent.setType("application/octet-stream");
        startActivityForResult(selectFileIntent, RequestCodes.SELECT_FILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        if (searchQuery != null && !searchQuery.isEmpty())
        {
            searchView.setQuery(searchQuery, false);
            searchView.setIconified(false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                selectionArgs = new String[] {"%" + newText.toLowerCase().trim() + "%"};
                QueryToDB();
                return true;
            }
        });
    return true;
    }

    public void onContextChange(MenuItem item){
        Intent intent = new Intent(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, EditCelebrationActivity.class);
        intent.putExtra("id", mItemId);
        startActivity(intent);
        finish();
    }

    public void onContextDelete(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
        builder.setTitle(R.string.are_you_sure);
        builder.setCancelable(true);
        builder.setMessage(getResources().getString(R.string.really_delete) + " " + mItemName + "\n" + mItemPhone);
        builder.setNegativeButton(R.string.negative_answer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.positive_answer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteRowFromDatabase delete = new DeleteRowFromDatabase(mItemId, com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
                if (delete.deleteRow()){
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.not_deleted), Toast.LENGTH_SHORT).show();
                }
                QueryToDB();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void QueryToDB() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                try {
                    dbHelper = new SQLiteDBHelper(getApplicationContext());
                    db = dbHelper.getReadableDatabase();
                    Cursor cursor = db.query(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.COLUMNS_NAMES, selection, selectionArgs, null, null, null);
                    final ArrayList<DataForListView> list = new ArrayList<>();

                    if (cursor.getCount() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                noData.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                noData.setVisibility(View.INVISIBLE);

                            }
                        });
                        cursor.moveToFirst();
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_ID));
                            String name = cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_NAME));
                            String phone = cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_PHONE));

                            String type = "";
                            switch (cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE))) {
                                case 1:
                                    type = getResources().getString(R.string.birthday) + ":";
                                    break;
                                case 2:
                                    type = getResources().getString(R.string.anniversary) + ":";
                                    break;
                                case 3:
                                    type = getResources().getString(R.string.holiday) + ":";
                                    break;
                            }

                            String date;
                            int year = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_YEAR));
                            int month = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_MONTH));
                            int day = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_DAY));

                            String age = "";
                            //Check if it's holiday, then skip year
                            if (year > 1) {
                                if (Locale.getDefault().getLanguage().equals("ru") || Locale.getDefault().getLanguage().equals("uk")) {
                                    date = day + "/" + month + "/" + year;
                                } else date = year + "-" + month + "-" + day;
                                //Calculate age
                                age = String.valueOf(new HowOld().getHowOld(year, month, day));
                            } else {
                                if (Locale.getDefault().getLanguage().equals("ru") || Locale.getDefault().getLanguage().equals("uk")) {
                                    date = day + "/" + month;
                                } else date = month + "-" + day;
                            }



                            list.add(new DataForListView(String.valueOf(id), name, phone, type, age, date));
                        } while (cursor.moveToNext());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new SimpleAdapter(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, list, R.layout.not_simple_item, new String[]
                                    {SQLiteDBHelper.KEY_ID, SQLiteDBHelper.KEY_NAME, SQLiteDBHelper.KEY_PHONE, SQLiteDBHelper.KEY_CELEBRATION_TYPE, "date", "age"},
                                    new int[]{R.id.tv_id, R.id.name_container, R.id.phone_container, R.id.type_container, R.id.date_container, R.id.age_container});
                            lv_dynamic.setAdapter(adapter);
                            lv_dynamic.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    TextView mTv_id = (TextView) view.findViewById(R.id.tv_id);
                                    TextView mName_container = (TextView) view.findViewById(R.id.name_container);
                                    TextView mPhone_container = (TextView) view.findViewById(R.id.phone_container);
                                    mItemId = Integer.parseInt(mTv_id.getText().toString());
                                    mItemName = mName_container.getText().toString();
                                    mItemPhone = mPhone_container.getText().toString();
                                    return false;
                                }
                            });
                            lv_dynamic.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                                @Override
                                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                                    MenuInflater inflater = new MenuInflater(v.getContext());
                                    inflater.inflate(R.menu.popup_menu, menu);
                                }
                            });
                        }
                    });
                    cursor.close();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    db.close();
                }
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.fab:
                countForShowAd++;
                if (countForShowAd > clickCountForShowAd){
                    if (mInterstitialAd.isLoaded()){
                        mInterstitialAd.show();
                    }
                }
                intent = new Intent(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, NewCelebrationActivity.class);
                startActivity(intent);
                break;
            case R.id.email:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: bogdan.kolomiiets@gmail.com"));
                startActivity(Intent.createChooser(intent, "Send feedback"));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SELECT_FILE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            importDataToDatabase(uri);
        }
    }

    private void importDataToDatabase(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArray;
                JSONObject jsonObject;
                ContentValues contentValues;
                SQLiteDBHelper helper;
                SQLiteDatabase database;
                BufferedReader bufferedReader = null;
                boolean import_result = false;

                helper = new SQLiteDBHelper(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this);
                database = helper.getWritableDatabase();
                contentValues = new ContentValues();
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this.getContentResolver().openInputStream(uri))));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    try {
                        jsonArray = new JSONArray(stringBuilder.toString());
                        database.beginTransaction();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            contentValues.put(SQLiteDBHelper.KEY_NAME, jsonObject.getString(SQLiteDBHelper.KEY_NAME));
                            contentValues.put(SQLiteDBHelper.KEY_PHONE, jsonObject.getString(SQLiteDBHelper.KEY_PHONE));
                            contentValues.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, jsonObject.getInt(SQLiteDBHelper.KEY_CELEBRATION_TYPE));
                            contentValues.put(SQLiteDBHelper.KEY_YEAR, jsonObject.getInt(SQLiteDBHelper.KEY_YEAR));
                            contentValues.put(SQLiteDBHelper.KEY_MONTH, jsonObject.getInt(SQLiteDBHelper.KEY_MONTH));
                            contentValues.put(SQLiteDBHelper.KEY_DAY, jsonObject.getInt(SQLiteDBHelper.KEY_DAY));
                            database.insert(SQLiteDBHelper.TABLE_NAME, null, contentValues);
                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();
                        import_result = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        import_result = false;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    database.close();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                if (import_result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, getApplicationContext().getResources().getString(R.string.imported_successful), Toast.LENGTH_SHORT).show();
                            QueryToDB();
                        }
                    });
                } else if (!import_result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.this, getApplicationContext().getResources().getString(R.string.not_imported), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        }).start();
    }
}
*/
