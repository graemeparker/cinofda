package com.adfonic.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import com.adfonic.domain.Named;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.util.CreditCardUtils;

/**
 * This class taken from http://sfjsf.blogspot.com/2006/03/usings-sets-with-uidata.html then dumbed down.
 */
@RequestScoped
@ManagedBean(name="util")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class UtilBean extends BaseBean{
	private Map<Collection, List> _map;
    private Map<Collection, List<SelectItem>> _map2;

    public UtilBean() {
        _map = new MakeList();
        _map2 = new MakeList2();
    }

    public Map<Collection, List> getList() { return _map; }
    public Map<Collection, List<SelectItem>> getSelectItemList() { return _map2; }

    private static final class MakeList extends AbstractMap<Collection, List> {
		public List get(Object o) {
            if (!(o instanceof Collection))
                return null;

			List l = new ArrayList((Collection) o);
            Collections.sort(l, Named.COMPARATOR);
            return l;
        }

        public Set<Map.Entry<Collection, List>> entrySet() {
            // Not worth implementing at the moment;  this Map is only
            // accessed from
            return Collections.emptySet();
        }
    }

    private static final class MakeList2 extends AbstractMap<Collection, List<SelectItem>> {
        public List<SelectItem> get(Object o) {
            if (!(o instanceof Collection))
                return null;

            return FacesUtils.makeSelectItems((Collection) o, true);
        }

        public Set<Map.Entry<Collection, List<SelectItem>>> entrySet() {
            // Not worth implementing at the moment;  this Map is only
            // accessed from
            return Collections.emptySet();
        }
    }

    public void validateEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!(value instanceof String) || !Functions.isValidEmail((String) value)) {
            throw new ValidatorException(messageForId("error.generic.email"));
        }
    }

    public void validatePhoneNumber(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!(value instanceof String) || !Functions.isValidPhoneNumber((String) value)) {
            throw new ValidatorException(messageForId("error.generic.phoneNumber"));
        }
    }

    public void validateClickToCallNumber(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!(value instanceof String) || !Functions.isValidClickToCallNumber((String) value)) {
            throw new ValidatorException(messageForId("error.generic.clickToCallNumber"));
        }
    }

    public void validateURL(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!(value instanceof String) || !Functions.isValidURL((String) value)) {
            throw new ValidatorException(messageForId("error.generic.URL"));
        }
    }

    public void validateCreditCardNumber(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null || !CreditCardUtils.isValidCreditCardNumber(value.toString())) {
            throw new ValidatorException(messageForId("error.creditCard.numberInvalid"));
        }

    }

    /** Returns a date pattern like MM/dd/yy or dd/MM/yy */
    public String getShortDatePattern() {
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, getUserLocale());
        return sdf.toPattern();
    }

    /**
    * Five bucks to anyone who can come up with a simpler,
    * internationalized method for generating dates in the form MM/DD
    * or DD/MM depending on locale..
    */
    public String getTinyDatePattern() {
        String pattern = getShortDatePattern();
        // Remove the year.
        pattern = pattern.replaceAll("y", "");
        // Remove leading or trailing punctuation
        char ch;
        ch = pattern.charAt(0);
        if (ch < '0' || ch > '9') {
            pattern = pattern.substring(1);
        }
        ch = pattern.charAt(pattern.length() - 1);
        if (ch < '0' || ch > '9') {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        return pattern;
    }

    public boolean isPostback() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getRenderKit().getResponseStateManager().isPostback(fc);
    }

}

