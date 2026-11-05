package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServiceKlokken extends IntentService implements Serializable {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);
    private static GmailMessageProcessor gmailMessageProcessor;
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_MODIFY
    };
    private static String accountName;
    private static MainActivity mainActivity;
    private static MessageNotification messageNotification;
    private static boolean mainActivityInitiated;

    public ServiceKlokken() {super("ServiceKlokken");}

    @Override
    protected void onHandleIntent(Intent intent) {

        accountName = (String) intent.getSerializableExtra("accountName");

        MainActivityTransfer mainActivityTransfer = (MainActivityTransfer) intent.getSerializableExtra("mainActivityTransfer");
        mainActivity = mainActivityTransfer == null ? null : mainActivityTransfer.getMainActivity();
        mainActivityInitiated = mainActivity != null;

        String provider = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_PREF_MAIL_PROVIDER, MailMessage.PROVIDER_GMAIL);
        if (MailMessage.PROVIDER_IMAP.equals(provider)) {
            ImapAccount imapAccount = ImapAccountStore.load(this);
            if (imapAccount == null) {
                if (mainActivityInitiated) {
                    Toast.makeText(this, R.string.imap_not_configured, Toast.LENGTH_LONG).show();
                }
                return;
            }
            try {
                Map<String, MailMessage> messages =
                        new ImapMessageProcessor(imapAccount).getUnreadMessages();
                publishResults(new MailMessagesTransfer(messages));
            } catch (Exception error) {
                Log.w("ServiceKlokken", "IMAP check failed; verify server and account settings.");
                if (mainActivityInitiated) {
                    Toast.makeText(this, R.string.imap_connection_failed, Toast.LENGTH_LONG).show();
                }
            }
            return;
        }

        GoogleAccountCredential accountCredential;
        if((accountName != null) || (mainActivity == null)){
            //Received Intent from Boot Receiver
            accountCredential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            accountCredential.setSelectedAccountName(accountName);
            mainActivityInitiated = false;
        }
        else {
            //Received Intent from MainActivity
            accountCredential = mainActivity.mCredential;
            mainActivityInitiated = true;
        }

        gmailMessageProcessor = new GmailMessageProcessor(accountCredential, this, mainActivity);
        gmailMessageProcessor.startMakeRequestTask();
    }

    public void gmailMessagesPostAsyncTask(){
        Map<String, MailMessage> messages = gmailMessageProcessor.getMessages();
        MailMessagesTransfer messageTransfer = new MailMessagesTransfer(messages);
        publishResults(messageTransfer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind (Intent intent){
        return mainBinder;
    }

    @Override
    public void onDestroy() {
    }

    private void publishResults(MailMessagesTransfer mailMessages){

        if(mainActivityInitiated){
            Toast.makeText(this, "Mail check complete", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra("mailMessages", mailMessages);

        HashMap<String, MailMessage> messageMap =
                (HashMap<String, MailMessage>) mailMessages.getMessageMap();

        if (messageMap != null && !messageMap.isEmpty()) {
            messageNotification = new MessageNotification(this, mainActivity, messageMap);
            messageNotification.createNotification();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}