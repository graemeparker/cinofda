package com.adfonic.webservices.view;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.reporting.sql.dto.gen.TagGroup;
import com.adfonic.reporting.sql.dto.gen.Tagged;

@Component
public class JsonPublisherStatisticsView extends AbstractJsonView {
    @Override
    protected void renderJson(Map model, HttpServletRequest request, JSONObject json) {
	boolean unique = ((Boolean) model.get("unique")).booleanValue();
    JSONObject inner = null;

    Object result = model.get("result");

    if (result instanceof Set<?>) { // overloading being static, need this anyway
        Set<Tagged> resultSet=(Set<Tagged>)result;
        if (unique) {
            inner = getStatisticsForPublisherJSON(((TagGroup)resultSet.iterator().next()).getTaggedSet());
        } else {
            inner = getStatisticsForPublisherJSON(resultSet);
        }
        
    }else if(result instanceof Map<?, ?>){
        Map<?, ?> resultMap=(Map<?, ?>)result;
        if (unique) {
            Object key = resultMap.keySet().iterator().next();
            Object value = resultMap.get(key);
            inner = getStatisticsForPublisherJSON(value);
        } else {
            inner = getStatisticsForPublisherJSON(resultMap);
        }
    }

    json.put("masg-response", inner);
    }
}
