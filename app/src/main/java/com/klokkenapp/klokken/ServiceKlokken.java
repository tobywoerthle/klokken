package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceKlokken extends IntentService {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);
    private GmailMessageProcessor gmailMessageProcessor;

    public ServiceKlokken() {
        super("ServiceKlokken");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Do Work Here

        GmailAuthTransfer authTransfer = (GmailAuthTransfer) intent.getSerializableExtra("authTransfer");
        GoogleAccountCredential accountCredential = authTransfer.getCredential();

        gmailMessageProcessor = new GmailMessageProcessor(accountCredential);
        gmailMessageProcessor.startMakeRequestTask();
        Map<String, GmailMessage> messages = gmailMessageProcessor.getMessages();

        //ThreadID
        //System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));

        Toast.makeText(this, "Handle Intent", Toast.LENGTH_SHORT).show();
        //HashMap<String, GmailMessage> sampleMap = new HashMap<>();
        //sampleMap.put("Aloha","Pineapple");

        GmailMessagesTransfer sampleMessageTransfer = new GmailMessagesTransfer(messages);
        publishResults(sampleMessageTransfer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind (Intent intent){
        return mainBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private void publishResults(GmailMessagesTransfer gmailMessages){

        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("gmailMessages", gmailMessages);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        sendBroadcast(intent);
    }
}