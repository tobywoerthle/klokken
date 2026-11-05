package com.klokkenapp.klokken;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;

public class ImapMessageAcknowledger extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ImapMessageAcknowledger";

    private final Context context;
    private final MailMessage message;

    public ImapMessageAcknowledger(Context context, MailMessage message) {
        this.context = context.getApplicationContext();
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        ImapAccount account = ImapAccountStore.load(context);
        if (account == null || message == null) {
            return null;
        }

        Store store = null;
        Folder folder = null;
        try {
            Session session = Session.getInstance(ImapMessageProcessor.createProperties(account));
            store = session.getStore(ImapAccount.SECURITY_SSL.equals(account.getSecurity())
                    ? "imaps" : "imap");
            store.connect(account.getHost(), account.getPort(),
                    account.getUsername(), account.getPassword());
            folder = store.getFolder(message.getFolder());
            folder.open(Folder.READ_WRITE);

            long uid = Long.parseLong(message.getMessageID());
            Message source = ((UIDFolder) folder).getMessageByUID(uid);
            if (source != null) {
                source.setFlag(Flags.Flag.SEEN, true);
            }
        } catch (Exception error) {
            // Never include account data or credentials in logs.
            Log.w(TAG, "Unable to mark the IMAP message as read.");
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception error) {
                Log.w(TAG, "Unable to close the IMAP connection.");
            }
        }
        return null;
    }
}
