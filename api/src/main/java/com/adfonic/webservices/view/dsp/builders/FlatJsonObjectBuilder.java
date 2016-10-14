package com.adfonic.webservices.view.dsp.builders;

import org.json.simple.JSONObject;

public class FlatJsonObjectBuilder implements FlatObjectBuilder<JSONObject>{
    
    JSONObject json;
    
    public FlatJsonObjectBuilder(JSONObject json) {
        this.json=json;
    }

    @Override
    public FlatObjectBuilder<JSONObject> set(String name, String value) {
        json.put(name, value);
        return this;
    }

    @Override
    public JSONObject built() {
        return json;
    }

}
