package com.adfonic.email.impl;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import com.adfonic.email.MailPoller;
import com.adfonic.email.MailProtocol;
import com.adfonic.email.MessageHandler;

public class MailPollerImpl implements MailPoller {
    private static final transient Logger LOG = Logger.getLogger(MailPollerImpl.class.getName());

    public static final String DEFAULT_FOLDER_NAME = "INBOX";

    private final MailProtocol protocol;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String mailboxDescription;
    private final String folderName;
    private Folder folder;
    private Store store;

    public MailPollerImpl(MailProtocol protocol, String host, String username, String password) {
        this(protocol, host, protocol.getDefaultPort(), username, password, DEFAULT_FOLDER_NAME);
    }
            
    public MailPollerImpl(MailProtocol protocol, String host, int port, String username, String password) {
        this(protocol, host, port, username, password, DEFAULT_FOLDER_NAME);
    }
            
    public MailPollerImpl(MailProtocol protocol, String host, int port, String username, String password, String folderName) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.folderName = folderName;
        mailboxDescription = protocol + "://" + host + ":" + port + "/" + folderName + "?username=" + username;
    }

    @PreDestroy
    public void destroy() {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
        
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, "Failed to close folder/store", e);
        }
    }

    private void ensureConnected() throws MessagingException {
        try {
            if (store != null && store.isConnected() && folder != null && folder.exists()) {
                return;
            }
        } catch (MessagingException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Exception while testing connection to " + mailboxDescription, e);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Connecting to " + mailboxDescription);
        }

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol.name().toLowerCase());

        Session session = Session.getInstance(props);
        
        store = session.getStore(protocol.name().toLowerCase());
        store.connect(host, port, username, password);

        folder = store.getFolder(folderName);
        if (folder == null || !folder.exists()) {
            throw new FolderNotFoundException(folder, "Folder not found or invalid: " + folderName);
        }
    }

    private void ensureFolderOpen() throws MessagingException {
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
    }

    @Override
    public void pollForNewMessages(MessageHandler messageHandler) throws MessagingException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Polling " + mailboxDescription);
        }
        
        ensureConnected();
        ensureFolderOpen();

        try {
            int count = folder.getMessageCount();
            if (count == -1) {
                throw new MessagingException("Folder is closed: " + folder.getFullName());
            } else if (count == 0) {
                return;
            }

            // Only poll for unseen messages
            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            if (messages != null && messages.length > 0) {
                processMessages(messages, messageHandler);
            }
        } finally {
            try {
                if (folder.isOpen()) {
                    folder.close(true);
                }
            } catch (MessagingException e) {
                // Some mail servers lock the folder, so this isn't
                // totally unexpected
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Failed to close folder: " + folder.getName(), e);
                }
            }
        }
    }

    private void processMessages(Message[] messages, MessageHandler messageHandler) throws MessagingException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Processing " + messages.length + " new message" + (messages.length == 1 ? "" : "s"));
        }

        for (Message message : messages) {
            if (message.getFlags().contains(Flags.Flag.DELETED)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Skipping deleted message");
                }
                continue;
            }
            
            boolean markSeenWhenDone = true;
            try {
                messageHandler.handleMessage(message);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to handle message", e);
                markSeenWhenDone = false;
            } finally {
                if (markSeenWhenDone) {
                    markMessageSeen(message);
                }
            }
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Done processing");
        }
    }

    private void markMessageSeen(Message message) {
        try {
            ensureFolderOpen();
            message.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, "Failed to mark message SEEN", e);
        }
    }
}
