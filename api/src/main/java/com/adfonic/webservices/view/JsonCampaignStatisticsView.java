package com.adfonic.webservices.view;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.sql.dto.gen.TagGroup;
import com.adfonic.reporting.sql.dto.gen.Tagged;

@Component
public class JsonCampaignStatisticsView extends AbstractJsonView {
	@Override
	protected void renderJson(Map model, HttpServletRequest request,
			JSONObject json) {
		//one-line-fix: json.put("masg-response", getStatisticsForCampaignJSON(model.get("result")));
        List<Metric> metrics = (List<Metric>)model.get("metrics");
	    boolean unique = ((Boolean) model.get("unique")).booleanValue();
		Object result = model.get("result");
		JSONObject inner = null;

		if(result instanceof Collection<?>){
		    Collection<Tagged> resultSet=(Collection<Tagged>)result;
		    Object obj = resultSet.iterator().next();
            inner = getStatisticsForCampaignJSON((unique && obj instanceof TagGroup) ? ((TagGroup)obj).getTaggedSet() : resultSet, metrics);
		} else {
	        if (unique) {
	            Map<?, ?> resultMap=(Map<?, ?>)result;
	            Set<?> entries=resultMap.entrySet();
	            inner=entries.isEmpty()
	                    ?getStatisticsForCampaignJSON(resultMap)
	                    :getStatisticsForCampaignJSON(((Map.Entry<?, ?>) entries.iterator().next()).getValue());
	        } else {
	            inner = getStatisticsForCampaignJSON(result);
	        }
		}
		
		json.put("masg-response", inner);
	}
}
