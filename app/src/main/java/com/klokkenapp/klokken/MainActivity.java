package com.klokkenapp.klokken;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.gmail.GmailScopes;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends FragmentActivity
        implements EasyPermissions.PermissionCallbacks {

    public GoogleAccountCredential mCredential;

    public static final String ClassName = "MainActivity";

    private static Intent mainIntentForService;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS};
    private static Bundle savedInstanceState;
    private static HashMap<String, AlertListFragment> allDisplayedMessages = new HashMap<String, AlertListFragment>();
    private GoogleApiClient client;
    private ServiceConnection serviceConnection;
    private static AlertAudio alertAudio;

    /**
     * Create the main activity.
     *
     * @param inSavedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle inSavedInstanceState) {

        savedInstanceState = inSavedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        Intent intent = getIntent();
        alertAudio = (AlertAudio) intent.getSerializableExtra("alertAudio");
        if(alertAudio != null){
            //Received Intent from Notification where Main Activity was closed
            alertAudio.stopRingAudio();
        }

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        makeBroadcastReceiver();
        getResultsFromApi();
        enableBootReceiver();
    }

    public void handleFragments(Boolean add, GmailMessage inGmailMessage) {

        System.out.println("handleFragmentsCalled--------------------------------");

        // https://developer.android.com/training/basics/fragments/fragment-ui.html
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null || add) {

            String curMessageID = inGmailMessage.getMessageID();

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null && !add) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            AlertListFragment newFragment = new AlertListFragment();
            //Set the Gmail message that contains the information for the fragment
            newFragment.setGmailMessage(inGmailMessage);

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            newFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, newFragment).commit();

            allDisplayedMessages.put(curMessageID, newFragment);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /*
    public void cancelledUserRecoverableAuthIOException(UserRecoverableAuthIOException inMLastError) {
        startActivityForResult(
                inMLastError.getIntent(),
                MainActivity.REQUEST_AUTHORIZATION);
        System.out.println("The following error occurred: UserRecoverableAuthIOException");
    }
    */

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static final int AlarmWakeUp = 1;

    /* --------------- Boot Receiver* ---------------*/

    //TODO: enable/disable boot receiver via settings

    private void enableBootReceiver() {
        //Ensure boot receiver stays enabled, even after reboot
        Context context = getApplicationContext();
        ComponentName receiverForTimer = new ComponentName(context, BootReceiverForTimer.class);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiverForTimer, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent("SetAlarm");

        MainActivityTransfer mainActivityTransfer = new MainActivityTransfer(this);

        intent.putExtra("accountName", mCredential.getSelectedAccountName());
        intent.putExtra("mainActivityTransfer", mainActivityTransfer);

        Log.d(ClassName, "enableBootReceiver - accountName: "+ mCredential.getSelectedAccountName());

        sendBroadcast(intent);
    }

    private void disableBootReceiver() {
        //Disables the boot receiver, even after reboot
        Context context = getApplicationContext();
        ComponentName receiverForTimer = new ComponentName(context, BootReceiverForTimer.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiverForTimer, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }


    /**
     * Calls {@link #getResultsFromApi()}
     *
     * @param v the view that called the function
     */

    public void buttonManualGmailCheckClick(View v)
    {
        getResultsFromApi();
    }

    public void settingsClick(View v)
    {
        Intent myIntent = new Intent(this, SettingsActivity.class);
        startActivity(myIntent);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");

        allDisplayedMessages = new HashMap<String, AlertListFragment>();
    }

    public void filterClick(View v)
    {

    }

    private void makeBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intentIn) {
            // Get extra data included in the Intent
            GmailMessagesTransfer inGmailMessageTransfer = (GmailMessagesTransfer) intentIn.getSerializableExtra("gmailMessages");

            HashMap<String, GmailMessage> messageMap = (HashMap<String, GmailMessage>) inGmailMessageTransfer.getMessageMap();

            if(messageMap != null){

                System.out.println("entrySet:");
                System.out.println(messageMap.entrySet());

                int count = 0;

                removeAllFragments();

                for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()){
                    count++;
                    Log.d(ClassName, "BroadcastReceiver.MessageSubject "+count);
                    GmailMessage curGmailMessage = entry.getValue();
                    Log.d(ClassName, "BroadcastReceiver.MessageSubject "+curGmailMessage.getMessageSubject());
                    Log.d(ClassName, "BroadcastReceiver.MessageSubject");

                    handleFragments(true, curGmailMessage);
                }

                if (count != 0){
                    //Play audio and display alert in different thread
                    alertInit(messageMap);
                }

                Log.d(ClassName, "BroadcastReceiver.receivedMessages");



            }
            else{
                Log.d(ClassName, "BroadcastReceiver.messageMap == null");
            }

        }
    };

    private void removeAllFragments() {

        for (HashMap.Entry<String, AlertListFragment> curID : allDisplayedMessages.entrySet()){
            AlertListFragment frag = curID.getValue();
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .remove(frag).commit();
        }

        allDisplayedMessages = new HashMap<String, AlertListFragment>();
    }

    private void alertInit(final HashMap<String, GmailMessage> messageMap) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Context context = getApplicationContext();
                //AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                //AlertAudio.NestedStaticClass alertAudio = new AlertAudio.NestedStaticClass(context);

                showAlert(messageMap);

            }
        });
    }

    //Need to be Gloabl because used in an inner class (AlertAcknowledgeDialog)
    private String curGmailMessageThreadID;
    private String curGmailMessageSubject;

    private void showAlert(final HashMap<String, GmailMessage> messageMap){

        for (HashMap.Entry<String, GmailMessage> entry : messageMap.entrySet()) {
            curGmailMessageThreadID = entry.getValue().getThreadID();
            curGmailMessageSubject = entry.getValue().getMessageSubject();

            AlertAcknowledgeDialog newFragment = new AlertAcknowledgeDialog(alertAudio, mCredential, curGmailMessageThreadID, curGmailMessageSubject);
            newFragment.show(getFragmentManager(), "AlertAcknowledgeDialog");
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
        unbindService(serviceConnection);
        Toast.makeText(this, "Service Un-Binded", Toast.LENGTH_LONG).show();
    }

    //Make New Service
    private void startServiceForMailCheck() {
        mainIntentForService = new Intent(this, ServiceKlokken.class);
        MainActivityTransfer mainActivityTransfer = new MainActivityTransfer(this);
        //Transfer to ensure main activity is used for OAUTH 2.0 (UserRecoverableAuthIOException)
        mainIntentForService.putExtra("mainActivityTransfer", mainActivityTransfer);

        Log.d(ClassName, "startServiceForMailCheck - accountName: "+ mCredential.getSelectedAccountName());

        startService(mainIntentForService);

        //PendingIntent pendingIntendt = new PendingIntent();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(mainIntentForService, serviceConnection, 0);
    }

    private void stopServiceForMailCheck() {
        stopService(mainIntentForService);
    }

    private void checkService() {

    }


    //" Other application components can then call bindService() to retrieve the interface and begin calling methods on the service"


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            //Call service manually
            startServiceForMailCheck();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void setAlertAudio(AlertAudio alertAudio) {
        this.alertAudio = alertAudio;
    }

}