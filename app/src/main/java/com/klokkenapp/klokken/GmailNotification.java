package com.klokkenapp.klokken;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.AudioManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.util.TypedValue;

import java.util.HashMap;

public class GmailNotification {

    private static HashMap<String, GmailMessage> messageMap;
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
    private static ServiceKlokken serviceKlokken;
    private static AlertAudio alertAudio;
    private static MainActivity mainActivity;
    private static Intent resultIntent;
    private static boolean mainActivityInitiated;

    public GmailNotification(MainActivity inMainActivity, ServiceKlokken inServiceKlokken, final HashMap<String, GmailMessage> inMessageMap, Boolean inMainActivityInitiated) {
        serviceKlokken = inServiceKlokken;
        messageMap = inMessageMap;
        mainActivity = inMainActivity;
        mainActivityInitiated = inMainActivityInitiated;
    }

    public void createNotification(){

        inboxStyle.setBigContentTitle("New Klokken Alerts:");

        String messageSubject = "";
        String messageFrom = "";

        for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()) {
            messageSubject = entry.getValue().getMessageSubject();
            messageFrom = entry.getValue().getMessageFrom();
            inboxStyle.addLine(messageSubject + " - " + parseFrom(messageFrom));
        }

        resultIntent = new Intent(serviceKlokken,  MainActivity.class);

        if(messageMap.size() != 0){

            Context context = serviceKlokken;
            AudioManager audio = (AudioManager) serviceKlokken.getSystemService(Context.AUDIO_SERVICE);

            alertAudio = new AlertAudio(context);
            resultIntent.putExtra("alertAudio", alertAudio);

            switch(audio.getRingerMode() ){
                case AudioManager.RINGER_MODE_NORMAL:
                    alertAudio.ringPhoneAlert();
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    alertAudio.vibratePhoneAlert();
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
            }

            if(mainActivity != null){
                mainActivity.setAlertAudio(alertAudio);
            }
        }

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        serviceKlokken,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Log.d("GmailNotification", "mainActivityInitiated: " + String.valueOf(mainActivityInitiated));

        if((messageMap.size() > 0)){

            Log.d("GmailNotification", "Case 1");

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(mainActivity, mainActivity.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity, 0, intent, 0);

            String alertsSumamryText;
            if(messageMap.size() == 1){
                alertsSumamryText = messageMap.size() +" alert";
            }
            else{
                alertsSumamryText = messageMap.size() +" alerts";
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mainActivity, mainActivity.getChannelID())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("New Klokken Alerts")
                    .setContentText(alertsSumamryText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setColorized(true)
                    .setColor(16748288);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);
            notificationManager.notify(1, builder.build());



            /* NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(serviceKlokken)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Klokken Alerts")
                            .setContentText(messageMap.size() +" alerts")
                            .setGroup("GmailNotification")
                            .setGroupSummary(true)
                            .setAutoCancel(true)
                            .setStyle(inboxStyle);


            //mBuilder.setContentIntent(resultPendingIntent);
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) serviceKlokken.getSystemService(serviceKlokken.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(1, mBuilder.build());
            */

        }
        else if(messageMap.size() != 0 && (mainActivityInitiated == false)){
            Log.d("GmailNotification", "Case 2");

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(serviceKlokken)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Klokken Alerts:")
                            .setAutoCancel(true)
                            .setContentText(messageSubject + " - " + parseFrom(messageFrom));
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr = (NotificationManager) serviceKlokken.getSystemService(serviceKlokken.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, mBuilder.build());
        }
        else {
            Log.d("GmailNotification", "Case 3");
        }
    }

    private String parseFrom(String inMessageFrom) {
        int index = inMessageFrom.indexOf("<");
        Log.d("GmailNotification", Integer.toString(index));
        if(index != -1 && inMessageFrom.length() != 0) {
            return inMessageFrom.substring(0, index-1);
        }
        else{
            return inMessageFrom;
        }
    }
}