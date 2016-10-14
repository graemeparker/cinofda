package com.adfonic.email;

import javax.mail.Message;

public interface MessageHandler {
    void handleMessage(Message message);
}
