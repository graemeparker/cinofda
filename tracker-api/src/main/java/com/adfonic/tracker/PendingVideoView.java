package com.adfonic.tracker;

import java.util.Date;

public interface PendingVideoView {
    Date getCreationTime();
    String getClickExternalID();
    int getViewMs();
    int getClipMs();
}