package com.klokkenapp.klokken;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;

public class GmailNotification {

    private static HashMap<String, GmailMessage> messageMap;
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
    private static ServiceKlokken serviceKlokken;
    private static MainActivity mainActivity;

    public void createNotification(){

        Intent resultIntent = new Intent(serviceKlokken,  MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mainActivity.getApplicationContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        inboxStyle.setBigContentTitle("New Klokken Alerts:");

        String messageSubject = "";
        String messageFrom = "";

        for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()) {
            messageSubject = entry.getValue().getMessageSubject();
            messageFrom = entry.getValue().getMessageFrom();
            inboxStyle.addLine(messageSubject + " - " + parseFrom(messageFrom));
        }

        if(messageMap.size() > 1){

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(serviceKlokken)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Klokken Alerts")
                            .setContentText(messageMap.size() +" alerts")
                            .setGroup("GmailNotification")
                            .setGroupSummary(true)
                            .setStyle(inboxStyle);

            mBuilder.setContentIntent(resultPendingIntent);
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) serviceKlokken.getSystemService(serviceKlokken.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(1, mBuilder.build());
        }
        else {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(serviceKlokken)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("New Klokken Alerts:")
                            .setContentText(messageSubject + " - " + parseFrom(messageFrom));
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr = (NotificationManager) serviceKlokken.getSystemService(serviceKlokken.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }

    public GmailNotification(MainActivity inMainActivity, ServiceKlokken inServiceKlokken, final HashMap<String, GmailMessage> inMessageMap) {
        mainActivity = inMainActivity;
        serviceKlokken = inServiceKlokken;
        messageMap = inMessageMap;
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