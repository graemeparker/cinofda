package com.adfonic.email;

import javax.mail.MessagingException;

public interface MailPoller {
    void pollForNewMessages(MessageHandler messageHandler) throws MessagingException;
}
