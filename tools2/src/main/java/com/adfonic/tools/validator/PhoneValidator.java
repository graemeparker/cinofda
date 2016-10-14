package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;

public class PhoneValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        boolean correctNumber = true;
        String phone = (String) value;
        FacesMessage fm = null;

        if (phone == null || phone.length() < 2) {
            correctNumber = false;
            fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone", "");
        }

        int startingIndex = 1;
        if (correctNumber) {
            if (correctNumber && !phone.startsWith("+")) {
                correctNumber = false;
                fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.startmissing", "");
            } else if (!Character.isDigit(phone.charAt(1))) {
                correctNumber = false;
                fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.startmissing", "");
            }
        }

        if (correctNumber) {
            // Strip out spaces, dashes, and parentheses; stop at the first
            // character that is not one of those or a digit
            StringBuilder sb = new StringBuilder();
            char[] exploded = phone.toCharArray();
            parser: for (int i = startingIndex; i < exploded.length; i++) {
                char ch = exploded[i];
                switch (ch) {
                case '(':
                case ')':
                case ' ':
                case '-':
                case '/':
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    sb.append(ch);
                    break;

                default:
                    if (!Character.isDigit(ch)) {
                        correctNumber = false;
                        fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.chars", "");
                    }
                    break parser;
                }
            }
            String rawDigits = sb.toString();

            if (correctNumber) {
                // Special tests for US and UK numbers
                if (rawDigits.startsWith("1")) {
                    // North American Numbering Plan, +1 + 10 digits
                    if (rawDigits.length() != 11) {
                        correctNumber = false;
                        fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.us", "");
                    }
                } else if (rawDigits.startsWith("44")) {
                    // British numbers are usually 10,
                    // and per 1028 may be as few as code + six digits
                    if ((rawDigits.length() < 8) || (rawDigits.length() > 13)) {
                        correctNumber = false;
                        fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.uk", "");
                    }
                } else {
                    // For rest-of-world, country codes are at least 2 digits
                    // long,
                    // and let's assume a minimum of 5 digits of phone number.
                    // In most cases that's probably not enough, but whatever.
                    if (rawDigits.length() < 7) {
                        correctNumber = false;
                        fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.error.validation.phone.uk", "");
                    }
                }
            }
        }

        if (!correctNumber) {
            throw new ValidatorException(fm);
        }
    }
}
