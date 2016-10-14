package com.adfonic.ddr.deviceatlas;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.ddr.DdrService;
import com.adfonic.test.AbstractAdfonicTest;

/**
 * Integration test (execute only in integration-phase maven phase)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/deviceatlas-s3-context.xml" })
public class DdrDeviveAtlasS3ServiceImplIT extends AbstractAdfonicTest {
    @Autowired
    protected ApplicationContext ac;
    
    @Test
    public void testNewDdrServiceGetDdrProperties(){
        DdrService ddrService = (DdrService) ac.getBean("DdrDeviceAtlasS3Service");
        testDdrServcie(ddrService);
    }

    private void testDdrServcie(DdrService ddrService) {
        Map<String, String> ddrProperties = ddrService.getDdrProperties("Mozilla/5.0 (iPad; U; CPU OS 4_3_2 like Mac OS X; ru-ru) AppleWebKit/532.9 (KHTML, like Gecko) Mobile/8H7 Safari/532.9");
        assertNotNull("ddrProperties are null", ddrProperties);
        assertTrue("ddrProperties size incorrect", ddrProperties.size()>2);
    }
}