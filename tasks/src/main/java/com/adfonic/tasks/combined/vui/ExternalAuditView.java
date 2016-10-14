package com.adfonic.tasks.combined.vui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.tasks.combined.web.ExternalAuditController;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;
import com.adfonic.tasks.xaudit.adx.AdXCreativeApiManager;
import com.adfonic.tasks.xaudit.appnxs.AppNexusApiClient;
import com.adfonic.tasks.xaudit.appnxs.AppNexusCreativeSystem;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = ExternalAuditView.VIEW_NAME)
public class ExternalAuditView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "/xaudit";

    TextField tfCreative = new TextField("Creative");
    OptionGroup ogExchange = new OptionGroup("Exchange");
    Button btLoad = new Button("Load");

    AuditFormLayout auditRecordForm = new AuditFormLayout();

    Button btRender = new Button("Render");
    Button btSubmit = new Button("Submit");

    Link apiLink = new Link("API Link", null);
    TextArea apiText = new TextArea("Audit API");

    @Autowired
    PublisherManager publisherManager;

    @Autowired
    CreativeManager creativeManager;

    @Autowired
    AdXAuditService adxService;

    @Autowired
    AdXCreativeApiManager adxClient;

    @Autowired
    AppNexusCreativeSystem apnxSystem;

    @Autowired
    AppNexusApiClient apnxClient;

    private static final ObjectMapper jackson = new ObjectMapper(); // for api responses....

    public ExternalAuditView() {
        Component menubar = DefaultView.buildMenuBar();
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setSpacing(true);
        addComponent(menubar);
        addComponent(content);
        HorizontalLayout mainInputs = new HorizontalLayout(tfCreative, ogExchange, btLoad);
        mainInputs.setSpacing(true);
        content.addComponent(mainInputs);

        ogExchange.addItems("AdX", "AppNexus");
        ogExchange.addStyleName("horizontal");

        VerticalLayout apiForm = new VerticalLayout();
        apiForm.addComponent(apiLink);
        apiLink.setTargetName("_blank");
        apiLink.setVisible(false);
        apiForm.addComponent(apiText);

        apiText.setColumns(40);
        apiText.setRows(20);
        HorizontalLayout forms = new HorizontalLayout(auditRecordForm, apiForm);
        forms.setSpacing(true);
        content.addComponent(forms);
        content.addComponent(new HorizontalLayout(btRender, btSubmit));

        btLoad.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                String creativeIdent = tfCreative.getValue();
                Long publisherId = getPublisherId();
                if (StringUtils.isNotBlank(creativeIdent) && publisherId != null) {
                    load(creativeIdent, publisherId);
                }
            }
        });

        btRender.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                String creativeIdent = tfCreative.getValue();
                Long publisherId = getPublisherId();
                if (StringUtils.isNotBlank(creativeIdent) && publisherId != null) {
                    render(creativeIdent, publisherId);
                }
            }
        });
        btSubmit.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                String creativeIdent = tfCreative.getValue();
                Long publisherId = getPublisherId();
                if (StringUtils.isNotBlank(creativeIdent) && publisherId != null) {
                    submit(creativeIdent, publisherId);
                }
            }
        });

    }

    private void load(String creativeIdent, long publisherId) {
        Long creativeId = com.adfonic.util.StringUtils.tryLong(creativeIdent);
        Creative byydCreative;
        if (creativeId != null) {
            byydCreative = creativeManager.getCreativeById(creativeId);
        } else {
            byydCreative = creativeManager.getCreativeByExternalId(creativeIdent);
        }
        if (byydCreative == null) {
            Notification.show("Not found", "Creative " + creativeIdent + " not found", Notification.Type.ERROR_MESSAGE);
            return;
        }
        Publisher byydPublisher = publisherManager.getPublisherById(publisherId);

        // Query exchange API
        queryAuditApi(byydCreative.getExternalID(), byydPublisher.getId());

        // Query tools DB
        PublisherAuditedCreative auditRecord = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(byydPublisher, byydCreative);
        auditRecordForm.setAuditRecord(auditRecord, byydCreative, publisherId);

        String apiUrl;
        if (publisherId == adxService.getPublisherId()) {
            apiUrl = "https://developers.google.com/apis-explorer/?hl=en_US#p/adexchangebuyer/v1.4/adexchangebuyer.creatives.get?accountId=" + adxClient.getApiAccountId()
                    + "&buyerCreativeId=" + byydCreative.getExternalID();
        } else {
            apiUrl = "https://bidder.adnxs.net/creative?code=" + byydCreative.getExternalID() + ((auditRecord != null) ? "&id=" + auditRecord.getExternalReference() : "");
        }
        apiLink.setResource(new ExternalResource(apiUrl));
        apiLink.setVisible(true);
    }

    private void render(String creativeIdent, Long publisherId) {
        Publisher byydPublisher = publisherManager.getPublisherById(publisherId, AdXAuditService.PUBLISHER_FETCH_STRATEGY);
        Long creativeId = com.adfonic.util.StringUtils.tryLong(creativeIdent);
        Creative byydCreative;
        if (creativeId != null) {
            byydCreative = creativeManager.getCreativeById(creativeId, AdXAuditService.CREATIVE_FETCH_STRATEGY);
        } else {
            byydCreative = creativeManager.getCreativeByExternalId(creativeIdent, AdXAuditService.CREATIVE_FETCH_STRATEGY);
        }
        if (byydCreative == null) {
            Notification.show("Not found", "Creative " + creativeIdent + " not found", Notification.Type.ERROR_MESSAGE);
            return;
        }
        FaultySupplier<Object> supplier = new FaultySupplier<Object>() {

            @Override
            public Object get() throws Exception {
                if (publisherId == adxService.getPublisherId()) {
                    return adxService.buildAdxCreative(byydCreative, byydPublisher);
                } else {
                    return apnxSystem.buildAppNexusCreative(byydCreative, byydPublisher);
                }
            }
        };
        doRenderApiJson(supplier);
    }

    protected void submit(String creativeIdent, Long publisherId) {
        Publisher byydPublisher = publisherManager.getPublisherById(publisherId);
        Long creativeId = com.adfonic.util.StringUtils.tryLong(creativeIdent);
        Creative byydCreative;
        if (creativeId != null) {
            byydCreative = creativeManager.getCreativeById(creativeId);
        } else {
            byydCreative = creativeManager.getCreativeByExternalId(creativeIdent);
        }
        if (byydCreative == null) {
            Notification.show("Not found", "Creative " + creativeIdent + " not found", Notification.Type.ERROR_MESSAGE);
            return;
        }
        PublisherAuditedCreative auditRecord = publisherManager.getPublisherAuditedCreativeByPublisherAndCreative(byydPublisher, byydCreative);
        if (publisherId == adxService.getPublisherId()) {
            if (auditRecord != null) {
                auditRecord.setStatus(PublisherAuditedCreative.Status.LOCAL_INVALID);
                auditRecord.setLastAuditRemarks("Manual resubmit");
                publisherManager.update(auditRecord);
                adxService.onScheduledCheck(byydCreative.getId());
                apiText.setValue("Updated audit record and submitted to AdX");
            } else {
                adxService.onNewCreative(byydCreative.getId());
                apiText.setValue("Created audit record and submitted to AdX");
            }
        } else {
            //TODO redevelop appnexus submittion..
            Notification.show("Sorry", "No AppNexus support", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void queryAuditApi(String creativeExternalId, long publisherId) {
        FaultySupplier<Object> supplier = new FaultySupplier<Object>() {

            @Override
            public Object get() {
                if (publisherId == adxService.getPublisherId()) {
                    return adxClient.getAdxCreative(creativeExternalId);
                } else {
                    return apnxClient.getCreativeByCode(creativeExternalId);
                }
            }
        };
        doRenderApiJson(supplier);

    }

    private void doRenderApiJson(FaultySupplier<Object> supplier) {
        try {
            Object object = supplier.get();
            if (object != null) {
                String apiJson = jackson.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                apiText.setValue(apiJson);
            } else {
                apiText.setValue(null);
            }
        } catch (Exception x) {
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            apiText.setValue(sw.getBuffer().toString());
        }

    }

    private Long getPublisherId() {
        String exchange = (String) ogExchange.getValue();
        if (exchange != null) {
            if (exchange.equals("AdX")) {
                return adxService.getPublisherId();
            } else {
                return ExternalAuditController.APPNEXUS_PUBLISHER_ID;
            }
        } else {
            return null;
        }
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // the view is constructed in the init() method()
    }

    class AuditFormLayout extends FormLayout {

        private static final long serialVersionUID = 1L;

        @PropertyId("id")
        TextField tfId = new TextField("Id");
        @PropertyId("externalReference")
        TextField tfExternalId = new TextField("External Id");
        @PropertyId("status")
        TextField tfStatus = new TextField("Status");
        @PropertyId("creationTime")
        TextField tfCreateTime = new TextField("Created");
        @PropertyId("latestFetchTime")
        TextField tfFetchTime = new TextField("Fetched");
        @PropertyId("messageCount")
        TextField tfMessages = new TextField("Messages");

        private BeanFieldGroup<PublisherAuditedCreative> beanFieldGroup;

        public AuditFormLayout() {
            setCaption("PUBLISHER_AUDITED_CREATIVE");
            addComponent(tfId);
            addComponent(tfStatus);
            addComponent(tfExternalId);
            addComponent(tfCreateTime);
            addComponent(tfFetchTime);
            addComponent(tfMessages);
            this.beanFieldGroup = new BeanFieldGroup<PublisherAuditedCreative>(PublisherAuditedCreative.class);
            this.beanFieldGroup.bindMemberFields(this);
            setSpacing(false);
            setMargin(false);
        }

        public void setAuditRecord(PublisherAuditedCreative auditRecord, Creative byydCreative, long publisherId) {
            this.beanFieldGroup.setItemDataSource(auditRecord);
        }
    }

    public interface FaultySupplier<T> {

        T get() throws Exception;
    }
}