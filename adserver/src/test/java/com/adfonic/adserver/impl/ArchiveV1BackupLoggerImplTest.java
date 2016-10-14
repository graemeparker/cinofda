package com.adfonic.adserver.impl;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;
import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.financial.FinancialCalc;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveV1BackupLoggerImplTest {

    private ArchiveV1BackupLoggerImpl testObj;
    
    @Mock
    private TargetingContext context;

    @Mock
    private AdserverDomainCache adserverDomainCache;
    
    @Test
    public void testChooseFinancialCase() throws IOException {
        testObj = new ArchiveV1BackupLoggerImpl(false, "fileName", ".yyyy-MM-dd-HH", "100MB", 2147483647, -1,"","","","","","",false,true,"","","","","","");
        
        // isSaaS, isMarginShare, isRtb, bidType
//        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, true, true, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("MarginShareCPC_RTB", testObj.chooseCase(false, true, true, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, false, true, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("ManagedCPC_RTB", testObj.chooseCase(false, false, true, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, true, false, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("MarginShareCPC_NONrtb", testObj.chooseCase(false, true, false, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, false, false, BidType.CPC).getClass().getSimpleName());
        Assert.assertEquals("ManagedCPC_NONrtb", testObj.chooseCase(false, false, false, BidType.CPC).getClass().getSimpleName());
        
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, true, true, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("MarginShareCPM_RTB", testObj.chooseCase(false, true, true, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, false, true, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("ManagedCPM_RTB", testObj.chooseCase(false, false, true, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, true, false, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("MarginShareCPM_NONrtb", testObj.chooseCase(false, true, false, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("LicenseCPM_RTB", testObj.chooseCase(true, false, false, BidType.CPM).getClass().getSimpleName());
        Assert.assertEquals("ManagedCPM_NONrtb", testObj.chooseCase(false, false, false, BidType.CPM).getClass().getSimpleName());
    }

    @Test
    public void testInvalidCase() throws IOException {
        testObj = new ArchiveV1BackupLoggerImpl(false, "fileName", ".yyyy-MM-dd-HH", "100MB", 2147483647, -1,"","","","","","",false,true,"","","","","","");
        
        FinancialCalc calc = testObj.chooseCase(false, false, false, BidType.CPI);
        Assert.assertNull(calc);
        
    }
    
    @Test
    public void addCommonValuesTakesOperatorFromContext() {
        OperatorDto operatorDto = new OperatorDto();
        operatorDto.setId(123L);
        
        Mockito.when(context.getAttribute(TargetingContext.OPERATOR, OperatorDto.class)).thenReturn(operatorDto);
        
        AdEvent ae = new AdEvent(); 
        AdAction outcome = AdAction.UNFILLED_REQUEST; 
        String reason = "timeout";
        Date eventTime = new Date(); 
        Impression impression = null;
        
        ArchiveV1BackupLoggerImpl.addCommonValues(ae, outcome, reason, eventTime, context, impression);
        Assert.assertEquals(123L, ae.getOperatorId().longValue());
        
        // dont replace if already set
        ae.setOperatorId(666L);
        ArchiveV1BackupLoggerImpl.addCommonValues(ae, outcome, reason, eventTime, context, impression);
        Assert.assertEquals(666L, ae.getOperatorId().longValue());
    }
    
    @Test
    public void getAdSpaceFromCache() throws IOException {
        testObj = new ArchiveV1BackupLoggerImpl(false, "fileName", ".yyyy-MM-dd-HH", "100MB", 2147483647, -1,"","","","","","",false,true,"","","","","","");
        
        final Long adSpaceId = 4567L;
        Impression impression = Mockito.mock(Impression.class);
        AdSpaceDto adSpace = Mockito.mock(AdSpaceDto.class);
        Mockito.when(impression.getAdSpaceId()).thenReturn(adSpaceId);
        
        Mockito.when(context.getAdserverDomainCache()).thenReturn(adserverDomainCache);
        Mockito.when(context.getAdSpace()).thenReturn(null);
        Mockito.when(adserverDomainCache.getAdSpaceById(adSpaceId)).thenReturn(adSpace);
        
        // act
        AdSpaceDto result = testObj.getAdSpaceFromContextOrCache(impression, context);
        
        Assert.assertSame(adSpace, result);
    }
    @Test
    public void getAdSpaceFromContext() throws IOException {
        testObj = new ArchiveV1BackupLoggerImpl(false, "fileName", ".yyyy-MM-dd-HH", "100MB", 2147483647, -1,"","","","","","",false,true,"","","","","","");
        
        final Long adSpaceId = 4567L;
        Impression impression = Mockito.mock(Impression.class);
        AdSpaceDto adSpace = Mockito.mock(AdSpaceDto.class);
        Mockito.when(impression.getAdSpaceId()).thenReturn(adSpaceId);
        
        Mockito.when(context.getAdSpace()).thenReturn(adSpace);
        
        // act
        AdSpaceDto result = testObj.getAdSpaceFromContextOrCache(impression, context);
        
        Assert.assertSame(adSpace, result);
        Mockito.verifyZeroInteractions(adserverDomainCache);
        Mockito.verify(context, Mockito.never()).getAdserverDomainCache();
    }
}
