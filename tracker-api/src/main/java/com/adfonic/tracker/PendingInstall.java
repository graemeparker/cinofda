package com.adfonic.tracker;

import java.util.Date;

public interface PendingInstall {
    Date getCreationTime();
    String getApplicationId();
    long getDeviceIdentifierTypeId();
    String getDeviceIdentifier();
    boolean isClaim();
}