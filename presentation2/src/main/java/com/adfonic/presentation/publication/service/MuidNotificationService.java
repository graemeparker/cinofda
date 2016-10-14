package com.adfonic.presentation.publication.service;

import java.math.BigDecimal;

import com.adfonic.presentation.audience.model.MuidSessionModel;

public interface MuidNotificationService {

    // Approval Dash board

    MuidSessionModel checkSessionStatus(BigDecimal sessionId);
}
