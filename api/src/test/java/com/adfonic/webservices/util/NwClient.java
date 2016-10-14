package com.adfonic.webservices.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.junit.Assert;

import com.adfonic.webservices.util.WSFixture.Format;

public class NwClient implements WSclient {

    private String user;

    private String password;

    private String host;

    private String port;

    private String ctxtroot;


    public NwClient(String host, String port, String ctxtroot, Credentials cred) {
        user = cred.getUser();
        password = cred.getPassword();

        this.host = host;
        this.port = port;
        this.ctxtroot = ctxtroot;
    }


    public String postForm(String urlPath, com.adfonic.webservices.util.Form form) throws Exception {
        return postForm(urlPath, this.user, this.password, form);
    }


    public String postForm(String urlPath, String user, String password, com.adfonic.webservices.util.Form form) throws Exception {
        Form wForm = new Form();
        for (Entry<String, String> entry : form.getEntries()) {
            wForm.set(entry.getKey(), entry.getValue());
        }

        WebClient client = createClient(urlPath, user, password);
        Response response = client.form(wForm);
        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) response.getEntity(), writer);
        String theString = writer.toString();
        return theString;
    }


    public String get(String urlPath, String user, String pass) throws Exception {
        com.adfonic.webservices.util.Response customResponse = getResponse(urlPath, user, pass);
        StringWriter writer = new StringWriter();
        IOUtils.copy(customResponse.getContent(), writer);
        return writer.toString();
    }


    public com.adfonic.webservices.util.Response getResponse(String urlPath, String user, String pass) throws Exception {
        WebClient client = createClient(urlPath, user, pass);
        if(System.getProperty("adf.tst.accpthdr.urlbased")!=null){
            if(urlPath.endsWith("json")){
                client.accept(MediaType.APPLICATION_JSON);
            }
        }
        Response response = client.get();
        com.adfonic.webservices.util.Response customResponse = new com.adfonic.webservices.util.Response();
        customResponse.setStatus(response.getStatus());
        System.out.println("Response Code: " + customResponse.getStatus());
        customResponse.setContent((InputStream) response.getEntity());
        return customResponse;
    }


    public String post(String urlPath, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        WebClient client = createClient(urlPath, user, password);

        requestBody = preProcReq(requestBody, fSnd, client);

        // currently it doesn't ignore accepts
        client.accept(fRcv == Format.JSON ? MediaType.APPLICATION_JSON : MediaType.TEXT_XML);
        // currently even this does not work
        // client.accept(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);

        System.out.println("Request.Content  : [" + requestBody + "]\n");
        Response response = client.post(requestBody);
        int status = response.getStatus();
        System.out.println("Response.Status  : [" + status + "]\n");
        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) response.getEntity(), writer);
        String theString = writer.toString();
        System.out.println("Response.Content : [" + theString + "]\n");
        if (expectedStatus > 0) {
            Assert.assertEquals(expectedStatus, status);
        }
        return theString;
    }


    public String put(String urlPath, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        WebClient client = createClient(urlPath, user, password);

        requestBody = preProcReq(requestBody, fSnd, client);

        // currently it doesn't ignore accepts
        client.accept(fRcv == Format.JSON ? MediaType.APPLICATION_JSON : MediaType.TEXT_XML);
        // currently even this does not work
        // client.accept(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);

        System.out.println("Request.Content  : [" + requestBody + "]\n");
        Response response = client.put(requestBody);
        int status = response.getStatus();
        System.out.println("Response.Status  : [" + status + "]\n");
        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) response.getEntity(), writer);
        String theString = writer.toString();
        System.out.println("Response.Content : [" + theString + "]\n");
        if (expectedStatus > 0) {
            Assert.assertEquals(expectedStatus, status);
        }
        return theString;
    }


    public void delete(String urlPath, Format format, int expectedStatus) throws Exception {
        WebClient client = createClient(urlPath, user, password);

        System.out.println("Request.DeleteResource  : [" + client.getCurrentURI() + "]\n");
        Response response = client.delete();
        int status = response.getStatus();
        System.out.println("Response.Status  : [" + status + "]\n");
        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) response.getEntity(), writer);
        String theString = writer.toString();
        System.out.println("Response.Content : [" + theString + "]\n");
        if (expectedStatus > 0) {
            Assert.assertEquals(expectedStatus, status);
        }
    }


    private String preProcReq(String requestBody, Format fSnd, WebClient client) {
        switch (fSnd) {
        case JSON:
            client.type(MediaType.APPLICATION_JSON);
            // return("masg-request:"+requestBody);
            return (requestBody);

        case XML:
            client.type(MediaType.TEXT_XML);
            // return("<masg-request>"+requestBody+"</masg-request>");
            return (requestBody);

        default:
            return (requestBody);
        }
    }


    private WebClient createClient(String urlPath, String user, String password) {
        return WebClient.create(buildUrl(urlPath), user, password, null);
    }


    private String buildUrl(String urlPath) {
        StringBuilder url = new StringBuilder();
        url.append("http://")
            .append(host).append(":")
            .append(port)
            .append(ctxtroot)
            .append(urlPath);

        return (URLEncoder.encode(url.toString()));
    }
}
