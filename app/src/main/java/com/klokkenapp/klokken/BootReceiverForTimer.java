package com.klokkenapp.klokken;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Toby on 5/28/2017.
 */

public final class BootReceiverForTimer extends BroadcastReceiver {

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    private static final int AlarmWakeUp = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarm(context);
        }
        else if (intent.getAction().equals("SetAlarm")) {
            Log.d("BootReceiverForTimer", "SetAlarmIntentReceived");
            if(alarmMgr == null){
                setAlarm(context);
            }
        }
    }

    //TODO: Figure out alarm

    public static void setAlarm(Context context) {

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceKlokken.class);
        alarmIntent = PendingIntent.getService(context, AlarmWakeUp, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 3000,
                3000, alarmIntent);

        Toast.makeText(context, "Alarm set!", Toast.LENGTH_LONG).show();
        Log.d("BootReceiverForTimer", "Alarm set!");

        //cancelAlarm();
    }

    private static void cancelAlarm() {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

}