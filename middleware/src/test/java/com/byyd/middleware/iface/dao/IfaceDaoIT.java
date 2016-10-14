package com.byyd.middleware.iface.dao;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static com.byyd.middleware.iface.dao.SortOrder.desc;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Creative;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.service.CreativeManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class IfaceDaoIT {
    
    @Autowired
    CreativeManager creativeManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testDottedNotationSorting() {
        try {
            Sorting sort = null;
            List<Creative> creatives = null;
            
            sort = new Sorting(asc(Creative.class, "campaign.advertiser.name"));
            creatives = creativeManager.getAllCreatives(new CreativeFilter().setDestinationContains("test"), sort);
            
            sort = new Sorting(asc(Creative.class, "campaign.advertiser.company.accountManager.email"));
            creatives = creativeManager.getAllCreatives(new CreativeFilter().setDestinationContains("test"), sort);

            sort = new Sorting(asc(Creative.class, "destination.data"));
            creatives = creativeManager.getAllCreatives(new CreativeFilter().setDestinationContains("test"), sort);

            for(Creative creative : creatives) {
                System.out.println(creative.getName());
            }
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
        
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testDottedNotationSortingDeux() {
        try {
            Sorting sort = null;
            
            sort = new Sorting(asc(Creative.class, "campaign.advertiser.name"));
            System.out.println(sort.toString(true));
            
            sort = new Sorting(asc(Creative.class, "campaign.advertiser.company.accountManager.email"));
            System.out.println(sort.toString(true));

            sort = new Sorting(desc(Creative.class, "campaign.advertiser.company.accountManager"));
            System.out.println(sort.toString(true));

            sort = new Sorting(asc(Creative.class, "campaign.advertiser.company"));
            System.out.println(sort.toString(true));

            sort = new Sorting(asc(Creative.class, "destination.data"));
            System.out.println(sort.toString(true));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
}
