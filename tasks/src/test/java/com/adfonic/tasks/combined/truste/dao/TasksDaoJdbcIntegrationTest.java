package com.adfonic.tasks.combined.truste.dao;

import java.io.IOException;

import org.apache.commons.dbcp.BasicDataSource;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore("rely on local database")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/tasks-dao-test-context.xml" })
public class TasksDaoJdbcIntegrationTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private BasicDataSource dataSource;
    JdbcTemplate jdbcTemplate;

    private TasksDaoJdbc testObj;

    @Before
    public void before() {
        testObj = new TasksDaoJdbc();
        jdbcTemplate = new JdbcTemplate(dataSource);
        testObj.jdbcTemplate = this.jdbcTemplate;
    }

    @Test
    public void testStoreAndLoadLastRunTime() throws IOException {
        Assert.assertNotNull(dataSource);

        DateTime lastRunTime = new DateTime(2001, 1, 1, 13, 59);

        for (int j = 0; j < 10; j++) {
            testObj.storeLastRunTime(lastRunTime);

            DateTime loaded = testObj.loadLastRunTime();
            Assert.assertEquals(loaded, lastRunTime);

            lastRunTime = lastRunTime.plusHours(1);
        }
    }

    @Test
    public void testInsertRecordIntoPropertiesIfMissing() throws IOException {

        int affected = jdbcTemplate.update("delete from PROPERTIES where PROPERTY_NAME='truste-sync-tasks.last-run-time'");
        logger.debug("affected {}", affected);

        DateTime lastRunTime = new DateTime(2001, 1, 1, 13, 59);
        testObj.storeLastRunTime(lastRunTime);

        DateTime loaded = testObj.loadLastRunTime();
        Assert.assertEquals(loaded, lastRunTime);
    }

}
