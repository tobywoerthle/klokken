package com.klokkenapp.klokken;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

public class MessageNotification {

    private final Context context;
    private final MainActivity mainActivity;
    private final Map<String, MailMessage> messages;

    public MessageNotification(
            Context context,
            MainActivity mainActivity,
            Map<String, MailMessage> messages) {
        this.context = context.getApplicationContext();
        this.mainActivity = mainActivity;
        this.messages = messages;
    }

    public void createNotification() {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        MainActivity.ensureNotificationChannel(context);
        AlertAudio alertAudio = new AlertAudio(context);
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            switch (audio.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    alertAudio.ringPhoneAlert();
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    alertAudio.vibratePhoneAlert();
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
            }
        }

        if (mainActivity != null) {
            mainActivity.setAlertAudio(alertAudio);
        }

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("alertAudio", alertAudio);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(context.getString(R.string.notification_title));
        for (MailMessage message : messages.values()) {
            String subject = message.getMessageSubject();
            String from = parseFrom(message.getMessageFrom());
            inboxStyle.addLine(subject + (from.isEmpty() ? "" : " — " + from));
        }

        String summary = context.getResources().getQuantityString(
                R.plurals.notification_alert_count, messages.size(), messages.size());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(summary)
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColorized(true)
                .setColor(16748288);

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }

    private String parseFrom(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int addressStart = value.indexOf('<');
        if (addressStart > 0) {
            return value.substring(0, addressStart).trim();
        }
        return value;
    }
}
