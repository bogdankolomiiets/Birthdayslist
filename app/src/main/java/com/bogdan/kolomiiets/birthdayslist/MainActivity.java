package com.bogdan.kolomiiets.birthdayslist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SQLiteDBHelper dbHelper;
    private SQLiteDatabase db;
    private FloatingActionButton fab;
    private ConstraintLayout constraintLayout;
    private ListView lv_dynamic;
    private TextView tv_id;
    private int mItemId = 0;
    private String mItemName = "";
    private String mItemPhone = "";
    private AdView mAdView;
    public static InterstitialAd mInterstitialAd;
    private SharedPreferences preferences;
    public static int countForAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //Setup default sharedPreferences
        initSharedPreferences();

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        lv_dynamic = (ListView) findViewById(R.id.lv_dynamic);
        tv_id = (TextView) findViewById(R.id.tv_id);
        QueryToDB();
        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this, getResources().getString(R.string.APP_ID));

        //BannerAd
        AdRequest adRequest = new AdRequest.Builder().build();
        adRequest.isTestDevice(this);
        mAdView.loadAd(adRequest);

        //InterstitialAd
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdOpened() {
                countForAd = 0;
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

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
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.aboutTheProgram);
        builder.setIcon(R.mipmap.ic_launcher);
        View view = getLayoutInflater().inflate(R.layout.about_layout, null);
        builder.setView(view);
        TextView feedback = view.findViewById(R.id.tvEmail);
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
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    public void onContextChange(MenuItem item){
        Intent intent = new Intent(MainActivity.this, EditBirthdayActivity.class);
        intent.putExtra("id", mItemId);
        startActivity(intent);
        finish();
    }

    public void onContextDelete(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.Question);
        builder.setCancelable(true);
        builder.setMessage(getResources().getString(R.string.reallyDelete) + " " + mItemName + "\n" + mItemPhone);
        builder.setNegativeButton(R.string.Negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.Positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteRowFromDatabase delete = new DeleteRowFromDatabase(mItemId, MainActivity.this);
                if (delete.deleteRow()){
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.notDeleted), Toast.LENGTH_SHORT).show();
                }
                MainActivity.this.recreate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void QueryToDB() {
        try {
            dbHelper = new SQLiteDBHelper(this);
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.COLUMNS_NAMES, null, null, null, null, null);
            ArrayList<DataForListView> list = new ArrayList<>();
            //Calendar current_date = Calendar.getInstance();

            if (cursor.getCount() == 0) {
                TextView noData = new TextView(MainActivity.this);
                noData.setText(R.string.noData);
                noData.setGravity(Gravity.CENTER);
                noData.setTextSize(getResources().getDimension(R.dimen.size12));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                noData.setLayoutParams(params);
                constraintLayout.addView(noData);
            } else {
                cursor.moveToFirst();
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_ID));
                    String name = cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_NAME));
                    String phone = cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_PHONE));

                    String type = "";
                    switch (cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE))) {
                        case 1:
                            type = getResources().getString(R.string.Birthday) + ":";
                            break;
                        case 2:
                            type = getResources().getString(R.string.Anniversary) + ":";
                            break;
                        case 3:
                            type = getResources().getString(R.string.Another) + ":";
                            break;
                    }

                    String date;
                    int year = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_YEAR));
                    int month = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_MONTH));
                    int day = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_DAY));

                    if (Locale.getDefault().getLanguage().equals("ru") || Locale.getDefault().getLanguage().equals("uk")) {
                        date = day + "/" + month + "/" + year;
                    } else date = year + "-" + month + "-" + day;

                    //Calculate age
                    int age = new HowOld().getHowOld(year, month, day);

                    list.add(new DataForListView(String.valueOf(id), name, phone, type, String.valueOf(age), date));
                } while (cursor.moveToNext());
            }

            ListAdapter adapter = new SimpleAdapter(MainActivity.this, list, R.layout.not_simple_item, new String[]
                    {SQLiteDBHelper.KEY_ID, SQLiteDBHelper.KEY_NAME, SQLiteDBHelper.KEY_PHONE, SQLiteDBHelper.KEY_CELEBRATION_TYPE, "date", "age"},
                    new int[] {R.id.tv_id, R.id.name_container, R.id.phone_container, R.id.type_container, R.id.date_container, R.id.age_container});
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
                    inflater.inflate(R.menu.context_menu, menu);
                }
            });

            cursor.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            db.close();
            fab.requestFocus();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.fab:
                countForAd++;
                if (countForAd > 7){
                    if (mInterstitialAd.isLoaded()){
                        mInterstitialAd.show();
                    }
                }
                intent = new Intent(MainActivity.this, NewBirthdayActivity.class);
                startActivity(intent);
                break;
            case R.id.tvEmail:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: bogdan.kolomiiets@gmail.com"));
                startActivity(Intent.createChooser(intent, "Send feedback"));
                break;
        }
    }
}
