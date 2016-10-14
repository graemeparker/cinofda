package com.adfonic.presentation.audience.dao;

import java.math.BigDecimal;

import com.adfonic.presentation.audience.model.MuidSessionModel;

public interface MuidNotificationDao {

    // MUID Notification

    MuidSessionModel inboundCheckProgress(BigDecimal sessionId);

}