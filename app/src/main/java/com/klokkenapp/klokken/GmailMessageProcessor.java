package com.klokkenapp.klokken;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;
import static com.klokkenapp.klokken.MainActivity.REQUEST_ACCOUNT_PICKER;
import static com.klokkenapp.klokken.MainActivity.REQUEST_AUTHORIZATION;
import static com.klokkenapp.klokken.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.klokkenapp.klokken.MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS;


public class GmailMessageProcessor {

    private static final String PREF_ACCOUNT_NAME = "accountName";

    public GmailMessageProcessor() {
        //Contructor
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
                    .setApplicationName("Gmail API Android Quickstart")
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

            Message m = printMessages(mService, user, "157ee48485e0cd54");

            for (Message message : listResponseMail.getMessages()) {
                messages.add(message.toPrettyString());

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

            //System.out.println("Message snippet: " + message.getSnippet());
            System.out.println("Message To: " + message.getPayload().getHeaders().get(0).getValue());
            System.out.println("Message Received: " + message.getPayload().getHeaders().get(1).getValue());


            System.out.println("Message Subject: " + message.getPayload().getHeaders().get(19).getValue());
            System.out.println("Message From: " + message.getPayload().getHeaders().get(20).getValue());

            try {
                getMimeMessage(mService, userId, messageId);
            } catch (MessagingException e) {
                e.printStackTrace();
            }

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

            System.out.println(email.getSubject());
            System.out.println(email.getFrom());
            System.out.println(email.getReceivedDate());

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
