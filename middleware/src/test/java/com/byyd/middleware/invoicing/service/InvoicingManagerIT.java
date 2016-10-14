package com.byyd.middleware.invoicing.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.TransactionNotification;
import com.adfonic.domain.User;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.UserManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class InvoicingManagerIT {
    
    @Autowired
    private InvoicingManager invoicingManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private AdvertiserManager advertiserManager;
    

    //----------------------------------------------------------------------------------------------------------------
    @Test
    public void testTransactionNotification() {
        String reference = "test reference";
        User user = userManager.getUserById(8L);
        Advertiser advertiser = advertiserManager.getAdvertiserById(8L);
        BigDecimal amount = new BigDecimal("10.00");
        
        TransactionNotification transactionNotification = null;
        
        try {
            transactionNotification = invoicingManager.newTransactionNotification(advertiser, user, amount, reference);
            assertNotNull(transactionNotification);
            long id = transactionNotification.getId();
            assertTrue(id > 0);
            assertEquals(advertiser, transactionNotification.getAdvertiser());
            assertEquals(user, transactionNotification.getUser());
            assertEquals(amount, transactionNotification.getAmount());
            assertEquals(reference, transactionNotification.getReference());
            
            transactionNotification = invoicingManager.getTransactionNotificationById(id);
            assertNotNull(transactionNotification);
            assertEquals(id, transactionNotification.getId());
            
            transactionNotification = invoicingManager.getTransactionNotificationById(Long.toString(id));
            assertNotNull(transactionNotification);
            assertEquals(id, transactionNotification.getId());
            
        }
        catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            //companyManager.delete(transactionNotification);
            //assertNull(companyManager.getTransactionNotificationById(transactionNotification.getId()));
        }
    }

}
