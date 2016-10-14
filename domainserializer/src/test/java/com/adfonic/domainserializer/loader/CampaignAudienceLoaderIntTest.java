package com.adfonic.domainserializer.loader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domainserializer.loader.CampaignAudienceLoader;

@Ignore("database needs to be populated before")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-domain-cache-loader-context.xml" })
public class CampaignAudienceLoaderIntTest {

    @Autowired
    private BasicDataSource dataSource;
    private CampaignAudienceLoader testObj = new CampaignAudienceLoader();

    @Test
    public void testLoadCampaignAudiences() throws SQLException {
        Assert.assertNotNull(dataSource);

        Map<Long, CampaignDto> campaignsById = new HashMap<Long, CampaignDto>();
        campaignsById.put(3745L, new CampaignDto());
        campaignsById.put(4012L, new CampaignDto());
        campaignsById.put(4033L, new CampaignDto());

        try (Connection conn = dataSource.getConnection();) {
            testObj.loadCampaignAudiences(conn, campaignsById);
        }

        CampaignDto campaign3745 = campaignsById.get(3745L);
        Assert.assertEquals(3, campaign3745.getDeviceIdAudiences().size());
    }

}
