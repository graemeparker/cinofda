package com.adfonic.sso.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

public class HearAboutPlacesBuilder {
    
    private HearAboutPlacesBuilder(){
    }

    public static List<HearAboutPlace> build() {
        List<HearAboutPlace> hearAboutPlacesList = new ArrayList<HearAboutPlace>();
        
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        Locale locale = LocaleContextHolder.getLocale();
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.directory.value", null, locale ),
                                                     context.getMessage("signup.form.hearabout.option.directory.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.blog.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.blog.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.search.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.search.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.press.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.press.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.wordofmouth.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.wordofmouth.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.twitter.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.twitter.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.linkedin.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.linkedin.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.facebook.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.facebook.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.email.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.email.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.onlineads.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.onlineads.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.events.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.events.label", null, locale)));
        
        hearAboutPlacesList.add(new HearAboutPlace( context.getMessage("signup.form.hearabout.option.other.value", null, locale),
                                                    context.getMessage("signup.form.hearabout.option.other.label", null, locale)));
        
        return hearAboutPlacesList;
    }

}
