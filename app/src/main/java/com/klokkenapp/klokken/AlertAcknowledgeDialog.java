package com.klokkenapp.klokken;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.HashMap;

/**
 * Created by Toby on 7/13/2017.
 */

@SuppressLint("ValidFragment")
public class AlertAcknowledgeDialog extends DialogFragment {
    //TO DO: fix Constructor to not use arguments

    private static AlertAudio alertAudio = null;
    private static GoogleAccountCredential mCredential = null;
    private static HashMap<String, GmailMessage> messageMap = null;

    //Ringtone constructor
    public AlertAcknowledgeDialog(AlertAudio inAlertAudio, GoogleAccountCredential inMCredential, HashMap<String, GmailMessage> inMessageMap) {
        alertAudio = inAlertAudio;
        mCredential = inMCredential;
        messageMap = inMessageMap;
    }



    //Vibrate or Silent constructor
    public AlertAcknowledgeDialog(GoogleAccountCredential inMCredential, HashMap<String, GmailMessage> inMessageMap) {
        mCredential = inMCredential;
        messageMap = inMessageMap;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.AlertAcknowledgeAlertsMessage)
                .setPositiveButton(R.string.AlertPositive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(alertAudio != null){
                            alertAudio.stopRingAudio();
                        }

                        //TO DO: Acknowledge Multiple Messages/Threads
                        String curGmailMessageThreadID = null;
                        for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()) {
                            curGmailMessageThreadID = entry.getValue().getThreadID();
                        }

                        new MessageLabelModifier(mCredential, curGmailMessageThreadID).execute();
                        Log.d("AlertAcknowledgeDialog", "post-execute_+++");

                        //TODO: Do not alert again for this message
                    }
                })
                .setNegativeButton(R.string.AlertNegative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        if(alertAudio != null){
                            alertAudio.stopRingAudio();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}