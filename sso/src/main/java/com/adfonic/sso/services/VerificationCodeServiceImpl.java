package com.adfonic.sso.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.domain.VerificationCode_;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());
    
    // Verification code fetch strategy
    private static final FetchStrategy VC_FS;

    static {
        VC_FS = new FetchStrategyBuilder()
                 .addInner(VerificationCode_.user)
                 .build();
    }
    
    @Autowired
    protected AccountManager accountManager;
    
    @Override
    @Transactional
    public VerificationCode newVerificationCode(User user, VerificationCode.CodeType codeType){
        return accountManager.newVerificationCode(user, codeType);
    }
    
    @Override
    @Transactional
    public VerificationCode getVerificationCode(String code){
        return accountManager.getVerificationCodeForCodeValue(code, VC_FS);
    }
    
    @Override
    @Transactional
    public boolean deleteVerificationCode(VerificationCode vc){
        boolean result = true;
        try {
            accountManager.delete(vc);
        }catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to remove vc code=" + vc.getCode(), e);
            result = false;
        }
        return result;
    }
}
