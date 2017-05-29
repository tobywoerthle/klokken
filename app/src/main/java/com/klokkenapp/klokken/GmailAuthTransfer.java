package com.klokkenapp.klokken;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.Serializable;

public class GmailAuthTransfer implements Serializable {

    private static GoogleAccountCredential credential;

    public GmailAuthTransfer(GoogleAccountCredential inCredential) {
        credential = inCredential;
    }

    public static GoogleAccountCredential getCredential() {
        return credential;
    }

    public static void setCredential(GoogleAccountCredential credential) {
        GmailAuthTransfer.credential = credential;
    }

}
