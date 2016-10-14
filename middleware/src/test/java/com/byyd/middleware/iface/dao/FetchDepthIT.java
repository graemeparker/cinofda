package com.byyd.middleware.iface.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.publication.service.PublicationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class FetchDepthIT {
    @Autowired
    private UserManager userManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private CreativeManager creativeManager;

    @Test
    public void testUser() {
        FetchStrategyImpl fetchStrategy = new FetchStrategyImpl();
        fetchStrategy.addEagerlyLoadedFieldForClass(User.class, "company", JoinType.INNER);
        fetchStrategy.addEagerlyLoadedFieldForClass(Company.class, "publisher", JoinType.LEFT);
        fetchStrategy.addEagerlyLoadedFieldForClass(Publisher.class, "account", JoinType.INNER);
        User user = userManager.getUserById(1L, fetchStrategy);
        try {
            user.getCompany().getPublisher().getAccount().getBalance();
        } catch (org.hibernate.LazyInitializationException e) {
            fail("Deep fetch didn't work");
        }
    }

    @Test
    public void testAdSpace() {
        FetchStrategyImpl asfs = new FetchStrategyImpl();
        //asfs.addEagerlyLoadedFieldForClass(AdSpace.class, "formats");
        asfs.addEagerlyLoadedFieldForClass(AdSpace.class, "publication", JoinType.LEFT);
        AdSpace adSpace = publicationManager.getAdSpaceById(1L, asfs);
        assertNotNull(adSpace);
        assertNotNull(adSpace.getFormats());
        assertNotNull(adSpace.getPublication());
        System.out.println("pub " + adSpace.getPublication().getId());
        System.out.println("formats " + adSpace.getFormats().size());
    }

    @Test
    public void testCreative() {

        FetchStrategyImpl creativeFs = new FetchStrategyImpl();
        creativeFs.addEagerlyLoadedFieldForClass(Creative.class, "campaign", JoinType.LEFT);
        creativeFs.addEagerlyLoadedFieldForClass(Campaign.class, "advertiser", JoinType.INNER);

        try {
            Creative creative = creativeManager.getCreativeById(27605l, creativeFs);
            creative.getCampaign().getAdvertiser().getName();
        } catch (Exception e) {
            fail("Deep fetch didn't work");
        }
    }


}
