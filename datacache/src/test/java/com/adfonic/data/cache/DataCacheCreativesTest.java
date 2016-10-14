package com.adfonic.data.cache;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.data.cache.test.CreativesTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test-cache-scheduling-context.xml"})
public class DataCacheCreativesTest {

    
    @Autowired
    private CreativesTest creativesTest;
   	
    @Ignore
    @Test
    public void compareTest() throws Exception {
    	
    	System.out.println(creativesTest.creativesTest());

    }
    
}
