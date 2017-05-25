package com.klokkenapp.klokken;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;

/**
 * Created by Toby on 5/7/2017.
 */

class AlertAudio {

    private static Context mainActivityContext = null;

    public AlertAudio(Context inMainActivityContext) {
        mainActivityContext = inMainActivityContext;
    }

    public static Context getMainActivityContext() {
        return mainActivityContext;
    }

    public void vibratePhoneAlert() {
        Vibrator v = (Vibrator) mainActivityContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        if (v.hasVibrator()) {
            //v.vibrate(400);

            // Start without a delay
            // Each element then alternates between vibrate, sleep, vibrate, sleep...
            //long[] pattern = {0, 10, 10, 10, 10, 20, 10, 30, 10, 50, 10, 80, 10, 130, 10, 210, 10};
            long[] pattern = {0, 100, 10, 100, 10, 100, 10, 100, 300, 100, 300, 100};

            // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
            v.vibrate(pattern, -1);
        }
    }

    public void ringPhoneAlert() {
        //stop should be called by other method, once alert is acknowledged
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE );
        Ringtone r = RingtoneManager.getRingtone(mainActivityContext, notification);

        r.play();
        SystemClock.sleep(9000);
        r.stop();
    }
}