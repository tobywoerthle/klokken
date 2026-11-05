package com.klokkenapp.klokken;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.UIDFolder;

public class ImapMessageProcessor {

    private static final String INBOX = "INBOX";
    private static final int MAX_ALERTS_PER_CHECK = 50;

    private final ImapAccount account;

    public ImapMessageProcessor(ImapAccount account) {
        this.account = account;
    }

    public Map<String, MailMessage> getUnreadMessages() throws Exception {
        if (account == null || !account.isValid()) {
            throw new IllegalArgumentException("IMAP settings are incomplete.");
        }

        Properties properties = createProperties(account);
        Session session = Session.getInstance(properties);
        Store store = session.getStore(ImapAccount.SECURITY_SSL.equals(account.getSecurity())
                ? "imaps" : "imap");
        Folder inbox = null;

        try {
            store.connect(account.getHost(), account.getPort(),
                    account.getUsername(), account.getPassword());
            inbox = store.getFolder(INBOX);
            inbox.open(Folder.READ_ONLY);

            Message[] unreadMessages = inbox.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            Map<String, MailMessage> result = new HashMap<>();
            UIDFolder uidFolder = (UIDFolder) inbox;
            int firstMessage = Math.max(0, unreadMessages.length - MAX_ALERTS_PER_CHECK);

            for (int i = firstMessage; i < unreadMessages.length; i++) {
                Message source = unreadMessages[i];
                long uid = uidFolder.getUID(source);
                if (uid <= 0) {
                    continue;
                }

                MailMessage mailMessage = new MailMessage();
                mailMessage.setProvider(MailMessage.PROVIDER_IMAP);
                mailMessage.setFolder(INBOX);
                mailMessage.setMessageID(Long.toString(uid));
                mailMessage.setThreadID(Long.toString(uid));
                mailMessage.setMessageFrom(addressesToString(source.getFrom()));
                mailMessage.setMessageTo(addressesToString(source.getRecipients(Message.RecipientType.TO)));
                mailMessage.setMessageSubject(source.getSubject() == null ? "" : source.getSubject());

                java.util.Date received = source.getReceivedDate();
                java.util.Date sent = source.getSentDate();
                java.util.Date messageDate = received != null ? received : sent;
                if (messageDate != null) {
                    mailMessage.setMessageDate(messageDate.toString());
                    mailMessage.setMessageReceived(messageDate.toString());
                }

                result.put(Long.toString(uid), mailMessage);
            }
            return result;
        } finally {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
            }
            if (store.isConnected()) {
                store.close();
            }
        }
    }

    static Properties createProperties(ImapAccount account) {
        Properties properties = new Properties();
        String protocol = ImapAccount.SECURITY_SSL.equals(account.getSecurity())
                ? "imaps" : "imap";
        String prefix = "mail." + protocol + ".";
        properties.setProperty(prefix + "connectiontimeout", "10000");
        properties.setProperty(prefix + "timeout", "15000");
        properties.setProperty(prefix + "writetimeout", "15000");

        if (ImapAccount.SECURITY_SSL.equals(account.getSecurity())) {
            properties.setProperty("mail.imaps.ssl.enable", "true");
            properties.setProperty("mail.imaps.ssl.checkserveridentity", "true");
        } else {
            properties.setProperty("mail.imap.starttls.enable", "true");
            properties.setProperty("mail.imap.starttls.required", "true");
            properties.setProperty("mail.imap.ssl.checkserveridentity", "true");
        }
        return properties;
    }

    private String addressesToString(javax.mail.Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (javax.mail.Address address : addresses) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(address.toString());
        }
        return result.toString();
    }
}
