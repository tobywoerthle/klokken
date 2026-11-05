package com.klokkenapp.klokken;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class ImapAccountStore {

    private static final String TAG = "ImapAccountStore";
    private static final String PREFERENCES_NAME = "imap_account_storage";
    private static final String ENCRYPTED_ACCOUNT_KEY = "encrypted_account";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "klokken_imap_account";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private ImapAccountStore() {
    }

    public static synchronized void save(Context context, ImapAccount account)
            throws Exception {
        if (account == null || !account.isValid()) {
            throw new IllegalArgumentException("Valid IMAP settings are required.");
        }

        JSONObject json = new JSONObject();
        json.put("host", account.getHost());
        json.put("port", account.getPort());
        json.put("username", account.getUsername());
        json.put("password", account.getPassword());
        json.put("security", account.getSecurity());

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(json.toString().getBytes(UTF_8));
        String value = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                + "."
                + Base64.encodeToString(encrypted, Base64.NO_WRAP);

        boolean saved = preferences(context).edit()
                .putString(ENCRYPTED_ACCOUNT_KEY, value)
                .commit();
        if (!saved) {
            throw new IOException("Unable to save encrypted IMAP settings.");
        }
    }

    public static synchronized ImapAccount load(Context context) {
        String value = preferences(context).getString(ENCRYPTED_ACCOUNT_KEY, null);
        if (value == null) {
            return null;
        }

        try {
            int separator = value.indexOf('.');
            if (separator <= 0 || separator == value.length() - 1) {
                throw new GeneralSecurityException("Invalid encrypted settings.");
            }

            byte[] iv = Base64.decode(value.substring(0, separator), Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(value.substring(separator + 1), Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            JSONObject json = new JSONObject(new String(cipher.doFinal(encrypted), UTF_8));

            ImapAccount account = new ImapAccount(
                    json.getString("host"),
                    json.getInt("port"),
                    json.getString("username"),
                    json.getString("password"),
                    json.getString("security"));
            return account.isValid() ? account : null;
        } catch (Exception error) {
            // Do not log the encrypted payload or any account data.
            Log.w(TAG, "Unable to decrypt saved IMAP settings.");
            return null;
        }
    }

    public static synchronized void clear(Context context) {
        preferences(context).edit().remove(ENCRYPTED_ACCOUNT_KEY).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        Key existingKey = keyStore.getKey(KEY_ALIAS, null);
        if (existingKey instanceof SecretKey) {
            return (SecretKey) existingKey;
        }

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();
        generator.init(spec);
        return generator.generateKey();
    }
}
