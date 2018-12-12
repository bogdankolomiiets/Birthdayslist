package com.bogdan.kolomiiets.holidayslist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "HOLIDAYS";
    public static final String TABLE_NAME = "LISTS";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_CELEBRATION_TYPE = "type";
    public static final String KEY_YEAR = "celebration_year";
    public static final String KEY_MONTH = "celebration_month";
    public static final String KEY_DAY = "celebration_day";

    public static final String[] COLUMNS_NAMES = new String[]{KEY_ID, KEY_NAME, KEY_PHONE, KEY_CELEBRATION_TYPE, KEY_YEAR, KEY_MONTH, KEY_DAY};

    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " TEXT, "
                + KEY_PHONE + " TEXT, " + KEY_CELEBRATION_TYPE + " NUMERIC, " + KEY_YEAR + " NUMERIC, " + KEY_MONTH + " NUMERIC, " + KEY_DAY + " NUMERIC)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(query);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(query);
        onCreate(db);
    }
}
