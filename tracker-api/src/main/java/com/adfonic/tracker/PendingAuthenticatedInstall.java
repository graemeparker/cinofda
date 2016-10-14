package com.adfonic.tracker;

import java.util.Date;

public interface PendingAuthenticatedInstall {
    Date getCreationTime();
    String getClickExternalID();
}