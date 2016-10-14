package com.byyd.middleware.domainlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AuditLog;
import com.adfonic.domain.Category;
import com.adfonic.domain.Company;
import com.adfonic.domain.Country;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.common.filter.CurrencyExchangeRatesFilter;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.domainlog.service.AuditLogManager;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-entity-auditor-test-context.xml"})
@DirtiesContext
public class EntityAuditorIT extends AbstractAdfonicTest {
    private EntityAuditor entityAuditor;
    private AuditLogManager auditLogManager;
    private UserManager userManager;
    private PublisherManager publisherManager;
    private CompanyManager companyManager;
    private CommonManager commonManager;
    private PublicationManager publicationManager;

    /**
     * We needed this unit test to run based on a very specific CTX, different from the other unit tests in this package.
     * Even tho we tried using the @DirtiesContext annotation, Spring kept in executing this one using the same CTX as the
     * others. So  we made the constructor explicitely load the CTX is must use, and wire the manager manually.
     * @throws Exception
     */
    public EntityAuditorIT() throws Exception {
        super();
        userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        companyManager = AdfonicBeanDispatcher.getBean(CompanyManager.class);
        entityAuditor = AdfonicBeanDispatcher.getBean(EntityAuditor.class);
        auditLogManager = AdfonicBeanDispatcher.getBean(AuditLogManager.class);
        commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        publisherManager = AdfonicBeanDispatcher.getBean(PublisherManager.class);
        publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
    }

    @Test
    public void testAuditPublicationStatedCategories() {
        String rtbId = randomAlphaNumericString(10);
        String name = randomAlphaNumericString(10);

        Publisher publisher = publisherManager.getPublisherById(1L);
        PublicationType publicationType = publicationManager.getPublicationTypeBySystemName("OTHER_APP");
        
        Publication publication = new Publication(publisher);
        publication.setPublicationType(publicationType);
        publication.setStatus(Publication.Status.PENDING);
        publication.setRtbId(rtbId);
        publication.setName(name);
        publication.setAutoApproval(true);
        publication.setBackfillEnabled(false);
        publication.setDefaultIntegrationType(publicationManager.getIntegrationTypeBySystemName("rtb"));
        publication.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));
        publication.setURLString(randomUrl());
        publication = publicationManager.create(publication);

        System.out.println("********** publication.id = " + publication.getId());

        System.out.println("********** publication.statedCategoriesAsString BEFORE = " + publication.getStatedCategoriesAsString());
        
        publication.getStatedCategories().add(commonManager.getCategoryByIabId("IAB1"));
        publication.getStatedCategories().add(commonManager.getCategoryByIabId("IAB5"));
        publication.getStatedCategories().add(commonManager.getCategoryByIabId("IAB9"));
        
        System.out.println("********** publication.statedCategoriesAsString PRE-UPDATE = " + publication.getStatedCategoriesAsString());

        publicationManager.update(publication);

        System.out.println("********** publication.statedCategoriesAsString POST-UPDATE = " + publication.getStatedCategoriesAsString());
    }

    @Test
    public void testEntityAuditor() {
        FetchStrategyImpl auditLogFetchStrategy = new FetchStrategyImpl();
        auditLogFetchStrategy.addEagerlyLoadedFieldForClass(AuditLog.class, "user", JoinType.LEFT);
        auditLogFetchStrategy.addEagerlyLoadedFieldForClass(AuditLog.class, "adfonicUser", JoinType.LEFT);

        // Create a Company
        Country origCountry = commonManager.getCountryByIsoCode("US");
        String origName = "EntityAuditorIT Company " + System.currentTimeMillis();
        
        // Get default currency exchange rate
        CurrencyExchangeRatesFilter currencyExchangeRatesFilter = new CurrencyExchangeRatesFilter().setDefaultConversion(true);
        List<CurrencyExchangeRate> currencyExchangeRates = commonManager.getCurrencyExchangeRates(currencyExchangeRatesFilter);

        Company company = companyManager.newCompany(origName, origCountry, currencyExchangeRates.get(0));

        List<AuditLog> list = auditLogManager.getAll(new AuditLogFilter().objectId(AuditLog.getObjectId(company)), auditLogFetchStrategy);
        assertTrue(list.size() >= 1);
        boolean foundCreate = false;
        Set<Long> auditLogIdsFromCreate = new HashSet<Long>();
        for (AuditLog auditLog : list) {
            auditLogIdsFromCreate.add(auditLog.getId()); // keep track of the ones we saw for the create
            assertNull("Old value for " + auditLog.getField() + " was not null", auditLog.getOldValue());
            assertNull(auditLog.getAdfonicUser());
            assertNull(auditLog.getUser());
            if (AuditLog.CREATE_FIELD_PLACEHOLDER.equals(auditLog.getField())) {
                foundCreate = true;
            } else if ("country".equals(auditLog.getField())) {
                assertEquals("Country mismatch", origCountry.getName(), auditLog.getNewValue());
            } else if ("name".equals(auditLog.getField())) {
                assertEquals("Name mismatch", origName, auditLog.getNewValue());
            }
        }
        assertTrue("Create marker not found", foundCreate);

        // We'll need a User and AdfonicUser to test the "bind context" stuff
        String emailForBoth = "unused" + System.currentTimeMillis() + "-noreply@adfonic.com";
        String passwordForBoth = "pa$$w0rD";
        User user = userManager.newUser(company, emailForBoth, passwordForBoth);

        AdfonicUser adfonicUser = new AdfonicUser();
        adfonicUser.setEmail(emailForBoth);
        adfonicUser.setPassword(passwordForBoth);
        adfonicUser.setStatus(AdfonicUser.Status.ACTIVE);
        adfonicUser.setLoginName("loginName" + System.currentTimeMillis());
        adfonicUser = userManager.create(adfonicUser);

        // The first thing we should check is that there are no AuditLog entries for
        // either of those entities we just created...they aren't configured in our
        // AuditorConfig in our app context.
        String oid = AuditLog.getObjectId(user);
        System.out.println("Looking for OID " + oid);
        assertEquals(Long.valueOf(0), auditLogManager.countAll(new AuditLogFilter().objectId(oid)));
        oid = AuditLog.getObjectId(adfonicUser);
        System.out.println("Looking for OID " + oid);
        assertEquals(Long.valueOf(0), auditLogManager.countAll(new AuditLogFilter().objectId(oid)));

        // Bind the User and AdfonicUser
        entityAuditor.bindContext(user, adfonicUser);
        try {
            // Now update the object and make sure the updated fields get audited
            Country newCountry = commonManager.getCountryByIsoCode("GB");
            String newName = origName + " new";
            company.setName(newName);
            company.setCountry(newCountry);
            companyManager.update(company);

            // We now expect to see 2 new rows (we establish "new" via maxIdFromCreate)
            list = auditLogManager.getAll(new AuditLogFilter().objectId(AuditLog.getObjectId(company)), auditLogFetchStrategy);
            assertEquals(auditLogIdsFromCreate.size() + 2, list.size());
            for (AuditLog auditLog : list) {
                if (auditLogIdsFromCreate.contains(auditLog.getId())) {
                    continue; // this is from the create, ignore it
                }

                // Make sure User and AdfonicUser were set
                assertNotNull("User wasn't set", auditLog.getUser());
                assertEquals("User mismatches", user, auditLog.getUser());
                assertNotNull("AdfonicUser wasn't set", auditLog.getAdfonicUser());
                assertEquals("AdfonicUser mismatches", adfonicUser, auditLog.getAdfonicUser());

                if ("country".equals(auditLog.getField())) {
                    assertEquals("Country mismatch (orig)", origCountry.getName(), auditLog.getOldValue());
                    assertEquals("Country mismatch (new)", newCountry.getName(), auditLog.getNewValue());
                } else if ("name".equals(auditLog.getField())) {
                    assertEquals("Name mismatch (orig)", origName, auditLog.getOldValue());
                    assertEquals("Name mismatch (new)", newName, auditLog.getNewValue());
                }
            }

            // Test binding the context a second time...not a good thing to do
            // but it shouldn't fail...and we need the code coverage
            entityAuditor.bindContext(user, adfonicUser);
            entityAuditor.bindContext(user, null);
            entityAuditor.bindContext(null, adfonicUser);
            entityAuditor.bindContext(null, null);
        } finally {
            entityAuditor.unbindContext();
            // Calling it again, while not likely, shouldn't hurt anything
            entityAuditor.unbindContext();
        }
    }
}
