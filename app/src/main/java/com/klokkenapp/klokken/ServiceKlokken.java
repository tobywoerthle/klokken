package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.klokkenapp.klokken.GmailMessageProcessor.*;

public class ServiceKlokken extends IntentService implements Serializable {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);
    private GmailMessageProcessor gmailMessageProcessor;
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS};
    private String accountName;
    private MainActivity mainActivity;

    public ServiceKlokken() {super("ServiceKlokken");}

    @Override
    protected void onHandleIntent(Intent intent) {

        accountName = (String) intent.getSerializableExtra("accountName");

        MainActivityTransfer mainActivityTransfer = (MainActivityTransfer) intent.getSerializableExtra("mainActivityTransfer");
        mainActivity = mainActivityTransfer.getMainActivity();

        GoogleAccountCredential accountCredential = null;

        if(accountName == null){
            //Received Intent from MainActivity
            accountCredential = mainActivity.mCredential;
        }
        else{
            //Received Intent from Boot Receiver
            accountCredential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            accountCredential.setSelectedAccountName(accountName);
        }

        Log.d("ServiceKlokken", "accountName = " + accountName);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //client.connect();

        //Do Work Here (non-main Thread)

        Log.d("ServiceKlokken", "mainActivity: " + mainActivity);

        gmailMessageProcessor = new GmailMessageProcessor(accountCredential, this, mainActivity);
        gmailMessageProcessor.startMakeRequestTask();

    }

    public void gmailMessagesPostAsyncTask(){
        Map<String, GmailMessage> messages = gmailMessageProcessor.getMessages();

        System.out.println("ASYNC ENTRY SET");
        System.out.println(messages.entrySet());

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
        Toast.makeText(this, "Mail check complete", Toast.LENGTH_SHORT).show();

        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("gmailMessages", gmailMessages);

        HashMap<String, GmailMessage> messageMap = (HashMap<String, GmailMessage>) gmailMessages.getMessageMap();

        if(messageMap != null){
            GmailNotification gmailNotification = new GmailNotification(mainActivity,this, messageMap);
            gmailNotification.createNotification();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        sendBroadcast(intent);

    }
}