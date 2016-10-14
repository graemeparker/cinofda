package com.byyd.middleware.auditlog.listener;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Segment;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.auditlog.config.AuditLogConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-context-auditlog-tests-configuration.xml"})
public class AuditLogJpaListenerMultithreadTest extends AbstractAdfonicTest {
    
    private static final Integer MAX_THREADS = 1;
    
    @Autowired
    AuditLogJpaListener auditLogJpaListener;
    
    @Autowired
    private AuditLogConfig auditLogConfig;
    
    @Autowired
    private AuditLogTestService auditLogTestService;

    @Test
    public void testAuditLogJpaListenerMultithread() throws InterruptedException, ExecutionException{
        // Defining the callable object
        Callable<Boolean> myCallable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return createEntities();
            }
        };
        
        // Execeute all threads
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
        for (int cnt=0; cnt<MAX_THREADS; cnt++){
            futures.add(executor.submit(myCallable));
        }
        
        // Wait until all finished
        while (executor.getCompletedTaskCount() < MAX_THREADS) {
            Thread.sleep(500);               
        }
        
        for(Future<Boolean> future : futures){
            assertTrue("The termination of one thread was incorrect. Check log to look up the generated exception stack trace.", future.get());
        }
        executor.shutdown();
    }
    
    public Boolean createEntities() {
        boolean result = true;
        Campaign campaign = null;
        Segment segment = null;

        try {
            // Setting context info
            AdfonicUser adfonicUser = auditLogTestService.getAdfonicUser();
            adfonicUser.setFirstName(Thread.currentThread().getName() + " " + adfonicUser.getFirstName());
            auditLogJpaListener.setContextInfo(null, adfonicUser);
            
            Advertiser advertiser = auditLogTestService.getAdvertiserInstance();
            
            // Campaign entity
            campaign = auditLogTestService.getCampaignInstance(advertiser);
            
            // Segment entity
            segment = auditLogTestService.getSegmentInstance(advertiser);
        }catch(Throwable t){
            t.printStackTrace();
            result = false;
        }finally{
            // Delete campaign and all objects
            try {
                auditLogTestService.deleteNewlyCreatedTestEntities(campaign, segment, null);
            }catch(Throwable t){
                t.printStackTrace();
                result = false;
            }
            
            // Unsetting context info
            auditLogJpaListener.cleanContextInfo();
        }
        return result;
    }
}
