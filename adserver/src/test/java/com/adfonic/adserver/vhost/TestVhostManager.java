package com.adfonic.adserver.vhost;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.Constant;
import com.adfonic.test.AbstractAdfonicTest;

public class TestVhostManager extends AbstractAdfonicTest {
    private VhostManager vhostManager;
    private ServletContext servletContext;
    private final File configFile = new File("src/test/resources/test-vhosts.xml");

    @Before
    public void runBeforeEachTest() {
        servletContext = mock(ServletContext.class);

        vhostManager = new VhostManager();
        vhostManager.setServletContext(servletContext);
        inject(vhostManager, "configFile", configFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReloadConfig05_blank_assetBaseUrl() throws Exception {
        inject(vhostManager, "configFile", new File("src/test/resources/test-vhosts-blank-assetBaseUrl.xml"));
        vhostManager.reloadConfig();
    }

    @Test(expected = IllegalStateException.class)
    public void testReloadConfig06_no_catch_all() throws Exception {
        inject(vhostManager, "configFile", new File("src/test/resources/test-vhosts-no-catch-all.xml"));
        vhostManager.reloadConfig();
    }

    @Test
    public void testReloadConfig07_multiple_for_same_name() throws Exception {
        inject(vhostManager, "configFile", new File("src/test/resources/test-vhosts-multiple-same-name.xml"));
        vhostManager.reloadConfig();
    }

    @Test(expected = IllegalStateException.class)
    public void testReloadConfig08_multiple_catch_alls() throws Exception {
        inject(vhostManager, "configFile", new File("src/test/resources/test-vhosts-multiple-catch-alls.xml"));
        vhostManager.reloadConfig();
    }

    @Test
    public void testReloadConfig09_good() throws Exception {
        vhostManager.reloadConfig();
    }

    /**
     * This isn't working...I haven't dug into why yet, just pulled it out of my ass.
    @Test
    public void testReloadWhenConfigFileIsUpdated() throws Exception {
        String xml = IOUtils.toString(new FileInputStream(configFile));
        File tmpConfig = new File("TestVhostManager-configFile.xml");
        IOUtils.write(xml, new FileOutputStream(tmpConfig));
        expect(new Expectations() {{
            oneOf (servletContext).getInitParameter("clickThroughUri"); will(throwException(new IllegalStateException("force defaults")));
        }});
        inject(vhostManager, "configFile", tmpConfig);
        inject(vhostManager, "checkForUpdatesPeriodSec", 1);
        vhostManager.initialize();
        // Rewrite the config
        IOUtils.write(xml, new FileOutputStream(tmpConfig));
        // Sleep a couple of seconds to let the reload kick in
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }
    }
    */

    @Test
    public void testDestroy10_null_monitor() {
        vhostManager.destroy();
    }

    @Test
    public void testGetBeaconBaseUrl12_not_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String beaconBaseUrl = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getBeaconBaseUrl();
                will(returnValue(beaconBaseUrl));
            }
        });
        vhostManager.configRef.set(config);
        assertEquals(beaconBaseUrl, vhostManager.getBeaconBaseUrl(request).toString());
    }

    @Test
    public void testGetBeaconBaseUrl13_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String scheme = randomAlphaNumericString(10);
        final String serverName = randomHostName();
        final int serverPort = randomInteger(1000) + 81; // some non-80 port atleast 81 or more
        final String contextPath = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getBeaconBaseUrl();
                will(returnValue(null));
                allowing(request).getScheme();
                will(returnValue(scheme));
                allowing(request).getServerName();
                will(returnValue(serverName));
                allowing(request).getServerPort();
                will(returnValue(serverPort));
                allowing(request).getContextPath();
                will(returnValue(contextPath));
            }
        });
        vhostManager.configRef.set(config);
        String expected = scheme + "://" + serverName + ":" + serverPort + contextPath + Constant.BEACON_URI_PATH;
        assertEquals(expected, vhostManager.getBeaconBaseUrl(request).toString());
    }

    @Test
    public void testGetClickRedirectBaseUrl14_not_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String clickRedirectBaseUrl = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getClickRedirectBaseUrl();
                will(returnValue(clickRedirectBaseUrl));
            }
        });
        vhostManager.configRef.set(config);
        assertEquals(clickRedirectBaseUrl, vhostManager.getClickRedirectBaseUrl(request).toString());
    }

    @Test
    public void testGetClickRedirectBaseUrl15_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String scheme = randomAlphaNumericString(10);
        final String serverName = randomHostName();
        final int serverPort = randomInteger(1000) + 81; // some non-80 port
        final String contextPath = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getClickRedirectBaseUrl();
                will(returnValue(null));
                allowing(request).getScheme();
                will(returnValue(scheme));
                allowing(request).getServerName();
                will(returnValue(serverName));
                allowing(request).getServerPort();
                will(returnValue(serverPort));
                allowing(request).getContextPath();
                will(returnValue(contextPath));
            }
        });
        vhostManager.configRef.set(config);
        String expected = scheme + "://" + serverName + ":" + serverPort + contextPath + Constant.CLICK_REDIRECT_PATH;
        assertEquals(expected, vhostManager.getClickRedirectBaseUrl(request).toString());
    }

    @Test
    public void testGetClickBaseUrl16_not_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String clickBaseUrl = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getClickBaseUrl();
                will(returnValue(clickBaseUrl));
            }
        });
        vhostManager.configRef.set(config);
        assertEquals(clickBaseUrl, vhostManager.getClickBaseUrl(request).toString());
    }

    @Test
    public void testGetClickBaseUrl17_empty() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String scheme = randomAlphaNumericString(10);
        final String serverName = randomHostName();
        final int serverPort = randomInteger(1000) + 81; // some non-80 port
        final String contextPath = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getClickBaseUrl();
                will(returnValue(null));
                allowing(request).getScheme();
                will(returnValue(scheme));
                allowing(request).getServerName();
                will(returnValue(serverName));
                allowing(request).getServerPort();
                will(returnValue(serverPort));
                allowing(request).getContextPath();
                will(returnValue(contextPath));
            }
        });
        vhostManager.configRef.set(config);
        String expected = scheme + "://" + serverName + ":" + serverPort + contextPath + Constant.CLICK_THROUGH_PATH;
        assertEquals(expected, vhostManager.getClickBaseUrl(request).toString());
    }

    @Test
    public void testGetAssetBaseUrl18() throws Exception {
        final VhostManager.Config config = mock(VhostManager.Config.class, "config");
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final Vhost vhost = mock(Vhost.class);
        final String assetBaseUrl = randomUrl();
        expect(new Expectations() {
            {
                oneOf(config).getVhost(request);
                will(returnValue(vhost));
                allowing(vhost).getAssetBaseUrl();
                will(returnValue(assetBaseUrl));
            }
        });
        vhostManager.configRef.set(config);
        assertEquals(assetBaseUrl, vhostManager.getAssetBaseUrl(request));
    }

    @Test
    public void testMakeBaseUrl19_port_80_with_no_baseUri() {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final String scheme = randomAlphaNumericString(10);
        final String serverName = randomHostName();
        final int serverPort = 80;
        final String contextPath = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                allowing(request).getScheme();
                will(returnValue(scheme));
                allowing(request).getServerName();
                will(returnValue(serverName));
                allowing(request).getServerPort();
                will(returnValue(serverPort));
                allowing(request).getContextPath();
                will(returnValue(contextPath));
            }
        });
        String expected = scheme + "://" + serverName + contextPath;
        assertEquals(expected, VhostManager.makeBaseUrl(request).toString());
    }

    @Test
    public void testMakeBaseUrl20_port_non_80_with_baseUri() {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final String scheme = randomAlphaNumericString(10);
        final String serverName = randomHostName();
        final int serverPort = 81;
        final String contextPath = randomAlphaNumericString(10);
        final String baseUri = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                allowing(request).getScheme();
                will(returnValue(scheme));
                allowing(request).getServerName();
                will(returnValue(serverName));
                allowing(request).getServerPort();
                will(returnValue(serverPort));
                allowing(request).getContextPath();
                will(returnValue(contextPath));
            }
        });
        String expected = scheme + "://" + serverName + ":" + serverPort + contextPath + baseUri;
        assertEquals(expected, VhostManager.makeBaseUrl(request, baseUri).toString());
    }
}
