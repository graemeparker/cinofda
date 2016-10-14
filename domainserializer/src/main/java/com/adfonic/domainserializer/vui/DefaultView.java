package com.adfonic.domainserializer.vui;

import javax.annotation.PostConstruct;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@UIScope
@SpringView(name = DefaultView.VIEW_NAME)
public class DefaultView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "";

    public DefaultView() {

    }

    @PostConstruct
    public void init() {
        addComponent(buildMenuBar());
        addComponent(new Label("I'm default view"));
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // TODO Auto-generated method stub
    }

    public static MenuBar buildMenuBar() {
        MenuBar menubar = new MenuBar();
        menubar.setWidth(100, Unit.PERCENTAGE);

        menubar.addItem("Status", new NavigateCommand("/status"));
        menubar.addItem("Eligibility", new NavigateCommand(EligibilityView.class));
        //file.addItem("Status", new NavigateCommand("/status"));

        MenuBar.MenuItem springBoot = menubar.addItem("Spring Boot", null, null);
        springBoot.addItem("Info", new NavigateCommand("/info"));
        springBoot.addItem("Health", new NavigateCommand("/health"));
        springBoot.addItem("Metrics", new NavigateCommand("/metrics"));
        springBoot.addItem("Spring Beans", new NavigateCommand("/beans"));
        springBoot.addItem("Thread Dump", new NavigateCommand("/dump"));
        springBoot.addItem("Configprops", new NavigateCommand("/configprops"));
        springBoot.addItem("URL Mappings", new NavigateCommand("/mappings"));
        springBoot.addItem("Autoconfig", new NavigateCommand("/autoconfig"));
        springBoot.addItem("Request Trace", new NavigateCommand("/trace"));

        /*
        MenuBar.MenuItem theme = menubar.addItem("Theme", null, null);
        theme.addItem("Valo", new SetThemeCommand("valo"));
        theme.addItem("Reindeer", new SetThemeCommand("reindeer"));
        */
        return menubar;
    }

    static class NavigateCommand implements MenuBar.Command {

        private static final long serialVersionUID = 1L;

        private String url;

        private String viewName;

        private boolean popup;

        public NavigateCommand(Class<? extends View> viewClass) {
            SpringView annotation = viewClass.getAnnotation(SpringView.class);
            if (annotation == null) {
                throw new IllegalArgumentException("No @SpringView annotation found on " + viewClass);
            }
            this.viewName = annotation.name();
        }

        public NavigateCommand(String url) {
            this(url, false);
        }

        public NavigateCommand(String url, boolean popup) {
            this.url = url;
            this.popup = popup;
        }

        @Override
        public void menuSelected(MenuItem selectedItem) {
            if (viewName != null) {
                UI.getCurrent().getNavigator().navigateTo(viewName);
            } else {
                String windowName = popup ? "_blank" : "_self";
                Page.getCurrent().open(url, windowName);
            }
        }

    }
}
