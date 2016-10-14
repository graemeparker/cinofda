package com.adfonic.tasks.combined.vui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import com.adfonic.tasks.config.combined.SchedulingSpringConfig;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = DefaultView.VIEW_NAME)
public class DefaultView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "";

    private Table tableCron = new Table("Cron");
    private Table tableFixedRate = new Table("Fixed Rate");
    private Table tableFixedDelay = new Table("Fixed Delay");
    private Table tableTriggred = new Table("Triggered");

    @Autowired
    private SchedulingSpringConfig schedulingSpringConfig;

    public DefaultView() {

        tableCron.addContainerProperty("Target", String.class, null);
        tableCron.addContainerProperty("Expression", String.class, null);

        tableFixedRate.addContainerProperty("Target", String.class, null);
        tableFixedRate.addContainerProperty("Interval", Long.class, null);

        tableFixedDelay.addContainerProperty("Target", String.class, null);
        tableFixedDelay.addContainerProperty("Interval", Long.class, null);

        tableTriggred.addContainerProperty("Target", String.class, null);
        tableTriggred.addContainerProperty("Trigger", String.class, null);

        Component menubar = buildMenuBar();
        addComponent(menubar);
        addComponent(tableCron);
        addComponent(tableFixedRate);
        addComponent(tableFixedDelay);
        addComponent(tableTriggred);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        int cnt = 0;
        /*
        ScheduledTaskRegistrar taskRegistrar = schedulingSpringConfig.getTaskRegistrar();

        tableCron.getContainerDataSource().removeAllItems();
        List<CronTask> cronTaskList = getNullPointerList(() -> taskRegistrar.getCronTaskList());
        for (CronTask task : cronTaskList) {
            Object[] cells = { getTarget(task.getRunnable()), task.getExpression() };
            tableCron.addItem(cells, ++cnt);
        }
        tableCron.setPageLength(cronTaskList.size());

        tableFixedDelay.getContainerDataSource().removeAllItems();
        List<IntervalTask> fixedDelayList = schedulingSpringConfig.getTaskRegistrar().getFixedDelayTaskList();

        for (IntervalTask task : fixedDelayList) {
            Object[] cells = { getTarget(task.getRunnable()), task.getInterval() };
            tableFixedDelay.addItem(cells, ++cnt);
        }
        tableFixedDelay.setPageLength(fixedDelayList.size());

        tableFixedRate.getContainerDataSource().removeAllItems();
        List<IntervalTask> fixedRateTaskList = schedulingSpringConfig.getTaskRegistrar().getFixedRateTaskList();
        for (IntervalTask task : fixedRateTaskList) {
            Object[] cells = { getTarget(task.getRunnable()), task.getInterval() };
            tableFixedRate.addItem(cells, ++cnt);
        }
        tableFixedRate.setPageLength(fixedRateTaskList.size());

        tableTriggred.getContainerDataSource().removeAllItems();
        List<TriggerTask> triggeredTaskList = getNullPointerList(() -> taskRegistrar.getTriggerTaskList());
        for (TriggerTask task : triggeredTaskList) {
            Object[] cells = { getTarget(task.getRunnable()), task.getTrigger().getClass().getName() };
            tableTriggred.addItem(cells, ++cnt);
        }
        tableTriggred.setPageLength(triggeredTaskList.size());
        */
    }

    /*
        private <X> List<X> getNullPointerList(Supplier<List<X>> producer) {
            try {
                return producer.get();
            } catch (NullPointerException npx) {
                return Collections.EMPTY_LIST;
            }
        }
    */
    private String getTarget(Runnable runnable) {
        if (runnable instanceof ScheduledMethodRunnable) {
            ScheduledMethodRunnable smr = (ScheduledMethodRunnable) runnable;
            return smr.getTarget().getClass().getName() + "." + smr.getMethod().getName();
        } else {
            return runnable.getClass().getName();
        }
    }

    public static MenuBar buildMenuBar() {
        MenuBar menubar = new MenuBar();
        menubar.setWidth(100, Unit.PERCENTAGE);

        menubar.addItem("Status", new NavigateCommand("/status"));
        menubar.addItem("External Audit", new NavigateCommand(ExternalAuditView.class));
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
    /**
    ExternalResource prueba= new ExternalResource("paginas/hola.html");    
    Embedded browser = new Embedded("",prueba);
    browser.setType(Embedded.TYPE_BROWSER);   
     */
}
