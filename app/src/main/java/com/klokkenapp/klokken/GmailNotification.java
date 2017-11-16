package com.klokkenapp.klokken;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import java.util.HashMap;

public class GmailNotification {

    private static MainActivity mainActivityContext;
    private static HashMap<String, GmailMessage> messageMap;
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

    public void createNotification(){

        inboxStyle.setBigContentTitle("New Klokken Alerts:");

        for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()) {
            String messageSubject = entry.getValue().getMessageSubject();
            String messageFrom = entry.getValue().getMessageFrom();
            setLinesNotification(messageSubject, messageFrom);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mainActivityContext)
                        .setSmallIcon(R.drawable.klokken)
                        .setGroup("GmailNotification")
                        .setGroupSummary(true)
                        .setStyle(inboxStyle)
                ;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) mainActivityContext.getSystemService(mainActivityContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, mBuilder.build());
    }

    private void setLinesNotification(String messageSubject, String messageFrom ) {
        inboxStyle.addLine(messageSubject + " - " + parseFrom(messageFrom));
    }

    public GmailNotification(MainActivity inMainActivityContext, final HashMap<String, GmailMessage> inMessageMap) {
        mainActivityContext = inMainActivityContext;
        messageMap = inMessageMap;
    }

    private String parseFrom(String inMessageFrom) {
        int index = inMessageFrom.indexOf("<");
        if(index != 0 && inMessageFrom.length() != 0) {
            return inMessageFrom.substring(0, index-1);
        }
        else{
            return "";
        }
    }
}