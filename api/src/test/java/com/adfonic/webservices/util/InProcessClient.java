package com.adfonic.webservices.util;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import com.adfonic.webservices.util.WSFixture.Format;

public class InProcessClient implements WSclient {

    private DispatcherServlet dispatcher;

    private String user;

    private String password;


    public InProcessClient(Credentials cred) {
        user = cred.getUser();
        password = cred.getPassword();
        dispatcher = MockWebContextLoader.getServletInstance();
    }


    public String postForm(String url, Form form) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    public String postForm(String url, String user, String password, Form form) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    public String get(String url, String user, String password) throws Exception {
        MockHttpServletRequest request = MockWebContextLoader.mockRequest("GET", "/asset/002a84c2-7295-47fb-97db-3236d6028038.json", user, password, null);
        MockHttpServletResponse response = MockWebContextLoader.mockResponse();
        dispatcher.service(request, response);

        String responseStr = response.getContentAsString();
        System.out.println("RESPSTR" + responseStr);
        return responseStr;
    }


    public Response getResponse(String url, String user, String password) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    public String post(String url, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    public String put(String url, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    public void delete(String url, Format format, int expectedStatus) throws Exception {
        // TODO Auto-generated method stub

    }

}
