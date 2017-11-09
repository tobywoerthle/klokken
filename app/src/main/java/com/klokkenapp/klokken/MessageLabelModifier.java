package com.klokkenapp.klokken;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.ModifyThreadRequest;
import com.google.api.services.gmail.model.Thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MessageLabelModifier extends AsyncTask<Void, Void, Void> {

    private com.google.api.services.gmail.Gmail mService = null;
    private static GoogleAccountCredential accountCredential = null;
    public Exception mLastError = null;
    private String threadID;

    public MessageLabelModifier(GoogleAccountCredential inAccountCredential, String inThreadID) {
        accountCredential = inAccountCredential;
        threadID = inThreadID;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, accountCredential)
                .setApplicationName("Klokken Message Checker and Ringer")
                .build();
        Log.d("MessageLabelModifer", "constructor");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("MessageLabelModifer", "doInBackground");
        try {
            return markMessageAsRead();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    public Void markMessageAsRead() throws IOException {

        List<String> labelsToAdd = new ArrayList<String>();
        List<String> labelsToRemove = new ArrayList<String>();
        labelsToRemove.add("UNREAD");

        ModifyThreadRequest mods = new ModifyThreadRequest().setAddLabelIds(labelsToAdd)
                .setRemoveLabelIds(labelsToRemove);

        try {
            Thread thread = mService.users().threads().modify("me", threadID, mods).execute();

        } catch (Exception e){
            Log.d("MessageLabelModifer", "Label Modify Error: " + threadID);
            Log.d("MessageLabelModifer", "Label Modify Error: " + e);
        }
        return null;
    }
}
