package com.bogdan.kolomiiets.birthdayslist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        Log.d("HERE", "I am");
        return db.delete(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.KEY_ID + "=" + mId, null) > 0;
    }
}
