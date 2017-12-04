package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
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
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS};
    private static String accountName;
    private static MainActivity mainActivity;
    private static GmailNotification gmailNotification;
    private static boolean mainActivityInitiated;

    public ServiceKlokken() {super("ServiceKlokken");}

    @Override
    protected void onHandleIntent(Intent intent) {

        accountName = (String) intent.getSerializableExtra("accountName");

        MainActivityTransfer mainActivityTransfer = (MainActivityTransfer) intent.getSerializableExtra("mainActivityTransfer");
        mainActivity = mainActivityTransfer.getMainActivity();

        GoogleAccountCredential accountCredential;

        if((accountName != null) && (mainActivity == null)){
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
        Map<String, GmailMessage> messages = gmailMessageProcessor.getMessages();
        GmailMessagesTransfer messageTransfer = new GmailMessagesTransfer(messages);
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

    private void publishResults(GmailMessagesTransfer gmailMessages){

        if(mainActivityInitiated){
            Toast.makeText(this, "Mail check complete", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra("gmailMessages", gmailMessages);

        HashMap<String, GmailMessage> messageMap = (HashMap<String, GmailMessage>) gmailMessages.getMessageMap();

        if(messageMap != null){
            gmailNotification = new GmailNotification(mainActivity,this, messageMap, mainActivityInitiated);
            gmailNotification.createNotification();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        sendBroadcast(intent);
    }
}