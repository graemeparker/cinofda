package com.adfonic.presentation;

import com.ocpsoft.pretty.PrettyContext;

public class PrettyUtils {
    
    private PrettyUtils(){
    }

    /*
     * get pretty's url
     */
    public static String prettyRequestURL() {
        PrettyContext prettyContext = PrettyContext.getCurrentInstance();
        if (prettyContext != null) {
            return prettyContext.getRequestURL().toURL();
        } else {
            return null;
        }
    }

}
