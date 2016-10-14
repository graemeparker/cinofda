package com.adfonic.sso.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.springframework.context.ApplicationContext;

import com.adfonic.sso.beans.ApplicationContextProvider;
import com.adfonic.sso.beans.ConfigurationBean;

public class CaptchaUtils {
    private static final Logger LOG = Logger.getLogger(CaptchaUtils.class.getName());
    
    private CaptchaUtils(){
    }
    
    public static boolean verify(String remoteAddr, String challenge, String userResponse){
        boolean isValid = true;
        
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        ConfigurationBean configurationBean = context.getBean(ConfigurationBean.class);
        
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(configurationBean.getRecaptchaPrivatekey());
        try{
            ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, userResponse);
            if (!reCaptchaResponse.isValid()){
                LOG.fine("Registration capture incorrect");
                isValid = false;
            }
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Could not validate ReCaptcha challenge", e);
            isValid = false;
        }
        return isValid;
    }

}
