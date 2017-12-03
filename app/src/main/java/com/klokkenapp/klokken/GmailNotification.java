package com.klokkenapp.klokken;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;

public class GmailNotification {

    private static HashMap<String, GmailMessage> messageMap;
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
    private static ServiceKlokken serviceKlokken;
    private static AlertAudio alertAudio;
    private static MainActivity mainActivity;
    private static Intent resultIntent;

    public GmailNotification(MainActivity inMainActivity, ServiceKlokken inServiceKlokken, final HashMap<String, GmailMessage> inMessageMap) {
        serviceKlokken = inServiceKlokken;
        messageMap = inMessageMap;
        mainActivity = inMainActivity;
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

        if(messageMap.size() > 1){

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(serviceKlokken)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Klokken Alerts")
                            .setContentText(messageMap.size() +" alerts")
                            .setGroup("GmailNotification")
                            .setGroupSummary(true)
                            .setAutoCancel(true)
                            .setStyle(inboxStyle);

            mBuilder.setContentIntent(resultPendingIntent);
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) serviceKlokken.getSystemService(serviceKlokken.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(1, mBuilder.build());
        }
        else if(messageMap.size() != 0){
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