package com.byyd.middleware.iface.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.TransparentNetwork_;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-db-context.xml"})
public class TestFetchStrategyBuilder {
    @Test
    public void test() {
        FetchStrategy fs = new FetchStrategyBuilder()
            .addLeft(Campaign_.segments)
            .addLeft(Campaign_.advertiser)
            .addLeft(Campaign_.defaultLanguage)
            .addLeft(Campaign_.timePeriods)
            .remove(Campaign_.timePeriods)
            .addLeft(Campaign_.transparentNetworks)
            .addLeft(TransparentNetwork_.rateCardMap)
            .addLeft(Campaign_.currentBid)
            .addInner(Advertiser_.company)
            .addRight(Company_.users)
            .build();

        assertTrue(fs instanceof FetchStrategyImpl);
        
        FetchStrategyImpl impl = (FetchStrategyImpl)fs;
        List<String> fields = impl.getEagerlyLoadedFieldsForClass(Campaign.class);
        assertTrue(fields.contains("segments"));
        assertTrue(fields.contains("advertiser"));
        assertTrue(fields.contains("defaultLanguage"));
        assertFalse(fields.contains("timePeriods"));
        assertTrue(fields.contains("transparentNetworks"));
        assertTrue(fields.contains("currentBid"));

        assertEquals(JoinType.LEFT, impl.getJoinType(Campaign.class, "currentBid"));
        assertEquals(JoinType.INNER, impl.getJoinType(Advertiser.class, "company"));
        assertEquals(JoinType.RIGHT, impl.getJoinType(Company.class, "users"));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRebuild() {
        FetchStrategyBuilder builder = new FetchStrategyBuilder()
            .addLeft(Campaign_.segments)
            .addLeft(Campaign_.advertiser);
        builder.build();
        builder.build();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testModifyAfterBuild() {
        FetchStrategyBuilder builder = new FetchStrategyBuilder()
            .addLeft(Campaign_.segments)
            .addLeft(Campaign_.advertiser);
        builder.build();
        builder.addLeft(Campaign_.creatives);
    }
}
