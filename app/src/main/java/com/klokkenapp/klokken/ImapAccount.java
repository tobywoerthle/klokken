package com.klokkenapp.klokken;

public class ImapAccount {

    public static final String SECURITY_SSL = "ssl";
    public static final String SECURITY_STARTTLS = "starttls";

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String security;

    public ImapAccount(String host, int port, String username, String password, String security) {
        this.host = host == null ? "" : host.trim();
        this.port = port;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
        this.security = SECURITY_STARTTLS.equals(security) ? SECURITY_STARTTLS : SECURITY_SSL;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurity() {
        return security;
    }

    public boolean isValid() {
        return !host.isEmpty()
                && port > 0
                && port <= 65535
                && !username.isEmpty()
                && !password.isEmpty();
    }
}
