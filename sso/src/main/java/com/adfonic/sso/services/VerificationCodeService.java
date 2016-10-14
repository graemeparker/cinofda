package com.adfonic.sso.services;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;

public interface VerificationCodeService {

    public VerificationCode newVerificationCode(User user, VerificationCode.CodeType codeType);
    public VerificationCode getVerificationCode(String code);
    public boolean deleteVerificationCode(VerificationCode code);
}
