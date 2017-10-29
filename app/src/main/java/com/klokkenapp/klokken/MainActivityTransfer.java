package com.klokkenapp.klokken;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.Serializable;

public class MainActivityTransfer implements Serializable {

    private static MainActivity mainActivity;

    public MainActivityTransfer(MainActivity inMainActivity) {
        mainActivity = inMainActivity;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity inMainActivity) {
        mainActivity = inMainActivity;
    }

}
