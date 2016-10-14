package com.adfonic.tasks;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public abstract class SpringTaskBase {

    private static final Logger LOG = LoggerFactory.getLogger(SpringTaskBase.class);

    private ApplicationContext appContext;

    /*
    protected SpringTaskBase(String... contextsNeeded) {
        LOG.debug("Initializing the ApplicationContext");
        try {
            SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

            bindStandardProperties(builder);

            // Give subclasses an opportunity to bind any additional resources
            bindResources(builder);
        } catch (javax.naming.NamingException e) {
            throw new RuntimeException("Unable to set up mock JNDI context", e);
        }
        
        // Set up the full list of contexts to be used
        Set<String> contexts = new LinkedHashSet<String>();
        contexts.add("adfonic-tasks-context.xml");
        contexts.add("adfonic-toolsdb-context.xml");
        if (contextsNeeded != null) {
            for (String additional : contextsNeeded) {
                contexts.add(additional);
            }
        }

        // Now just log which contexts are being included
        for (String context : contexts) {
            LOG.debug("Including context: {}", context);
        }

        // Create the application context
        appContext = new ClassPathXmlApplicationContext(contexts.toArray(new String[contexts.size()]));

        // Autowire ourself
        autowire(this);

        LOG.debug("Done initializing");
    }
    */
    /** Override this method if you need to bind your application's own
        custom resources.
    
    protected void bindResources(SimpleNamingContextBuilder builder) {
    }

    public interface ResourceBinder {
        void bindResources(SimpleNamingContextBuilder builder);
    }

    public static void runBean(String... contextsNeeded) {
        runBean(null, null, contextsNeeded);
    }

    public static void runBean(ResourceBinder resourceBinder, String... contextsNeeded) {
        runBean(null, resourceBinder, contextsNeeded);
    }
    */
    public static void runBean(Class clazz, Class... configurationClasses) {
        LOG.debug("Initializing the ApplicationContext");
        GenericApplicationContext springContext = buildSpringContext(clazz, configurationClasses);
        runBean(clazz, springContext);
    }

    public static void runBean(Class clazz, String... contextsNeeded) {
        LOG.debug("Initializing the ApplicationContext");
        /*
        try {
            SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

            bindStandardProperties(builder);

            if (resourceBinder != null) {
                resourceBinder.bindResources(builder);
            }
        } catch (javax.naming.NamingException e) {
            throw new RuntimeException("Unable to set up mock JNDI context", e);
        }
        */
        // Create the application context
        GenericApplicationContext appContext = buildSpringContext(clazz, contextsNeeded);
        runBean(clazz, appContext);
    }

    private static void runBean(Class clazz, GenericApplicationContext appContext) {
        LOG.debug("Done initializing ApplicationContext");

        boolean runnable = false;
        Object bean = null;
        if (clazz != null) {
            bean = appContext.getBean(clazz);
            if (bean instanceof Runnable) {
                runnable = true;
                LOG.debug("Invoking {}.run()", clazz.getName());
                ((Runnable) bean).run();
                LOG.debug("{}.run() returned", clazz.getName());
            }
        }

        if (!runnable) {
            LOG.debug("Running a sleep loop to let the context run...");
            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted");
                    break;
                }
            }
        }
        LOG.debug("Returning from runBean");
    }

    private static GenericApplicationContext buildSpringContext(Class<?> clazz, Class<?>... configurationClasses) {
        AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext();
        for (Class<?> configurationClass : configurationClasses) {
            springContext.register(configurationClass);
        }

        if (clazz != null) {
            LOG.debug("Registering bean: {}", clazz.getName());
            AnnotatedGenericBeanDefinition beanDef = new AnnotatedGenericBeanDefinition(clazz);
            springContext.registerBeanDefinition(ClassUtils.getShortClassName(clazz), beanDef);
        }
        //StandardEnvironment environment = new StandardEnvironment();
        //environment.getPropertySources().addLast(null);
        //springContext.setEnvironment(mockEnvironment);

        springContext.refresh();
        return springContext;
    }

    private static GenericApplicationContext buildSpringContext(Class clazz, String... contextsNeeded) {
        GenericApplicationContext appContext = new GenericApplicationContext();

        // Load all of the app context XML resources
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
        for (String context : contextsNeeded) {
            LOG.info("Loading context: {}", context);
            xmlReader.loadBeanDefinitions(new ClassPathResource(context));
        }

        if (clazz != null) {
            LOG.debug("Registering bean: {}", clazz.getName());
            AnnotatedGenericBeanDefinition beanDef = new AnnotatedGenericBeanDefinition(clazz);
            appContext.registerBeanDefinition(ClassUtils.getShortClassName(clazz), beanDef);
        }

        appContext.refresh();
        return appContext;
    }

    /*
        private static void bindStandardProperties(SimpleNamingContextBuilder builder) {
            // Wire up the tasks, toolsdb, and reportingdb properties...which
            // actually all point to the same properties file currently.
            builder.bind("java:comp/env/adfonicTasksProperties", System.getProperty("adfonicTasksProperties", "file:/usr/local/adfonic/config/adfonic-tasks.properties"));
            builder.bind("java:comp/env/adfonicToolsdbProperties", System.getProperty("adfonicToolsdbProperties", "file:/usr/local/adfonic/config/adfonic-tasks.properties"));
            builder.bind("java:comp/env/adfonicReportingdbProperties", System.getProperty("adfonicReportingdbProperties", "file:/usr/local/adfonic/config/adfonic-tasks.properties"));
        }
    */
    /*
    protected final void autowire(Object obj) {
        LOG.debug("Autowiring: {}", obj.getClass().getName());
        appContext.getAutowireCapableBeanFactory().autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
    */
    public static String loadResource(String templateLocation) throws java.io.IOException {
        ClassPathResource resource = new ClassPathResource(templateLocation);
        InputStream inputStream = resource.getInputStream();
        try {
            return IOUtils.toString(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    /*
    protected ApplicationContext getApplicationContext() {
        return appContext;
    }
    */
}
