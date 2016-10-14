package com.adfonic.webservices;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;

import com.adfonic.domain.User;
import com.adfonic.util.Base64;

public class Util {

    public static String buildUrl(String serviceUrl, String format, String username, String password, String hostName, String hostPort, String prefix) throws Exception {
        StringBuffer buffer = new StringBuffer();

        buffer.append("http://");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            buffer.append(username + ":" + password + "@");
        }
        buffer.append(hostName + ":" + hostPort + prefix + serviceUrl + (format != null ? "." + format : ""));

        return buffer.toString();
    }


    public static String getWebServiceResponse(String url, String username, String password) throws Exception {
        WebClient client = WebClient.create(url, username, password, null);
        Response response = client.get();
        System.out.println("Response Code: " + response.getStatus());
        StringWriter writer = new StringWriter();
        IOUtils.copy((InputStream) response.getEntity(), writer);
        String theString = writer.toString();
        return theString;
    }


    private static String getAuthString(User user) {
        return user.getEmail() + ":" + user.getDeveloperKey();
    }


    private static String getAuthString(String username, String password) {
        return Base64.encodeString(username + ":" + password);
    }


    public static void logDecodedJsonWebServiceResponse(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map values = mapper.readValue(response, Map.class);

        Set<Entry> set = values.entrySet();
        for (Entry entry : set) {
            String key = (String) entry.getKey();
            if ("masg-response".equals(key)) {
                if (entry.getValue() instanceof ArrayList) {
                    ArrayList list = (ArrayList) entry.getValue();
                    for (Object obj : list) {
                        if (obj instanceof Map) {
                            log("------------------------------------------------------");
                            Map map = (Map) obj;
                            Set<Entry> mapSet = map.entrySet();
                            for (Entry mapEntry : mapSet) {
                                log(mapEntry.getKey() + ": " + mapEntry.getValue());
                            }
                        }
                    }
                    log("------------------------------------------------------");
                } else if (entry.getValue() instanceof Map) {
                    Map map = (Map) entry.getValue();
                    Set<Entry> mapSet = map.entrySet();
                    for (Entry mapEntry : mapSet) {
                        log(mapEntry.getKey() + ": " + mapEntry.getValue());
                    }
                }
            } else if ("masg-error".equals(key)) {
                Map map = (Map) entry.getValue();
                Integer code = (Integer) map.get("code");
                String description = (String) map.get("description");
                log("Got Error code " + code + ": " + description);
            }
        }
    }


    static void log(String s) {
        System.out.println(s);
    }

}
