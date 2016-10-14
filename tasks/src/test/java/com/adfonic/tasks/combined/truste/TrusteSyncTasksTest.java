package com.adfonic.tasks.combined.truste;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.tasks.combined.DeviceIdentifierValidator;
import com.adfonic.tasks.combined.truste.BatchPreferenceService;
import com.adfonic.tasks.combined.truste.TrusteIdTypeMapper;
import com.adfonic.tasks.combined.truste.TrusteSyncTasks;
import com.adfonic.tasks.combined.truste.TrusteUnreachableException;
import com.adfonic.tasks.combined.truste.dao.OptOutServiceDao;
import com.adfonic.tasks.combined.truste.dao.TasksDao;
import com.adfonic.tasks.combined.truste.dto.AdditionalIds;
import com.adfonic.tasks.combined.truste.dto.BatchPreference;

@RunWith(MockitoJUnitRunner.class)
public class TrusteSyncTasksTest {

    @Mock
    private DeviceIdentifierValidator deviceIdentifierValidator;
    @Mock
    private OptOutServiceDao optOutServiceDao;
    @Mock
    private TrusteIdTypeMapper idTypeMapper;
    @Mock
    private BatchPreferenceService batchPreferenceService;
    @Mock
    private TasksDao tasksDao;
    
    @InjectMocks
    private TrusteSyncTasks testObj;
    
    final DateTime now = new DateTime(2014, 5, 14, 14, 59);
    final DateTime lastRun = new DateTime(2012, 12, 12, 12, 59);
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        testObj.batchSize = 10;
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        
        Mockito.when(optOutServiceDao.saveUserPreferences(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(1);
    }
    
    @After
    public void after() {
    	DateTimeUtils.setCurrentMillisSystem();
    }
    
    @Test
    public void testConstructor() {
    	TrusteSyncTasks testObj = new TrusteSyncTasks();
    	Assert.assertNull(testObj.lastRunTime);
    }
    
    @Test
    public void testLastRunTimeIsNullAtVeryFirstTime() throws Exception {
        
        DateTime defaultLastRunTime = now.withTimeAtStartOfDay().minusDays(500);
        testObj.lastRunTime = null;
        Mockito.when(tasksDao.loadLastRunTime()).thenReturn(null);
        
        // act
        testObj.process();
        
        Assert.assertEquals(now, testObj.lastRunTime);
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getCount(defaultLastRunTime, now);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void testProcessUpdatesLastRunTime() throws IOException, TrusteUnreachableException {
    	
    	testObj.lastRunTime = null;
    	Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
    	
    	// act
    	testObj.process();
    	
    	Assert.assertEquals(now, testObj.lastRunTime);
    	Mockito.verify(tasksDao).loadLastRunTime();
    	Mockito.verify(batchPreferenceService).getCount(lastRun, now);
    	Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void expectedChangeAfterWhenTaskDaoFailed() throws IOException, TrusteUnreachableException {
    	
    	DateTime expectedChangeAfter = now.withTimeAtStartOfDay().minusDays(500);
    	testObj.lastRunTime = null;
    	Throwable ex = new IOException("lastRunTime failed");
		Mockito.when(tasksDao.loadLastRunTime()).thenThrow(ex);
    	
    	// act
    	testObj.process();
    	
    	Assert.assertEquals(now, testObj.lastRunTime);
    	Mockito.verify(tasksDao).loadLastRunTime();
		Mockito.verify(batchPreferenceService).getCount(expectedChangeAfter, now);
    	Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void taskDaoNotCalledIfLastRunTimeIsKnown() throws IOException, TrusteUnreachableException {
    	
    	testObj.lastRunTime = lastRun;
    	
    	// act
    	testObj.process();
    	
    	Assert.assertEquals(now, testObj.lastRunTime);
    	Mockito.verify(tasksDao, Mockito.never()).loadLastRunTime();
		Mockito.verify(batchPreferenceService).getCount(lastRun, now);
    	Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    
    @Test
    public void lastRunTimeIsNotUpdatedWhenTrusteUnreachable() throws TrusteUnreachableException {
    	
    	Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
    	Mockito.when(batchPreferenceService.getBatch(Mockito.anyInt(), Mockito.anyInt(), Mockito.<DateTime>any(), Mockito.<DateTime>any())).thenThrow(new TrusteUnreachableException());
    	
    	testObj.lastRunTime = lastRun;
    	
    	// act
    	testObj.process();
    	
    	Assert.assertEquals(lastRun, testObj.lastRunTime);
    	Mockito.verifyZeroInteractions(optOutServiceDao);
    	Mockito.verifyZeroInteractions(tasksDao);
    }
    
    
    @Test
    public void testProcessBatchEmptyList() throws TrusteUnreachableException, IOException {
    	Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
    	Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
    	
        BatchPreference batchPreference = new BatchPreference();
        batchPreference.setAdditionalIds(Collections.<AdditionalIds>emptyList());
        batchPreference.setOptinFlag(true);
        
        List<BatchPreference> list = Arrays.asList(batchPreference);
        Mockito.when(batchPreferenceService.getBatch(0, 10, lastRun, now)).thenReturn(list);
        
        // act
        testObj.process();
        
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getBatch(0, 10, lastRun, now);
        Mockito.verifyZeroInteractions(optOutServiceDao);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }

    @Test
    public void testProcessBatchTwoValidAdditionalids() throws TrusteUnreachableException, IOException {
    	Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
    	Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
    	
    	Mockito.when(idTypeMapper.mapAdfonicIdType("UDID")).thenReturn(1L);
    	Mockito.when(idTypeMapper.mapAdfonicIdType("MAC-SHA1")).thenReturn(2L);
    	
    	Mockito.when(deviceIdentifierValidator.isDeviceIdValid(Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        
        BatchPreference batchPreference = new BatchPreference();
        
        AdditionalIds id1 = new AdditionalIds();
        id1.setIdName("UDID");
        id1.setIdValue("E-7-eaa833f6-25a0710-e1542-02a46ad72");
        
        AdditionalIds id2 = new AdditionalIds();
        id2.setIdName("MAC-SHA1");
        id2.setIdValue("5dc5d98318071f1a8fb78a83837c2c09539aec57");
        
        batchPreference.setAdditionalIds(Arrays.asList(id1, id2));
        batchPreference.setOptinFlag(false);
        
        List<BatchPreference> list = Arrays.asList(batchPreference);
        Mockito.when(batchPreferenceService.getBatch(0, 10, lastRun, now)).thenReturn(list);
        
        testObj.process();
        
        Mockito.verify(optOutServiceDao).saveUserPreferences(//
        		"E-7-eaa833f6-25a0710-e1542-02a46ad72~1"//
        		+"|5dc5d98318071f1a8fb78a83837c2c09539aec57~2"//
        		, false);
        
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getBatch(0, 10, lastRun, now);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void testProcessBatchWithOneInvalidId() throws TrusteUnreachableException, IOException {
    	Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
    	Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
    	
    	Mockito.when(idTypeMapper.mapAdfonicIdType("UDID")).thenReturn(1L);
    	Mockito.when(idTypeMapper.mapAdfonicIdType("MAC-SHA1")).thenReturn(13L);
    	
    	Mockito.when(deviceIdentifierValidator.isDeviceIdValid(Mockito.anyString(), Mockito.eq(1L))).thenReturn(true);
    	Mockito.when(deviceIdentifierValidator.isDeviceIdValid(Mockito.anyString(), Mockito.eq(13L))).thenReturn(false);
    	
    	BatchPreference batchPreference = new BatchPreference();
    	
    	AdditionalIds id1 = new AdditionalIds();
    	id1.setIdName("UDID");
    	id1.setIdValue("E-7-eaa833f6-25a0710-e1542-02a46ad72");
    	
    	AdditionalIds id2 = new AdditionalIds();
    	id2.setIdName("MAC-SHA1");
    	id2.setIdValue("5dc5d98-INVALID-09539aec57");
    	
    	batchPreference.setAdditionalIds(Arrays.asList(id1, id2));
    	batchPreference.setOptinFlag(false);
    	
    	List<BatchPreference> list = Arrays.asList(batchPreference);
    	Mockito.when(batchPreferenceService.getBatch(0, 10, lastRun, now)).thenReturn(list);
    	
    	// act
    	testObj.process();
    	
    	Mockito.verify(optOutServiceDao).saveUserPreferences(//
    			"E-7-eaa833f6-25a0710-e1542-02a46ad72~1"//
    			, false);
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getBatch(0, 10, lastRun, now);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void testProcessBatchWithUnknownIdTypes() throws TrusteUnreachableException, IOException {
    	Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
    	Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
    	Mockito.when(idTypeMapper.mapAdfonicIdType("UDID")).thenReturn(1L);
    	Mockito.when(idTypeMapper.mapAdfonicIdType("MAC-SHA1")).thenReturn(0L);
    	Mockito.when(deviceIdentifierValidator.isDeviceIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
        
        BatchPreference batchPreference = new BatchPreference();
        
        AdditionalIds id1 = new AdditionalIds();
        id1.setIdName("UDID");
        id1.setIdValue("E-7-eaa833f6-25a0710-e1542-02a46ad72");
        
        AdditionalIds unknownType = new AdditionalIds();
        unknownType.setIdName("MAC-SHA1");
        unknownType.setIdValue("5dc5d98318071f1a8fb78a83837c2c09539aec57");
        
        batchPreference.setAdditionalIds(Arrays.asList(id1, unknownType));
        batchPreference.setOptinFlag(false);
        
        List<BatchPreference> list = Arrays.asList(batchPreference);
        Mockito.when(batchPreferenceService.getBatch(0, 10, lastRun, now)).thenReturn(list);
        
        testObj.process();
        
        Mockito.verify(optOutServiceDao).saveUserPreferences(//
        		"E-7-eaa833f6-25a0710-e1542-02a46ad72~1"//
        		, false);
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getBatch(0, 10, lastRun, now);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void testProcessBatchWithOnlyUnknownIdTypes() throws TrusteUnreachableException, IOException {
        Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
        Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
        Mockito.when(idTypeMapper.mapAdfonicIdType("UDID")).thenReturn(1L);
        Mockito.when(idTypeMapper.mapAdfonicIdType("MAC-SHA1")).thenReturn(0L);
        Mockito.when(deviceIdentifierValidator.isDeviceIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
        
        BatchPreference batchPreference = new BatchPreference();
        
        AdditionalIds id1 = new AdditionalIds();
        id1.setIdName("MAC-SHA1");
        id1.setIdValue("very invalid id");
        
        AdditionalIds unknownType = new AdditionalIds();
        unknownType.setIdName("MAC-SHA1");
        unknownType.setIdValue("5dc5d98318071f1a8fb78a83837c2c09539aec57");
        
        batchPreference.setTpid("tpid234");
        batchPreference.setAdditionalIds(Arrays.asList(id1, unknownType));
        batchPreference.setOptinFlag(false);
        
        List<BatchPreference> list = Arrays.asList(batchPreference);
        Mockito.when(batchPreferenceService.getBatch(0, 10, lastRun, now)).thenReturn(list);
        
        testObj.process();
        
        Mockito.verifyZeroInteractions(optOutServiceDao);
        Mockito.verify(tasksDao).loadLastRunTime();
        Mockito.verify(batchPreferenceService).getBatch(0, 10, lastRun, now);
        Mockito.verify(tasksDao).storeLastRunTime(now);
    }
    
    @Test
    public void testWhenDaoThrowsRuntimeExceptions() throws Exception{
        
        Mockito.when(tasksDao.loadLastRunTime()).thenThrow(new RuntimeException("simulating"));
        Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
        
        // act
        testObj.process();
    }
    
    @Test
    public void testWhenBatchPreferenceServiceThrowsException2() throws Exception{
        
        Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
        Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
        Mockito.when(batchPreferenceService.getCount(Mockito.any(DateTime.class), Mockito.any(DateTime.class))).thenThrow(new RuntimeException("simulating"));
        
        // act
        testObj.process();
    }
    
    @Test
    public void testWhenBatchPreferenceServiceThrowsException() throws Exception{
        
        Mockito.when(tasksDao.loadLastRunTime()).thenReturn(lastRun);
        Mockito.when(batchPreferenceService.getCount(lastRun, now)).thenReturn(10);
        Mockito.when(batchPreferenceService.getBatch(Mockito.anyInt(), Mockito.anyInt(), Mockito.<DateTime>any(), Mockito.<DateTime>any())).thenThrow(new RuntimeException("simulating"));
        
        // act
        testObj.process();
    }
}
