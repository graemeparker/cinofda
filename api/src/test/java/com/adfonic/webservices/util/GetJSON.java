package com.adfonic.webservices.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

import javax.ws.rs.core.Response;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.adfonic.webservices.util.WSFixture.Format;

public class GetJSON extends AbstractGetHandler implements Get {

    public GetJSON(WSFixture fixture) {
        super(fixture);
    }


    @Override
    protected Format getFormat() {
        return (Format.JSON);
    }


    @Override
    // Just to capture the status. Being processing object won't hit class invariants much
    protected void verifyStatus(int status) {
        super.verifyStatus(this.status = status);
    }

    int status;


    @Override
    public <T> T parseResponseEntity(InputStream entity, Class<T> clazz) throws IOException {
        String jsonStr = IOUtils.toString(entity);
        System.out.println("Response.Message.Raw: " + jsonStr);
        JSONObject masgRoot = (JSONObject) JSONSerializer.toJSON(jsonStr);

        String rootElem = "masg-" + (status == Response.Status.OK.getStatusCode() ? "response" : "error");

        String jsonMasgContent;
        if (clazz.isArray()) {
            if (!masgRoot.isArray()) {
                JSON json;
                try {
                    json = masgRoot.getJSONArray(rootElem);
                } catch (JSONException je) {// Bad API; has to work around
                    json = masgRoot.getJSONObject(rootElem);
                }
                if (json.isEmpty()) {
                    return ((T) Array.newInstance(clazz.getComponentType(), 0));
                } else {
                    jsonMasgContent = json.toString();
                }
            } else {
                throw new RuntimeException("unexpected");
            }
        } else {
            JSONObject jsonObj = masgRoot.getJSONObject(rootElem);
            if (jsonObj.isEmpty()) {
                try {
                    return (clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            jsonMasgContent = jsonObj.toString();
        }
        System.out.println("Response.Message.JSON.Processed: " + jsonMasgContent);
        return (gson.fromJson(jsonMasgContent, clazz));
    }

}
