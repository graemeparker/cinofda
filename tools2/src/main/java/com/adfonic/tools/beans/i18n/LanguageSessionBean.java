
/* Session Managed bean to change languages in the web app.
 * Default Language is en_uk.
 *
 */
package com.adfonic.tools.beans.i18n;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.util.DateUtils;

/**
 * The Class LanguageBean.
 */
@Component
@Scope("session")
public class LanguageSessionBean implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The locale. */
    private Locale locale = FacesUtils.getLocale();

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        return locale.getLanguage();
    }

    /**
     * Sets the language.
     *
     * @param language
     *            the new language
     */
    public void setLanguage(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    /**
     * Change locale.
     *
     * @param e
     *            the e
     */
    public void changeLocale(ValueChangeEvent e) {
        String newLocaleValue = e.getNewValue().toString();
        setLanguage(newLocaleValue);
    }

    public String getLongDateFormat() {
        return DateUtils.getLongDateFormat();
    }
    
    public String getDateFormat() {
        return DateUtils.getDateFormat();
    }

    public String getDateFormatTooltips() {
        return DateUtils.getDateFormatTooltips();
    }

    public String getTimeStampFormat() {
        return DateUtils.getTimeStampFormat();
    }
}
