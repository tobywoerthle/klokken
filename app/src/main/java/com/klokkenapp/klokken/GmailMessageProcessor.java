package com.klokkenapp.klokken;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.model.MessagePartHeader;


public class GmailMessageProcessor extends MainActivity {

    public static final String ClassName = "GmailMessageProcessor";

    private static final String PREF_ACCOUNT_NAME = "accountName";

    public Map<String, GmailMessage> messagesToTransfer = new HashMap<>();

    public Map<String, GmailMessage> getMessages() {
        return messagesToTransfer;
    }

    private static ServiceKlokken serviceKlokken;

    private static GoogleAccountCredential accountCredential;

    public GmailMessageProcessor(GoogleAccountCredential inAccountCredential, ServiceKlokken inServiceKlokken) {
        accountCredential = inAccountCredential;
        serviceKlokken = inServiceKlokken;
    }

    public void startMakeRequestTask(){
        new MakeRequestTaskGmail(accountCredential).execute();
        System.out.println("MakeRequestTaskGmail creation");
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTaskGmail extends AsyncTask<Void, Void, List<String>> {
    //public class MakeRequestTaskGmail {
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

            System.out.println("getDataFromApi call");
            // Get the labels in the user's account.
            String user = "me";
            List<String> messageIds = new ArrayList<String>();
            /*
            ListLabelsResponse listResponse =
                    mService.users().labels().list(user).execute();
            ListMessagesResponse listResponseMail =
                    mService.users().messages().list(user).execute();
            */

            // TODO: Ability to set mailbox
            // ex. in:inbox OR in:Klokken

            System.out.println("getDataFromApi queryMessageReturn call");
            ListMessagesResponse queryMessageReturn = mService.users().messages().list(user).setQ("in:inbox is:unread").execute();
            System.out.println("getDataFromApi queryMessageReturn call finished");

            List<Message> messagesRaw = queryMessageReturn.getMessages();

            HashMap<String, GmailMessage> curMessages = new HashMap<String, GmailMessage>();

            if(messagesRaw != null && messagesRaw.isEmpty() == false){
                for (Message message : messagesRaw) {
                    Log.d(ClassName, "getDataFromApi.messageIter");

                    GmailMessage curMessage = printMessages(mService, user, message.getId());

                    messagesToTransfer.put(message.getId(),curMessage);
                    messageIds.add(message.getId());;
                }
            }
            else{
                Log.d(ClassName, "getDataFromApi.messageIter messagesRaw null");
            }

            return messageIds;
        }


        /**
        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            //mProgress.show();
        }
        */

        public GmailMessage printMessages (com.google.api.services.gmail.Gmail service, String userId, String messageId)
                throws IOException {

            boolean messageDebug = false;

            Message message = service.users().messages().get(userId, messageId).execute();
            Log.d(ClassName, "printMessages.message");

            List<MessagePartHeader> headers = message.getPayload().getHeaders();

            GmailMessage gmailMessage = new GmailMessage();
            gmailMessage.setMessageID(messageId);

            for (MessagePartHeader header: headers) {
                if(header.getName().equals("From")){
                    if(messageDebug){
                        System.out.println("Message From: " + header.getValue());
                    }
                    gmailMessage.setMessageFrom(header.getValue());
                }
                else if(header.getName().equals("To")){
                    if(messageDebug){
                        System.out.println("Message To: " + header.getValue());
                    }
                    gmailMessage.setMessageTo(header.getValue());
                }
                else if(header.getName().equals("Date")){

                    if(messageDebug){
                        System.out.println("Message Date: " + header.getValue());
                    }
                    gmailMessage.setMessageDate(header.getValue());
                }
                else if(header.getName().equals("Received")){
                    String unparsedReceived = header.getValue();
                    int indexOfSemicolon = unparsedReceived.indexOf(";");
                    String formattedDate = unparsedReceived.substring(indexOfSemicolon+1);

                    while(Character.isWhitespace(formattedDate.charAt(0))){
                        formattedDate = formattedDate.substring(1);
                    }

                    gmailMessage.setMessageReceived(formattedDate);

                    if(messageDebug){
                        System.out.println("Message Received (Raw): " + header.getValue());
                        System.out.println("Message Received (Format): |" + formattedDate + "|") ;
                    }

                }
                else if(header.getName().equals("Subject")){
                    if(messageDebug){
                        System.out.println("Message Subject: " + header.getValue());
                    }
                    gmailMessage.setMessageSubject(header.getValue());
                }
                else {
                    if(messageDebug){
                        System.out.println("Other: " + header.getName() + " | " + header.getValue());
                    }
                }

                gmailMessage.setMessageBody(StringUtils.newStringUtf8(Base64.decodeBase64( message.getPayload().getBody().getData())));

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
            return gmailMessage;
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
            System.out.println("Finished ASYNC completely");
            serviceKlokken.gmailMessagesPostAsyncTask();
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
