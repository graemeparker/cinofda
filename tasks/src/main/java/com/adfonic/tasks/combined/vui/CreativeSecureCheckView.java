package com.adfonic.tasks.combined.vui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = CreativeSecureCheckView.VIEW_NAME)
public class CreativeSecureCheckView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "/secure";

    private final TextField tfCreative = new TextField("Creative");

    private final Button btCheck = new Button("Check");

    private final Table tbResults = new Table();

    public CreativeSecureCheckView() {
        addComponent(tfCreative);
        addComponent(btCheck);
        addComponent(tbResults);

        btCheck.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                checkCreative(tfCreative.getValue());
            }
        });
    }

    private void checkCreative(String creativeIdent) {
        //CreativeServiceImpl.
    }

    @Override
    public void enter(ViewChangeEvent event) {

    }
}
