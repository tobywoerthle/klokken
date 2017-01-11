package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class ServiceKlokken extends IntentService {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);

    public ServiceKlokken() {
        super("ServiceKlokken");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Do Work Here
        Toast.makeText(this, "Handle Intent", Toast.LENGTH_SHORT).show();
        Map<String, String> sampleMap = new HashMap<>();
        sampleMap.put("Aloha","Pineapple");
        System.out.println("onHandleIntentCalled");

        GmailMessagesTransfer sampleMessageTransfer = new GmailMessagesTransfer(sampleMap);

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

    private void publishResults(GmailMessagesTransfer gmailMessageIDs){

        Log.d("sender", "Broadcasting message");
        System.out.println("publishResults");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("gmailMessages", gmailMessageIDs);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        sendBroadcast(intent);
    }



}