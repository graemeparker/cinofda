package com.adfonic.servlet;

import static com.adfonic.test.AbstractAdfonicTest.randomAlphaNumericString;
import static com.adfonic.test.AbstractAdfonicTest.randomEmailAddress;
import static com.adfonic.test.AbstractAdfonicTest.randomUrl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.adfonic.beans.BaseBean;
import com.adfonic.domain.AdfonicUser;
import com.byyd.middleware.account.service.UserManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseBean.class})
public class TestPostSecurityLoginFilter {
    private UserManager userManager;
    private RequestCache requestCache;
    private PostSecurityLoginFilter filter;

    @Before
    public void runBeforeEachTest() {
        userManager = createMock(UserManager.class);
        requestCache = createMock(RequestCache.class);
        filter = new PostSecurityLoginFilter(userManager, requestCache);
    }

    @Test
    public void test00_autowired_constructor_code_coverage() {
        new PostSecurityLoginFilter(null);
    }

    @Test
    public void test01_detectAdfonicUser_already_logged_in() {
        String loginName = randomAlphaNumericString(10);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        AdfonicUser adfonicUser = createMock(AdfonicUser.class);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(adfonicUser);
        expect(adfonicUser.getLoginName()).andReturn(loginName);
        replayAll();
        assertEquals(adfonicUser, filter.detectAdfonicUser(request));
    }

    @Test
    public void test02_detectAdfonicUser_not_logged_in_and_no_remote_user() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(null);
        expect(request.getRemoteUser()).andReturn(null);
        replayAll();
        assertNull(filter.detectAdfonicUser(request));
    }

    @Test
    public void test03_detectAdfonicUser_not_logged_in_unrecognized_email() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        String email = randomEmailAddress();
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(null);
        expect(request.getRemoteUser()).andReturn(email);
        expect(userManager.getAdfonicUserByEmail(email)).andReturn(null);
        replayAll();
        assertNull(filter.detectAdfonicUser(request));
    }

    @Test
    public void test04_detectAdfonicUser_not_logged_in_valid_login() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        String email = randomEmailAddress();
        AdfonicUser adfonicUser = createMock(AdfonicUser.class);
        String loginName = randomAlphaNumericString(10);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(null);
        expect(request.getRemoteUser()).andReturn(email);
        expect(userManager.getAdfonicUserByEmail(email)).andReturn(adfonicUser);
        expect(adfonicUser.getLoginName()).andReturn(loginName);
        BaseBean.setAdfonicUser(request, adfonicUser); expectLastCall().once();
        replayAll();
        assertEquals(adfonicUser, filter.detectAdfonicUser(request));
    }

    @Test
    public void test05_doFilterInternal_not_logged_in() throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain filterChain = createMock(FilterChain.class);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(null);
        expect(request.getRemoteUser()).andReturn(null);
        filterChain.doFilter(request, response); expectLastCall().once();
        replayAll();
        filter.doFilterInternal(request, response, filterChain);
    }

    @Test
    public void test06_doFilterInternal_logged_in_no_saved_request() throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain filterChain = createMock(FilterChain.class);
        AdfonicUser adfonicUser = createNiceMock(AdfonicUser.class);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(adfonicUser);
        expect(requestCache.getRequest(request, response)).andReturn(null);
        filterChain.doFilter(request, response); expectLastCall().once();
        replayAll();
        filter.doFilterInternal(request, response, filterChain);
    }

    @Test
    public void test07_doFilterInternal_logged_in_saved_request_no_redirect_url() throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain filterChain = createMock(FilterChain.class);
        AdfonicUser adfonicUser = createNiceMock(AdfonicUser.class);
        SavedRequest savedRequest = createMock(SavedRequest.class);
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(adfonicUser);
        expect(requestCache.getRequest(request, response)).andReturn(savedRequest);
        expect(savedRequest.getRedirectUrl()).andReturn(null);
        filterChain.doFilter(request, response); expectLastCall().once();
        replayAll();
        filter.doFilterInternal(request, response, filterChain);
    }

    @Test
    public void test08_doFilterInternal_redirect() throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain filterChain = createMock(FilterChain.class);
        AdfonicUser adfonicUser = createNiceMock(AdfonicUser.class);
        SavedRequest savedRequest = createMock(SavedRequest.class);
        String redirectUrl = randomUrl();
        mockStatic(BaseBean.class);
        expect(BaseBean.adfonicUser(request)).andReturn(adfonicUser);
        expect(requestCache.getRequest(request, response)).andReturn(savedRequest);
        expect(savedRequest.getRedirectUrl()).andReturn(redirectUrl).atLeastOnce();
        requestCache.removeRequest(request, response); expectLastCall().once();
        response.sendRedirect(redirectUrl); expectLastCall().once();
        replayAll();
        filter.doFilterInternal(request, response, filterChain);
    }
}