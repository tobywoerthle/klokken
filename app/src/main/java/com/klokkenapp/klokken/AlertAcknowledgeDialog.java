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

    private final AlertAudio alertAudio;
    private final GoogleAccountCredential mCredential;
    private final MailMessage mailMessage;

    //Ringtone constructor
    public AlertAcknowledgeDialog(
            AlertAudio inAlertAudio,
            GoogleAccountCredential inMCredential,
            MailMessage inMailMessage) {
        alertAudio = inAlertAudio;
        mCredential = inMCredential;
        mailMessage = inMailMessage;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.AlertAcknowledgeAlertsMessage)
                        + ": " + mailMessage.getMessageSubject())
                .setPositiveButton(R.string.AlertPositive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(alertAudio != null){
                            alertAudio.stopRingAudio();
                        }
                        if (MailMessage.PROVIDER_IMAP.equals(mailMessage.getProvider())) {
                            new ImapMessageAcknowledger(
                                    getActivity().getApplicationContext(), mailMessage).execute();
                        } else {
                            new MessageLabelModifier(mCredential, mailMessage.getThreadID()).execute();
                        }
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