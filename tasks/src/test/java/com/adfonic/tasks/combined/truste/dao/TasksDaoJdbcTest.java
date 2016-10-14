package com.adfonic.tasks.combined.truste.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class TasksDaoJdbcTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private TasksDaoJdbc testObj = new TasksDaoJdbc();
    
    @Test
    public void testLoadNULL() throws IOException {
        
        List<String> list = new ArrayList<>();
        list.add(null);
        
        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.any(Object[].class), Mockito.eq(String.class))).thenReturn(list);
        testObj.jdbcTemplate = jdbcTemplate;
        
        // act
        DateTime result = testObj.loadLastRunTime();
        
        Assert.assertNull(result);
    }

    @Test
    public void testLoadEmptyList() throws IOException {
        
        List<String> list = Arrays.asList();
        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.any(Object[].class), Mockito.eq(String.class))).thenReturn(list);
        testObj.jdbcTemplate = jdbcTemplate;
        
        // act
        DateTime result = testObj.loadLastRunTime();
        
        Assert.assertNull(result);
    }
    
}
