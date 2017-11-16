package com.klokkenapp.klokken;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Toby on 7/13/2017.
 */

@SuppressLint("ValidFragment")
public class AlertAcknowledgeDialog extends DialogFragment {
    //TO DO: fix Constructor to not use arguments

    private static AlertAudio alertAudio = null;
    private static GoogleAccountCredential mCredential = null;
    private String curGmailMessageThreadID = null;
    private String curGmailMessageSubject = null;

    //Ringtone constructor
    public AlertAcknowledgeDialog(AlertAudio inAlertAudio, GoogleAccountCredential inMCredential, String inCurGmailMessageThreadID, String inCurGmailMessageSubject) {
        alertAudio = inAlertAudio;
        mCredential = inMCredential;
        curGmailMessageThreadID = inCurGmailMessageThreadID;
        curGmailMessageSubject = inCurGmailMessageSubject;
    }

    //Vibrate or Silent constructor
    public AlertAcknowledgeDialog(GoogleAccountCredential inMCredential, String inCurGmailMessageThreadID, String inCurGmailMessageSubject) {
        mCredential = inMCredential;
        curGmailMessageThreadID = inCurGmailMessageThreadID;
        curGmailMessageSubject = inCurGmailMessageSubject;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.AlertAcknowledgeAlertsMessage) + ": " + curGmailMessageSubject)
                .setPositiveButton(R.string.AlertPositive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(alertAudio != null){
                            alertAudio.stopRingAudio();
                        }

                        new MessageLabelModifier(mCredential, curGmailMessageThreadID).execute();
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

        return builder.create();
    }

    @Override
    //If outside of dialog is pressed, cancel audio if it exists
    public void onCancel(DialogInterface dialog) {
        if(alertAudio != null){
            alertAudio.stopRingAudio();
        }
    }
}