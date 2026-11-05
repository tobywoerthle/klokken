package com.klokkenapp.klokken;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ImapAccountActivity extends Activity {

    private EditText hostInput;
    private EditText portInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private Spinner securityInput;
    private ImapAccount savedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imap_account);

        hostInput = (EditText) findViewById(R.id.imap_host);
        portInput = (EditText) findViewById(R.id.imap_port);
        usernameInput = (EditText) findViewById(R.id.imap_username);
        passwordInput = (EditText) findViewById(R.id.imap_password);
        securityInput = (Spinner) findViewById(R.id.imap_security);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.imap_security_titles,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityInput.setAdapter(adapter);

        savedAccount = ImapAccountStore.load(this);
        if (savedAccount != null) {
            hostInput.setText(savedAccount.getHost());
            portInput.setText(Integer.toString(savedAccount.getPort()));
            usernameInput.setText(savedAccount.getUsername());
            passwordInput.setHint(R.string.imap_password_keep_hint);
            securityInput.setSelection(ImapAccount.SECURITY_STARTTLS.equals(
                    savedAccount.getSecurity()) ? 1 : 0);
        }

        findViewById(R.id.imap_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccount();
            }
        });
        findViewById(R.id.imap_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void saveAccount() {
        String host = hostInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String portText = portInput.getText().toString().trim();
        String security = securityInput.getSelectedItemPosition() == 1
                ? ImapAccount.SECURITY_STARTTLS : ImapAccount.SECURITY_SSL;

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException error) {
            portInput.setError(getString(R.string.imap_invalid_port));
            return;
        }

        if (password.isEmpty() && savedAccount != null) {
            password = savedAccount.getPassword();
        }

        ImapAccount account = new ImapAccount(host, port, username, password, security);
        if (!account.isValid()) {
            if (host.isEmpty()) {
                hostInput.setError(getString(R.string.imap_host_required));
            } else if (username.isEmpty()) {
                usernameInput.setError(getString(R.string.imap_username_required));
            } else if (password.isEmpty()) {
                passwordInput.setError(getString(R.string.imap_password_required));
            } else {
                portInput.setError(getString(R.string.imap_invalid_port));
            }
            return;
        }

        try {
            ImapAccountStore.save(this, account);
            Toast.makeText(this, R.string.imap_account_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception error) {
            Toast.makeText(this, R.string.imap_account_save_failed, Toast.LENGTH_LONG).show();
        }
    }
}
