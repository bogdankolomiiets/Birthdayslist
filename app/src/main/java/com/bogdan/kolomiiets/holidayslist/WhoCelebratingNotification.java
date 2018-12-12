package com.bogdan.kolomiiets.holidayslist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;

public class WhoCelebratingNotification extends Service {
    private final String CHANNEL_ID = "BirthdayChannel";
    private final String CHANNEL_NAME = "BirthdayChannelName";
    private SQLiteDBHelper helper;
    private SQLiteDatabase database;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new SQLiteDBHelper(getApplicationContext());
        database = helper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int when = getApplicationContext().getSharedPreferences(SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(SettingsActivity.CHECKED_RADIO, 0);

        String strWhen = "";
        switch (when) {
            case 0:
                strWhen = getApplicationContext().getString(R.string.CelebratesToday);
                break;
            case 1:
                strWhen = getApplicationContext().getString(R.string.CelebratesTomorrow);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case 2:
                strWhen = getApplicationContext().getString(R.string.CelebratesAfterTomorrow);
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                break;
        }

        String selection = SQLiteDBHelper.KEY_DAY + " = ? and " + SQLiteDBHelper.KEY_MONTH + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), String.valueOf(calendar.get(Calendar.MONTH) + 1)};
        try {
            Cursor c = database.query(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.COLUMNS_NAMES, selection, selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    Intent detailIntent = new Intent(getApplicationContext(), NotificationDetail.class);
                    detailIntent.putExtra(SQLiteDBHelper.KEY_NAME, c.getString(c.getColumnIndex(SQLiteDBHelper.KEY_NAME)));
                    detailIntent.putExtra(SQLiteDBHelper.KEY_PHONE, c.getString(c.getColumnIndex(SQLiteDBHelper.KEY_PHONE)));
                    detailIntent.putExtra(SQLiteDBHelper.KEY_CELEBRATION_TYPE, c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE)));
                    detailIntent.putExtra(SQLiteDBHelper.KEY_YEAR, c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_YEAR)));
                    detailIntent.putExtra(SQLiteDBHelper.KEY_MONTH, c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_MONTH)));
                    detailIntent.putExtra(SQLiteDBHelper.KEY_DAY, c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_DAY)));

                    pendingIntent = PendingIntent.getActivity(getApplicationContext(), c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_ID)), detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker(strWhen)
                            .setContentTitle(strWhen)
                            .setContentText(c.getString(c.getColumnIndex(SQLiteDBHelper.KEY_NAME)))
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                            .setDefaults(Notification.DEFAULT_VIBRATE);

                    if (c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE)) != 3) {
                        builder.setContentIntent(pendingIntent);
                    }

                    NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                        builder.setChannelId(CHANNEL_ID);
                        manager.createNotificationChannel(channel);
                    }
                    manager.notify(c.getInt(c.getColumnIndex(SQLiteDBHelper.KEY_ID)), builder.build());
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            database.close();
            stopSelf();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
