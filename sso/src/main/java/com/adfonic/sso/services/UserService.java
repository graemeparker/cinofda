package com.adfonic.sso.services;

import java.util.List;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.util.AdfonicTimeZone;

public interface UserService {
    
    public User getUserById(Long id);
    public User getUserByEmail(String email);
    public VerificationCode resetUserPwd(User user);
    public boolean verifyUser(User user, VerificationCode vc);
    public User updateUserPwd(Long userId, String pwd);
    public User createUser (String companyName,
                            Country country,
                            String email,
                            String firstName,
                            String lastName,
                            String password,
                            String referralType,
                            String referralTypeOther,
                            List<String> roleNames,
                            AccountType accountType,
                            CurrencyExchangeRate defaultCurrencyExchangeRate,
                            AdfonicTimeZone timezone,
                            boolean keepMeInformed);
}
