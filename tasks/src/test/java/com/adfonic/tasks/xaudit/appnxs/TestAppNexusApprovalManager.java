package com.adfonic.tasks.xaudit.appnxs;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord.AuditStatus;
import com.adfonic.tasks.xaudit.appnxs.dat.CreativeMobile;
import com.adfonic.tasks.xaudit.appnxs.dat.CreativeTemplate;

@RunWith(PowerMockRunner.class)
@Ignore
public class TestAppNexusApprovalManager {

    private String creativeServiceUrl = "http://api-impbus.client-testing.adnxs.net/creative/4551";
    private int memberId = 4551;
    private String authenticateUrl = "http://api-impbus.client-testing.adnxs.net/auth";
    private String username = "byyd_ctmember_user";
    private String password = "byydtesting";

    //	private String creativeServiceUrl = "http://api.adnxs.net/creative/2560";
    //	private int memberId = 2560;
    //	private String authenticateUrl = "http://api.adnxs.net/auth";
    //	private String username = "byyd_bidder_user";
    //	private String password = "LLpUArU";

    private int connTtlMs = 10000;
    private int maxTotal = 1;
    private int defaultMaxPerRoute = 1;
    private int connectTimeout = 10000;
    private int socketTimeout = 10000; // ten seconds

    @InjectMocks
    private AppNexusApiClient approvalManager = new AppNexusApiClient(creativeServiceUrl, authenticateUrl, "username", "password", memberId, connTtlMs, maxTotal,
            defaultMaxPerRoute, connectTimeout, socketTimeout);

    @Before
    public void setup() {
        // Watch out for UNAUTH, you'll need to generate a new token using the method below.
        //String token = approvalManager.getToken(0);
        //ReflectionTestUtils.setField(approvalManager, "authToken", "123");
        ReflectionTestUtils.setField(approvalManager, "authToken", "ibapi:79361:54187dc304f91:nym2");
    }

    @Test
    @Ignore
    public void checkCreativeStatus() {
        AppNexusCreativeRecord creative2 = approvalManager.getCreative("19147225");
        assertEquals(AuditStatus.rejected, creative2.getAudit_status());
    }

    @Test
    @Ignore
    public void deleteACreativeStatus() {
        boolean ok = false;
        ok = approvalManager.deleteCreative("813836");
        Assert.assertTrue(ok);
    }

    @Test
    @Ignore
    public void testGetToken() {
        String token = approvalManager.getAuthToken();
        System.out.println("token:" + token);
        Assert.assertNotNull(token);
    }

    @Test
    @Ignore
    public void testPostCreative() {
        AppNexusCreativeRecord appNxsCreative = new AppNexusCreativeRecord();
        appNxsCreative
                .setContent("<!-- Banner image -->\n<a href=\"http://appnexus-rtb.byyd.net/anxs/ct/${ASP_ID}/${AUCTION_IMP_ID}?pubr=6a91c79e-d662-40e3-9589-d37da13760b8&crid=c820b864-2a74-4ed0-8bf1-679dd75afbcb\"><img border=\"0\" alt=\"\" src=\"http://asset.byyd.net/as/21fd5db6-fa69-4448-b886-9931a5685a3d\" width=\"216\" height=\"36\" /></a>\n\n<!-- Beacon 1 -->\n    <img width=1 height=1 src=\"http://appnexus-rtb.byyd.net/anxs/bc/${ASP_ID}/${AUCTION_IMP_ID}.gif?pubr=6a91c79e-d662-40e3-9589-d37da13760b8&crid=c820b864-2a74-4ed0-8bf1-679dd75afbcb\"/>\n\n<!-- ${CUSTOM_BEACONS} -->");
        appNxsCreative.setAudit_status(AuditStatus.pending);
        appNxsCreative.setWidth(216);
        appNxsCreative.setTemplate(new CreativeTemplate(7));
        appNxsCreative.setHeight(36);
        appNxsCreative.setAllow_audit(true);
        appNxsCreative.setMember_id(memberId);
        appNxsCreative.setNo_iframes(true);
        appNxsCreative.setMobile(new CreativeMobile("http://www.whatever.com/"));
        String id = approvalManager.postCreative(appNxsCreative);
        System.out.println(id);
        Assert.assertNotNull(id);
    }

    @Test
    @Ignore
    public void testGetCreative() {
        AppNexusCreativeRecord creative = approvalManager.getCreative("18560824");
        assertEquals(creative.getMedia_url(), "http://www.google.com");
    }

    @Test
    @Ignore
    public void testUpdateCreative() {
        AppNexusCreativeRecord creative = new AppNexusCreativeRecord();
        creative.setContent("test contentt");
        creative.setWidth(300);
        creative.setHeight(250);
        creative.setMedia_url("http://www.google.com");
        creative.setTemplate(new CreativeTemplate(1));
        String id = approvalManager.postCreative(creative);
        //change the media
        creative.setMedia_url("http://www.google-tech.com");
        approvalManager.updateCreative(id, creative);
        AppNexusCreativeRecord creative2 = approvalManager.getCreative("733433");
        assertEquals(creative2.getMedia_url(), "http://www.google-tech.com");
    }
}
