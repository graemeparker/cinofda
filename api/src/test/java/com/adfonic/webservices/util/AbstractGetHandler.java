package com.adfonic.webservices.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

import com.adfonic.webservices.dto.CampStats;
import com.adfonic.webservices.dto.Statistics;
import com.adfonic.webservices.util.WSFixture.Format;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public abstract class AbstractGetHandler implements Get {

    public <T> T get(Class<T> clazz, String path, String... params) throws Exception {
        Response response = getResponse(getFormat(), path, params);

        verifyStatus(response.getStatus());

        return parseResponseEntity(response.getContent(), clazz);
    }


    protected void verifyStatus(int status) {
    }


    abstract protected Format getFormat();


    abstract protected <T> T parseResponseEntity(InputStream entity, Class<T> clazz) throws IOException;

    private GsonBuilder gsonBuilder;
    protected Gson gson;
    {// just to keep creation policy in one place
        gsonBuilder = new GsonBuilder();
        gsonBuilder// .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
                .registerTypeAdapter(Statistics[].class, new ArrSer())/*.registerTypeAdapter(AdSlotStatistics[].class, new ArrSer())*/.registerTypeAdapter(CampStats[].class, new ArrSer()).setDateFormat("MM-dd-yyyy HH:mm");// per implementation; not spec
        gson = gsonBuilder.create();
    }

    class ArrSer<T, E> implements JsonDeserializer<T> {

        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = gsonBuilder.create();
            // TODO Auto-generated method stub
            if (json.isJsonArray()) {
                System.out.println("shou "+typeOfT+" sf "+json);
                // return(context.deserialize(json, Statistics[].class));//not got enough recursive loop
                return (gson.fromJson(json, typeOfT));
            } else if (json.isJsonObject()) {
                Class<T> t = (Class<T>) typeOfT;
                E[] stats = (E[]) Array.newInstance(t.getComponentType(), 1);
                stats[0] = (E) gson.fromJson(json, t.getComponentType());
                // Statistics[] stats=new Statistics[1];
                // stats[0]=gson.fromJson(json, Statistics.class);
                // return((T)stats);
                return ((T) stats);
            }
            return (null);
        }

    }

    WSFixture f;


    public AbstractGetHandler(WSFixture fixture) {
        f = fixture;
    }


    private Response getResponse(Format format, String path, String... params) throws Exception {
        String url = f.buildUrl(path, format) + getQueryString(params);
        System.out.println("Firing Request:\nurl:" + url + "\nemail:" + f.userEmail + "\ndevkey:" + f.userDeveloperKey);
        return f.getResponse(url, f.userEmail, f.userDeveloperKey);
    }


    private String getQueryString(String... params) {
        String sep = "?";
        String qStr = "";
        for (String p : params) {
            if (p.indexOf('=') == -1) {
                String pv = f.globalParamMap.get(p);
                if (pv != null) {
                    p += "=" + pv;
                }
            }
            qStr += sep + p;
            sep = "&";
        }
        return (qStr);
    }

}
