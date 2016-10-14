package com.adfonic.email;

public enum MailProtocol {
    POP3(110), POP3S(995), IMAP(143), IMAPS(993);

    private final int defaultPort;
    
    private MailProtocol(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int getDefaultPort() {
        return defaultPort;
    }
}