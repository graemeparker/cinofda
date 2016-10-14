package com.adfonic.test;

import java.util.*;
import java.util.logging.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public abstract class SpringTestBase {
    protected ApplicationContext appContext;
    protected Logger logger = Logger.getLogger(getClass().getName());

    protected SpringTestBase() {
        this(null);
    }
    
    protected SpringTestBase(String[] contextsNeeded) {
        logger.info("Initializing the ApplicationContext");

	try {
	    SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
	    builder.bind("java:comp/env/adfonicOlapTestProperties", 
			 "file:///usr/local/adfonic/config/adfonic-olap-test.properties");
	} catch (javax.naming.NamingException e) { 
	    // Um.
	}

        // Set up the full list of contexts to be used
        Set<String> contexts = new LinkedHashSet<String>();
        contexts.add("adfonic-olap-test-context.xml");
        if (contextsNeeded != null) {
            for (String additional : contextsNeeded) {
                contexts.add(additional);
            }
        }
        
        // Now just log which contexts are being included
        for (String context : contexts) {
            logger.fine("Including context: " + context);
        }
        
        // Create the application context
        appContext = new ClassPathXmlApplicationContext(contexts.toArray(new String[0]));

        // Autowire ourself
        logger.info("Autowiring");
        appContext.getAutowireCapableBeanFactory()
            .autowireBeanProperties(this,
                                    AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                    false);
        
        logger.info("Done initializing");
    }
}
