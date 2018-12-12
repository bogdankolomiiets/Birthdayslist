package com.bogdan.kolomiiets.holidayslist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExportDataFromDatabase extends AsyncTask{
    private SQLiteDBHelper helper;
    private SQLiteDatabase db;
    private Context context;
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private BufferedWriter writer = null;
    private SimpleDateFormat format;
    private String filename;
    private String folderName;
    private String savedPath;
    int count;

    ExportDataFromDatabase(Context context){
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        folderName = context.getResources().getString(R.string.file_name);

        //Append date to filename
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        if (Locale.getDefault().getLanguage().equals("ru") || Locale.getDefault().getLanguage().equals("uk")) {
            format = new SimpleDateFormat("dd-MM-yyyy");
        } else {format = new SimpleDateFormat("yyyy-MM-dd");}

        filename = context.getResources().getString(R.string.file_name) + "_" + format.format(date) + ".json";
        helper = new SQLiteDBHelper(context);
        db = helper.getReadableDatabase();
        jsonArray = new JSONArray();

        try {
            Cursor cursor = db.query(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.COLUMNS_NAMES, null, null, null, null, null);
            cursor.moveToFirst();
            count = cursor.getCount();
            if (count > 0) {
                do {
                    jsonObject = new JSONObject();
                    jsonObject.put(SQLiteDBHelper.KEY_NAME, cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_NAME)));
                    jsonObject.put(SQLiteDBHelper.KEY_PHONE, cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_PHONE)));
                    jsonObject.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE)));
                    jsonObject.put(SQLiteDBHelper.KEY_YEAR, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_YEAR)));
                    jsonObject.put(SQLiteDBHelper.KEY_MONTH, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_MONTH)));
                    jsonObject.put(SQLiteDBHelper.KEY_DAY, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_DAY)));
                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());

                //Check environment
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
                    path.mkdirs();
                    File file = new File(path, filename);
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write(jsonArray.toString());
                    savedPath = path.toString();
                    writer.close();
                } else {
                    writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(filename, context.MODE_PRIVATE)));
                    writer.write(jsonArray.toString());
                    savedPath = context.getFilesDir().toString() + "/" + filename;
                    writer.close();
                }
            }
        } catch (Exception e){
        }
        finally {
            db.close();
            if (writer != null){
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (count == 0) {
            Toast.makeText(context, context.getResources().getString(R.string.noDataToSave), Toast.LENGTH_SHORT).show();
        } else if (savedPath == null) {
            Toast.makeText(context, context.getResources().getString(R.string.noWritePermission), Toast.LENGTH_SHORT).show();
        } else if (savedPath != null && count > 0){
            Toast.makeText(context, context.getResources().getString(R.string.fileSaved) + " " + savedPath + "\n" + count + " " + context.getResources().getString(R.string.exported), Toast.LENGTH_LONG).show();
        }
    }
}
