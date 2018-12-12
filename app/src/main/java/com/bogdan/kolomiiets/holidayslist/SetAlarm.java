package com.bogdan.kolomiiets.holidayslist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import java.util.Calendar;

public class SetAlarm {
    private Intent mIntent;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    public void onAlarm(Context context, boolean alarmOn, int hour, int minute) {
        try {
            mIntent = new Intent(context, WhoCelebratingNotification.class);
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mPendingIntent = PendingIntent.getService(context, RequestCodes.PENDING_INTENT_REQUEST_CODE, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (!alarmOn) {
                if (mAlarmManager != null) {
                    mAlarmManager.cancel(mPendingIntent);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                mAlarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPendingIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
