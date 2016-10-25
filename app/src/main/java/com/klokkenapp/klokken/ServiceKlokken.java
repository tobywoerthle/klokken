package com.klokkenapp.klokken;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class ServiceKlokken extends IntentService {

    private IBinder mainBinder = new ServiceKlokkenClientCommunication(this);

    public ServiceKlokken() {
        super("ServiceKlokken");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Do Work Here
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



}