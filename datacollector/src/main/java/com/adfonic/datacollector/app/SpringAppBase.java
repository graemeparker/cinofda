package com.adfonic.datacollector.app;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringAppBase {

    private static final transient Logger LOG = Logger.getLogger(SpringAppBase.class.getName());

    private final ClassPathXmlApplicationContext appContext;

    public SpringAppBase() {
        this(null);
    }

    public SpringAppBase(String... contextsNeeded) {
        LOG.info("Initializing the ApplicationContext");
        /*
        try {
            SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        } catch (javax.naming.NamingException e) {
            throw new UnsupportedOperationException("Unable to set up mock JNDI context", e);
        }
        */
        // Set up the full list of contexts to be used
        List<String> contexts = new ArrayList<String>();
        if (contextsNeeded != null) {
            for (String additional : contextsNeeded) {
                contexts.add(additional);
            }
        }

        // Now just log which contexts are being included
        for (String context : contexts) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Including context: " + context);
            }
        }

        // Create the application context
        appContext = new ClassPathXmlApplicationContext(contexts.toArray(new String[contexts.size()]));

        // Autowire ourself
        LOG.info("Autowiring");
        appContext.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);

        LOG.info("Done initializing");
    }

    public ApplicationContext getApplicationContext() {
        return appContext;
    }

    protected final void closeApplicationContext() {
        LOG.info("Stopping the ApplicationContext");
        appContext.stop();
        LOG.info("Closing the ApplicationContext");
        appContext.close();
        LOG.info("Destroying the ApplicationContext");
        appContext.destroy();
    }
}
