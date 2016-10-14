package com.adfonic.tasks.combined.truste;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.tasks.combined.DeviceIdentifierValidator;
import com.adfonic.tasks.combined.truste.dao.OptOutServiceDao;
import com.adfonic.tasks.combined.truste.dao.TasksDao;

@Ignore("rely on local mysql")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/adfonic-tasks-test-context.xml" })
public class TrusteSyncTasksIntTest {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private OptOutServiceDao optOutServiceDaoMock;
    @Autowired
    private DeviceIdentifierValidator deviceIdentifierValidatorMock;

    @Autowired
    private TasksDao tasksDao;
    @Autowired
    private BatchPreferenceService batchPreferenceService;
    @Autowired
    private TrusteSyncTasks testObj;

    @Autowired
    @Qualifier("muidDataSource")
    private DataSource dataSource;
    JdbcTemplate jT;

    @Autowired
    @Value("${muid.jdbc.url}")
    String url;

    final DateTime now = new DateTime(2014, 6, 4, 14, 59);

    @Before
    public void before() {
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());

        jT = new JdbcTemplate(dataSource);
        removeFromProcessingQ();
    }

    @After
    public void after() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void test() throws TrusteUnreachableException, ClientProtocolException, IOException {

        // mock HttpClient
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        batchPreferenceService.httpClient = httpClient;
        HttpResponse countResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        StringEntity entity = new StringEntity("2");
        countResponse.setEntity(entity);
        HttpResponse jsonResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        StringEntity json = new StringEntity(sample());
        jsonResponse.setEntity(json);
        Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(countResponse, jsonResponse);

        // make sure queue is empty
        Assert.assertEquals(0, countProcessingQ());
        Assert.assertNotNull(testObj);

        DateTime changeAfter = now.minusDays(30);
        testObj.lastRunTime = changeAfter;

        LOG.info("start test");

        // act
        testObj.process();

        // expects 2 ids in the table
        Assert.assertEquals(2, countProcessingQ());
        DateTime lastRunTime = tasksDao.loadLastRunTime();
        Assert.assertEquals(now, lastRunTime);

    }

    private String sample() {
        String sample = "[\n"
                + "{\"optinFlag\":false,\n"
                + "\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\n"
                + "\"additionalIds\":[\n"
                + "{\"idName\":\"anid\",\"idValue\":\"6633bb7b-c967-4\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\"changedDate\":null,\"createdDate\":null},\n"
                + "{\"idName\":\"anid-sha1\",\"idValue\":\"059c9f83872d671284e4dd460512b7828521a7f2\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\"changedDate\":null,\"createdDate\":null},\n"
                + "{\"idName\":\"anid-md5\",\"idValue\":\"5631ae21ee39fec431d64a3d432f460d\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\n"
                + "\"changedDate\":null,\"createdDate\":null}\n"
                + "],\"changedDate\":\"2014-06-03 18:39:41.876\",\n"
                + "\"createdDate\":\"2014-06-03 18:39:41.874\",\n"
                + "\"company\":\"AdFonic\",\n"
                + "\"appID\":\"1fpuama23p81s\"},\n"
                + "{\"optinFlag\":false,\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\n"
                + "\"additionalIds\":[{\"idName\":\"anid\",\"idValue\":\"6633bb7b-c967-4\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\"changedDate\":null,\"createdDate\":null},{\"idName\":\"anid-sha1\",\"idValue\":\"059c9f83872d671284e4dd460512b7828521a7f2\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\"changedDate\":null,\"createdDate\":null},{\"idName\":\"anid-md5\",\"idValue\":\"5631ae21ee39fec431d64a3d432f460d\",\"tpid\":\"9bcaeaa9-0033-3b29-b3df-a3772b78a49f\",\"changedDate\":null,\"createdDate\":null}],\n"
                + "\"changedDate\":\"2014-06-03 18:39:39.749\",\"createdDate\":\"2014-06-03 18:39:39.731\",\"company\":\"AdFonic\",\"appID\":\"TRUSTE.GLOBAL\"}\n" + "]";

        return sample;
    }

    private void removeFromProcessingQ() {
        String sql = "delete from weve.muid_option_processing_queue";
        jT.update(sql);
    }

    private int countProcessingQ() {
        String sql = "select count(*) from weve.muid_option_processing_queue";
        int count = jT.queryForObject(sql, Integer.class);
        return count;
    }
}
