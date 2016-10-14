package com.adfonic.presentation.publication.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.presentation.audience.dao.MuidNotificationDao;
import com.adfonic.presentation.audience.model.MuidSessionModel;
import com.adfonic.presentation.publication.service.MuidNotificationService;

@Service("muidNotificationService")
public class MuidNotificationServiceImpl implements MuidNotificationService {

    @Autowired
    private MuidNotificationDao muidNotificationDao;

    // MUID notification API

    @Override
    public MuidSessionModel checkSessionStatus(BigDecimal sessionId) {
        return muidNotificationDao.inboundCheckProgress(sessionId);
    }

}
