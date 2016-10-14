package com.adfonic.webservices.view;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Country;
import com.adfonic.domain.Format;
import com.adfonic.domain.HasExternalID;
import com.adfonic.domain.Language;
import com.adfonic.domain.Publication;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.sql.dto.gen.OrderedStatistics;
import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.reporting.sql.dto.gen.TaggedTagGroup;
import com.adfonic.webservices.util.Reporting;
import com.google.common.base.CaseFormat;

@SuppressWarnings("unchecked")
public abstract class AbstractJsonView extends BaseAbstractView  {
    public String getContentType() {
        return "application/json";
    }
    
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        renderJson(model, request, json);
        response.setContentType(getContentType());
        json.writeJSONString(response.getWriter());
    }
    
    protected abstract void renderJson(Map model, HttpServletRequest request, JSONObject json);

    protected JSONObject getAdSpaceJSON(AdSpace adSpace) {
        JSONObject json = new JSONObject();
        json.put("id", adSpace.getExternalID());
        json.put("publication", adSpace.getPublication().getExternalID());
        json.put("name", adSpace.getName());
        if (!adSpace.getFormats().isEmpty()) {
            StringBuilder bld = new StringBuilder();
            for (Format format : adSpace.getFormats()) {
                if (bld.length() > 0) {
                    bld.append(',');
                }
                bld.append(format.getSystemName());
            }
            json.put("formats", bld.toString());
        }
        return json;
    }
    
    protected JSONObject getPublicationJSON(Publication pub) {
        JSONObject json = new JSONObject();
        json.put("id", pub.getExternalID());
        json.put("name", pub.getName());
        if (pub.getDescription() != null && !"".equals(pub.getDescription())) {
            json.put("description", pub.getDescription());
        }
        json.put("type", pub.getPublicationType().getSystemName());
        if (pub.getReference() != null) {
            json.put("reference", pub.getReference());
        }
        if (pub.getURLString() != null && !"".equals(pub.getURLString())) {
            json.put("url", pub.getURLString());
        }
        json.put("transparent", pub.getTransparentNetwork() != null);
        // MAD-918 requests and users are not used anymore and the columns are deleted from PUBLICATION table
        // once we review the process of versioning we should create new service for this
        if (!pub.getLanguages().isEmpty()) {
            StringBuilder bld = new StringBuilder();
            for (Language language : pub.getLanguages()) {
                if (bld.length() > 0) {
                    bld.append(',');
                }
                bld.append(language.getISOCode());
            }
            json.put("languages", bld.toString());
        }
        json.put("autoapprove", pub.isAutoApproval());
        json.put("status", pub.getStatus().name());
        return json;
    }

    /*
    protected JSONObject getUserJSON(User usr) {
        JSONObject json = new JSONObject();
        json.put("email",usr.getEmail());
        json.put("firstName",(StringUtils.isEmpty(usr.getFirstName()) ? "" : usr.getFirstName()));
        json.put("lastName",(StringUtils.isEmpty(usr.getLastName()) ? "" : usr.getLastName()));
        json.put("phoneNumber",(StringUtils.isEmpty(usr.getPhoneNumber()) ? "" : usr.getPhoneNumber()));
        json.put("country",(usr.getCountry() == null ? "" : usr.getCountry().getName()));
        json.put("status",usr.getStatus().toString());
        json.put("lastLogin",(usr.getLastLogin() == null ? "" : DATE_TIME_FORMAT.get().format(usr.getLastLogin())));
        return json;
    }
    */

    protected JSONObject getAdvertiserJSON(Advertiser adv) {
        JSONObject json = new JSONObject();
        json.put("id",adv.getExternalID());
        json.put("name",adv.getName());
        json.put("status",adv.getStatus().toString());
        json.put("dailyBudget",(adv.getDailyBudget() == null ? "" : CURRENCY_FORMAT.get().format(adv.getDailyBudget())));
        json.put("balance",CURRENCY_FORMAT.get().format(adv.getAccount().getBalance()));
        json.put("notifyLimit",(adv.getNotifyLimit() == null ? "" : CURRENCY_FORMAT.get().format(adv.getNotifyLimit())));
        return json;
    }
    
    protected JSONObject getCampaignJSON(Campaign cam) {
        JSONObject json = new JSONObject();
        json.put("id",cam.getExternalID());
        json.put("name",cam.getName());
        json.put("status",cam.getStatus().toString());
        json.put("description",StringUtils.isEmpty(cam.getDescription()) ? "" : cam.getDescription());
        json.put("advertiser",cam.getAdvertiser().getExternalID());
        json.put("startDate",cam.getStartDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getStartDate()));
        json.put("endDate",cam.getEndDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getEndDate()));
        json.put("activationDate",cam.getActivationDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getActivationDate()));
        json.put("deactivationDate",cam.getDeactivationDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getDeactivationDate()));
        BudgetType budgetType = (BudgetType) ObjectUtils.defaultIfNull(cam.getBudgetType(), BudgetType.MONETARY);
        json.put("budgetType", budgetType.toString());
        json.put("dailyBudget", formattedBudget(cam.getDailyBudget(), budgetType));
        json.put("overallBudget", formattedBudget(cam.getOverallBudget(), budgetType));
        json.put("dailyBudgetWeekday", formattedBudget(cam.getDailyBudgetWeekday()));
        json.put("dailyBudgetWeekend", formattedBudget(cam.getDailyBudgetWeekend()));
        return json;
    }


    // Adds key "statistics", value {...} to argument
    protected JSONObject getStatisticsInnerForPublisher(Object[] statistics) {
        JSONObject statsJson = new JSONObject();

        statsJson.put("requests", makeInt(statistics[0]));
        statsJson.put("impressions", makeInt(statistics[1]));
        statsJson.put("clicks", makeInt(statistics[2]));

        JSONObject earningsJson = new JSONObject();
        earningsJson.put("currency", "USD");
        earningsJson.put("amount", CURRENCY_FORMAT.get().format(statistics[3]));

        statsJson.put("earnings", earningsJson);

        return statsJson;
    }

    public JSONObject getStatisticsForPublisherJSON(Object object) {
        if (object instanceof Object[]) {
            return getStatisticsInnerForPublisher((Object[]) object);
        } 
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map<?,?> map = (Map) object;
            for (Map.Entry<?,?> entry : map.entrySet()) {
                Object key = entry.getKey();
                String keyStr = null;
                if (key instanceof HasExternalID) {
                    keyStr = ((HasExternalID) key).getExternalID();
                } else if (key instanceof Country) {
                    keyStr = ((Country) key).getIsoCode();
                } else if (key instanceof String && ((String)key).substring(1, Reporting.getPfxIdLen()).equals(Reporting.getPfxIdSfx())) {
                    keyStr = ((String)key).substring(Reporting.getPfxIdLen());
                }
                json.put(keyStr, getStatisticsForPublisherJSON(entry.getValue()));
            }
            return json;
        }
        // Shouldn't reach here
        throw new IllegalArgumentException();
    }

    public JSONObject getStatisticsForPublisherJSON(Collection<Tagged> taggedSet) {
        JSONObject json = new JSONObject();
        for (Tagged tagged : taggedSet) {
            Tag tag = tagged.getTag();
            if (tagged instanceof TaggedTagGroup) {
                json.put(tag.getValue(), getStatisticsForPublisherJSON(((TaggedTagGroup) tagged).getTaggedSet()));
            } else if (tagged instanceof StatisticsBasicDto) {
                json.put(tag.getValue(), getStatisticsInnerForPublisher(((StatisticsBasicDto) tagged).getStatistics().asObjectArray()));
            }
        }
        return json;
    }

    // Adds key "statistics", value {...} to argument
    protected JSONObject getStatisticsInnerForCampaign(Object[] statistics) {
        JSONObject statsJson = new JSONObject();

        statsJson.put("impressions", makeInt(statistics[0]));
        statsJson.put("clicks", makeInt(statistics[1]));
        statsJson.put("conversions", makeInt(statistics[2]));
        statsJson.put("conversionsPercent", PERCENTAGE_FORMAT.get().format(makeDouble(statistics[3]) * 100.0));

        JSONObject ctrJson = new JSONObject();
        ctrJson.put("currency", "USD");
        ctrJson.put("amount", CURRENCY_FORMAT.get().format(statistics[4]));
        statsJson.put("CTR", ctrJson);

        JSONObject ecpmJson = new JSONObject();
        ecpmJson.put("currency", "USD");
        ecpmJson.put("amount", CURRENCY_FORMAT.get().format(statistics[5]));
        statsJson.put("ECPM", ecpmJson);

        JSONObject costPerConversionJson = new JSONObject();
        costPerConversionJson.put("currency", "USD");
        costPerConversionJson.put("amount", CURRENCY_FORMAT.get().format(statistics[6]));
        statsJson.put("costPerConversion", costPerConversionJson);

        JSONObject spendJson = new JSONObject();
        spendJson.put("currency", "USD");
        spendJson.put("amount", CURRENCY_FORMAT.get().format(statistics[7]));
        statsJson.put("spend", spendJson);

       return statsJson;
    }

    public JSONObject getStatisticsForCampaignJSON(Object object) {
        if (object instanceof Object[]) {
            return getStatisticsInnerForCampaign((Object[]) object);
        } 
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map<?,?> map = (Map) object;
            for (Map.Entry<?,?> entry : map.entrySet()) {
                Object key = entry.getKey();
                String keyStr = null;
                if (key instanceof HasExternalID) {
                    keyStr = ((HasExternalID) key).getExternalID();
                } else if (key instanceof Country) {
                    keyStr = ((Country) key).getIsoCode();
                }
                json.put(keyStr, getStatisticsForCampaignJSON(entry.getValue()));
            }
            return json;
        }
        // Shouldn't reach here
        throw new IllegalArgumentException();
    }

    protected JSONObject getStatisticsInnerForCampaign(OrderedStatistics orderedStats, List<Metric> metrics) {
        Object[] statistics=orderedStats.asObjectArray();
        JSONObject statsJson = new JSONObject();

        if(metrics==null) {
            statsJson.put("impressions", makeInt(statistics[0]));
            statsJson.put("clicks", makeInt(statistics[1]));
            statsJson.put("conversions", makeInt(statistics[2]));
            statsJson.put("spend", currencyJSON(statistics[7]));
            statsJson.put("CTR", PERCENTAGE_FORMAT.get().format(makeDouble(statistics[4]) * 100.0));
            statsJson.put("conversionsPercent", PERCENTAGE_FORMAT.get().format(makeDouble(statistics[3]) * 100.0));
            statsJson.put("ECPM", currencyJSON(statistics[5]));
            statsJson.put("ECPC", currencyJSON(statistics[8])); // *new_stat*
            statsJson.put("costPerConversion", currencyJSON(statistics[6]));
        } else {
            for (Metric metric : metrics) {
                String metricName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metric.name());
                // TODO - encapsulate Metric into OrderedStatistics
                Object value = orderedStats.getStatisticByName(metricName);
                switch (metric.getType()) {
                    case INTEGER:
                        statsJson.put(metricName, makeInt(value));
                        break;
                    case PERCENT:
                        statsJson.put(metricName, PERCENTAGE_FORMAT.get().format(makeDouble(value) * 100.0));
                        break;
                    case CURRENCY:
                        statsJson.put(metricName, currencyJSON(value));
                        break;
                    default:
                        statsJson.put(metricName, value.toString());
                        break;
                }
            }
        }
        
       return statsJson;
    }

    private JSONObject currencyJSON(Object usd) {
        JSONObject currencyJSON = new JSONObject();
        currencyJSON.put("currency", "USD");
        currencyJSON.put("amount", CURRENCY_FORMAT.get().format(usd));
        return currencyJSON;
    }

    // TODO - similar to getStatisticsForPublisherJSON - merge both
    public JSONObject getStatisticsForCampaignJSON(Collection<Tagged> taggedSet, List<Metric> metrics) {
        JSONObject json = new JSONObject();
        for (Tagged tagged : taggedSet) {
            Tag tag = tagged.getTag();
            if (tagged instanceof TaggedTagGroup) {
                json.put(tag.getValue(), getStatisticsForCampaignJSON(((TaggedTagGroup) tagged).getTaggedSet(), metrics));
            } else if (tagged instanceof StatisticsBasicDto) {
                json.put(tag.getValue(), getStatisticsInnerForCampaign(((StatisticsBasicDto) tagged).getStatistics(), metrics));
            }
        }
        return json;
    }

}
