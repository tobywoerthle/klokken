package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.klokkenapp.klokken.GmailMessageProcessor.*;

public class ServiceKlokken extends IntentService {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);
    private GmailMessageProcessor gmailMessageProcessor;

    public ServiceKlokken() {
        super("ServiceKlokken");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Do Work Here (non-main Thread)

        GmailAuthTransfer authTransfer = (GmailAuthTransfer) intent.getSerializableExtra("authTransfer");
        GoogleAccountCredential accountCredential = authTransfer.getCredential();

        MainActivityTransfer mainActivityTransfer = (MainActivityTransfer) intent.getSerializableExtra("mainActivityTransfer");
        MainActivity mainActivity = mainActivityTransfer.getMainActivity();


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

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        sendBroadcast(intent);
    }
}