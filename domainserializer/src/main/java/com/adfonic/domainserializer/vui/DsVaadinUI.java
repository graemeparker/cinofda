package com.adfonic.domainserializer.vui;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domainserializer.web.InvalidInputException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

@Theme("valo")
@SpringUI(path = "/vui")
@Title("Domain Serializer VUI")
public class DsVaadinUI extends UI implements ErrorHandler {

    private static final long serialVersionUID = 1L;

    @Autowired
    private SpringViewProvider viewProvider;

    @Override
    protected void init(VaadinRequest request) {
        setSizeFull();
        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(viewProvider);
        setNavigator(navigator);

        //getNavigator().addViewChangeListener(this);
        getSession().setErrorHandler(this);
        getSession().setConverterFactory(new CustomConverterFactory());
    }

    @Override
    public void error(com.vaadin.server.ErrorEvent event) {
        Throwable throwable = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
        if (throwable instanceof InvalidInputException) {
            InvalidInputException iix = (InvalidInputException) throwable;
            Notification.show(iix.getMessage(), Notification.Type.ERROR_MESSAGE);
        } else {
            DefaultErrorHandler.doDefault(event);
        }
    }

    /**
     * Normally Vadding prints numbers with ',' when > 999
     */
    static class CustomConverterFactory extends DefaultConverterFactory {

        private static final long serialVersionUID = 1L;

        @Override
        protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> findConverter(Class<PRESENTATION> presentationType, Class<MODEL> modelType) {
            if (presentationType == String.class && modelType == Integer.class) {
                return (Converter<PRESENTATION, MODEL>) new PlainStringToIntegerConverter();
            } else if (presentationType == String.class && modelType == Long.class) {
                return (Converter<PRESENTATION, MODEL>) new PlainStringToLongConverter();
            }
            return super.findConverter(presentationType, modelType);
        }
    }

    static class PlainStringToIntegerConverter extends StringToIntegerConverter {

        private static final long serialVersionUID = 1L;

        @Override
        protected java.text.NumberFormat getFormat(Locale locale) {
            NumberFormat format = super.getFormat(locale);
            format.setGroupingUsed(false);
            return format;
        };
    }

    static class PlainStringToLongConverter extends StringToLongConverter {

        private static final long serialVersionUID = 1L;

        @Override
        protected java.text.NumberFormat getFormat(Locale locale) {
            NumberFormat format = super.getFormat(locale);
            format.setGroupingUsed(false);
            return format;
        };
    }
}