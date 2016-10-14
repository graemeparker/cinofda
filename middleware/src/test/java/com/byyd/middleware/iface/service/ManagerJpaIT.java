package com.byyd.middleware.iface.service;

import static com.byyd.middleware.iface.dao.SortOrder.desc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Advertiser;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class ManagerJpaIT {
    
    @Autowired
    CreativeManager creativeManager;    

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testObjectById() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);
        Advertiser advertiser = creativeManager.getObjectById(Advertiser.class, 1L, fs);
        System.out.println(advertiser.getName() + " - " + advertiser.getCompany().getName());
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testObjectsByIds() {
           FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Advertiser.class, "company", JoinType.INNER);
        List<Long> ids = new ArrayList<Long>();
        for(long id = 1;id <= 10L;id++) {
            ids.add(id);
        }
        List<Advertiser> advertisers = creativeManager.getObjectsByIds(Advertiser.class, ids, new Sorting(desc("name")), fs);
        for(Advertiser advertiser : advertisers) {
            System.out.println(advertiser.getName() + " - " + advertiser.getCompany().getName());
        }
    }
}
