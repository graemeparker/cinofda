package com.adfonic.tasks.combined.vui;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;

@Theme("valo")
@SpringUI(path = "/vui")
public class TasksVaadinUI extends UI implements ErrorHandler, ViewChangeListener {

    private static final long serialVersionUID = 1L;

    @Autowired
    private SpringViewProvider viewProvider;

    @Override
    protected void init(VaadinRequest request) {
        setSizeFull();
        //getSession().getConfiguration().getHeartbeatInterval()

        //DiscoveryNavigator automaticaly gathers Spring managed @VaadinView anotated Views  
        //getSession().setErrorHandler(this);
        //getNavigator().addViewChangeListener(this);

        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(viewProvider);
        setNavigator(navigator);
        VaadinSession.getCurrent().setConverterFactory(new CustomConverterFactory());
    }

    @Override
    public void error(com.vaadin.server.ErrorEvent event) {
        DefaultErrorHandler.doDefault(event);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        return true;
    }

    @Override
    public void afterViewChange(ViewChangeEvent event) {

    }

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