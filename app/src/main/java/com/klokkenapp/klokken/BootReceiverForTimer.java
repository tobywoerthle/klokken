package com.klokkenapp.klokken;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Toby on 5/28/2017.
 */

public final class BootReceiverForTimer extends BroadcastReceiver {

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    private static final int AlarmWakeUp = 1;
    private static String accountName;
    private static MainActivityTransfer mainActivityTransfer;

    @Override
    public void onReceive(Context context, Intent intent) {

        accountName = (String) intent.getSerializableExtra("accountName");
        mainActivityTransfer = (MainActivityTransfer) intent.getSerializableExtra("mainActivityTransfer");

        Log.d("BootReceiver", "accountName = " + accountName);

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

    public static void setAlarm(Context context) {

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceKlokken.class);
        intent.putExtra("accountName", accountName);
        intent.putExtra("mainActivityTransfer", mainActivityTransfer);

        alarmIntent = PendingIntent.getService(context, AlarmWakeUp, intent, 0);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivityTransfer.getMainActivity());
        String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "15");
        int freqInMilSec = Integer.parseInt(syncConnPref);
        freqInMilSec = freqInMilSec * 60000;

        Log.d("BootReceiverForTimer", "SyncFreq: " + syncConnPref);

        if(freqInMilSec > 0){
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + freqInMilSec,
                    freqInMilSec, alarmIntent);
            Toast.makeText(context, "Alarm set!", Toast.LENGTH_LONG).show();
            Log.d("BootReceiverForTimer", "Alarm set!");
        }
        else{
            cancelAlarm();
        }
    }

    private static void cancelAlarm() {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}