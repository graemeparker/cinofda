package com.adfonic.adserver.controller.commands;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class RequestCommandWeb extends RequestCommandBase {

    protected JSONObject execute(String url, String requestData) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPostRequest = new HttpPost(url);
        HttpEntity httpEntity = new StringEntity(requestData);
        httpPostRequest.setEntity(httpEntity);
        //System.out.println("Hitting url "+ url);
        //System.out.println("With Request Data "+ requestData);
        HttpResponse response = httpClient.execute(httpPostRequest);
        //System.out.println(response.toString());
        InputStream in = response.getEntity().getContent();
        String data = IOUtils.toString(in, "UTF-8");
        //System.out.println(data);
        JSONObject jsonReponse = new JSONObject(data);
        return jsonReponse;
    }

    protected String executeAndGetString(String url, String requestData) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPostRequest = new HttpPost(url);
        HttpEntity httpEntity = new StringEntity(requestData);
        httpPostRequest.setEntity(httpEntity);
        //System.out.println("Hitting url "+ url);
        //System.out.println("With Request Data "+ requestData);
        HttpResponse response = httpClient.execute(httpPostRequest);
        for (Header oneHeader : response.getAllHeaders()) {
            //System.out.println("Header : " + oneHeader.getName() + "=" + oneHeader.getValue());
        }

        //System.out.println("httpresponse="+response.toString());
        InputStream in = response.getEntity().getContent();
        String data = IOUtils.toString(in, "UTF-8");
        //System.out.println("url "+ url+" returned following result");
        //System.out.println("Response="+data);
        return data;
    }

    protected String executeAndGetRedirectedUrl(String url, String requestData) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPostRequest = new HttpPost(url);
        HttpEntity httpEntity = new StringEntity(requestData);
        httpPostRequest.setEntity(httpEntity);
        //System.out.println("Hitting url "+ url);
        //System.out.println("With Request Data "+ requestData);
        HttpResponse response = httpClient.execute(httpPostRequest);
        String redirectedUrl = null;
        for (Header oneHeader : response.getAllHeaders()) {
            if ("Location".equalsIgnoreCase(oneHeader.getName())) {
                redirectedUrl = oneHeader.getValue();
            }
            //System.out.println("Header : " + oneHeader.getName() + "=" + oneHeader.getValue());
        }

        //System.out.println("httpresponse="+response.toString());
        InputStream in = response.getEntity().getContent();
        String data = IOUtils.toString(in, "UTF-8");
        //System.out.println("url "+ url+" returned following result");
        //System.out.println("Response="+data);

        return redirectedUrl;
    }
}
