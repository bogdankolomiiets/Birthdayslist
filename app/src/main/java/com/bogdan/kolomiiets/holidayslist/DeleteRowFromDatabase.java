package com.bogdan.kolomiiets.holidayslist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DeleteRowFromDatabase{

    private int mId;
    private Context context;
    private SQLiteDBHelper helper;
    private SQLiteDatabase db;

    DeleteRowFromDatabase(int id, Context context){
        this.mId = id;
        this.context = context;
    }

    public boolean deleteRow(){
        helper = new SQLiteDBHelper(context);
        db = helper.getWritableDatabase();
        return db.delete(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.KEY_ID + "=" + mId, null) > 0;
    }
}
