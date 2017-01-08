package com.klokkenapp.klokken;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.model.MessagePartHeader;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;
import static com.klokkenapp.klokken.MainActivity.REQUEST_ACCOUNT_PICKER;
import static com.klokkenapp.klokken.MainActivity.REQUEST_AUTHORIZATION;
import static com.klokkenapp.klokken.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.klokkenapp.klokken.MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS;


public class GmailMessageProcessor {

    public static final String ClassName = "GmailMessageProcessor";

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static Context mainActivityContext = null;

    public static Context getMainActivityContext() {
        return mainActivityContext;
    }

    public GmailMessageProcessor(Context mainActivityContextIn) {
        //Contructor
        mainActivityContext  = mainActivityContextIn;
    }

    public void startMakeRequestTask(GoogleAccountCredential mCredentialIn) {
        new MakeRequestTaskGmail(mCredentialIn).execute();
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTaskGmail extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTaskGmail(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Klokken Message Checker and Ringer")
                    .build();
        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get the labels in the user's account.
            String user = "me";
            List<String> labels = new ArrayList<String>();
            List<String> messages = new ArrayList<String>();
            ListLabelsResponse listResponse =
                    mService.users().labels().list(user).execute();
            ListMessagesResponse listResponseMail =
                    mService.users().messages().list(user).execute();
            for (Label label : listResponse.getLabels()) {
                labels.add(label.getName());
            }

            //Log.d(ClassName, "getDataFromApi.listResponse");
            //System.out.println(listResponse);
            //Log.d(ClassName, "getDataFromApi.listResponseMail");
            //System.out.println(listResponseMail);

            // TODO: Ability to set mailbox
            // ex. in:inbox OR in:Klokken

            ListMessagesResponse queryMessageReturn = mService.users().messages().list(user).setQ("in:inbox is:unread").execute();


            List<Message> messagesRaw = queryMessageReturn.getMessages();

            if(messagesRaw != null && messagesRaw.isEmpty() == false){
                for (Message message : messagesRaw) {
                    printMessages(mService, user, message.getId());
                    messages.add(message.toPrettyString());
                }
            }

            return messages;
        }


        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            //mProgress.show();
        }

        public Message printMessages (com.google.api.services.gmail.Gmail service, String userId, String messageId)
                throws IOException {
            Message message = service.users().messages().get(userId, messageId).execute();

            Log.d(ClassName, "printMessages.message");

            List<MessagePartHeader> headers = message.getPayload().getHeaders();

            //System.out.println("Message snippet: " + message.getSnippet());

            for (MessagePartHeader header: headers) {
                if(header.getName().equals("From")){
                    System.out.println("Message From: " + header.getValue());
                }
                else if(header.getName().equals("To")){
                    System.out.println("Message To: " + header.getValue());
                }
                else if(header.getName().equals("Date")){
                    System.out.println("Message Date: " + header.getValue());
                }
                else if(header.getName().equals("Received")){
                    System.out.println("Message Received: " + header.getValue());
                }
                else if(header.getName().equals("Subject")){
                    System.out.println("Message Subject: " + header.getValue());
                }
                else {
                    //System.out.println("Other: " + header.getName() + " | " + header.getValue());
                }

            }

            //Get full message body
            //System.out.println("Message Body HTML: " + StringUtils.newStringUtf8(Base64.decodeBase64( message.getPayload().getBody().getData())));


            /*
            try {
                getMimeMessage(mService, userId, messageId);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            */

            return message;
        }

        public MimeMessage getMimeMessage(com.google.api.services.gmail.Gmail service, String userId, String messageId)
                throws IOException, MessagingException {
            Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();

            Base64 base64Url = new Base64(true);
            byte[] emailBytes = base64Url.decodeBase64(message.getRaw());

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

            //System.out.println("1"+email.getSubject());
            //System.out.println("2"+email.getFrom());
            //System.out.println("3"+email.getReceivedDate());

            return email;
        }



        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                System.out.println("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Gmail API:");
                System.out.println(TextUtils.join("\n", output));
            }

            //TODO: Notification broadcast
            //TODO: Dialog to dismiss/snooze
            //TODO: Add custom ringtone
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone r = RingtoneManager.getRingtone(mainActivityContext, notification);
            r.play();
            r.stop();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    //showGooglePlayServicesAvailabilityErrorDialog(
                    //        ((GooglePlayServicesAvailabilityIOException) mLastError)
                          //          .getConnectionStatusCode());
                    System.out.println("The following error occurred: GooglePlayServicesAvailabilityIOException");
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    //startActivityForResult(
                      //      ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        //    REQUEST_AUTHORIZATION);
                    System.out.println("The following error occurred: UserRecoverableAuthIOException");
                } else {
                    System.out.println("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                System.out.println("Request cancelled.");
            }
        }
    }
}
