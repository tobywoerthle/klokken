package com.klokkenapp.klokken;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

/**
 * Created by Toby on 7/13/2017.
 */

public class AlertAcknowledgeDialog extends DialogFragment {

    private static AlertAudio alertAudio = null;

    //Ringtone constructor
    public AlertAcknowledgeDialog(AlertAudio inAlertAudio) {
        alertAudio = inAlertAudio;
    }
    //TO DO: fix Constructor to not use arguments


    //Vibrate or Silent constructor
    public AlertAcknowledgeDialog() {
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
                            //// TODO: Do not alert again for this message
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
        // Create the AlertDialog object and return it
        return builder.create();
    }
}