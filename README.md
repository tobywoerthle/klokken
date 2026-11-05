Klokken

Messaging Ringer Android App

## Email providers

Klokken keeps the existing Gmail API sign-in and adds one configurable IMAP account.

1. Open **Settings** from the main screen.
2. Set **Provider** to **IMAP server**.
3. Open **Configure IMAP account** and enter the server host, port, encryption mode,
   username, and password or app password.

SSL/TLS on port 993 and STARTTLS on port 143 are supported. Klokken checks unread
messages in Inbox and marks an IMAP message as read when its alert is acknowledged.
The account settings are encrypted with an Android Keystore key on the device.

This IMAP sign-in supports providers that permit password or app-password
authentication. OAuth-only IMAP accounts need a provider-specific sign-in flow and
are not supported by this initial IMAP implementation. Only one email provider is
active at a time; choose Gmail or the configured IMAP account in Settings.

Feature Ideas

-Service + Timer for email check
-Search only today's emails
-Ringer (Ringtone) when email found
-Dismiss button
-Search Criteria
-Slack Integration
-SMS integration
-FaceBook Messenger integration
-Steam Integration
-Discord Integration
