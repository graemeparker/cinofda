package com.byyd.middleware.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/h2-db-context.xml" })
public class AdfonicBeanDispatcherTest {

    @Test
    public void testGetInstance() {
        assertNotNull(AdfonicBeanDispatcher.getInstance());
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testNotExistingBeanViaAdfonicBeanDispatcher() {
        AdfonicBeanDispatcher.getBean("notExistingBean");
    }

    @Test
    public void testEntityManagerFactoryBeanViaAdfonicBeanDispatcher() {
        EntityManagerFactoryInfo bean = (EntityManagerFactoryInfo)AdfonicBeanDispatcher.getBean("entityManagerFactory");
        assertNotNull(bean);
        assertEquals("adfonic-domain", bean.getPersistenceUnitName());
    }

}
