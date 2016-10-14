package com.adfonic.webservices.view;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Country;
import com.adfonic.domain.Format;
import com.adfonic.domain.Language;
import com.adfonic.domain.Publication;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.sql.dto.gen.OrderedStatistics;
import com.adfonic.reporting.sql.dto.gen.StatisticsBasicDto;
import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.reporting.sql.dto.gen.TaggedTagGroup;
import com.adfonic.util.XmlWriter;
import com.adfonic.webservices.util.Reporting;
import com.google.common.base.CaseFormat;

public abstract class AbstractXmlView extends BaseAbstractView {

	public String getContentType() {
        return "text/xml";
    }
    
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        XmlWriter xml = new XmlWriter(response.getOutputStream());
        xml.startDoc();
        renderXml(model, request, xml);
        xml.endDoc();
    }
    
    protected abstract void renderXml(Map model, HttpServletRequest request, XmlWriter xml);

    protected void writeAdSpace(AdSpace adSpace, XmlWriter xml) {
        xml.startTag("adslot").newLine();
        xml.startTag("id").text(adSpace.getExternalID()).endTag(true);
        xml.startTag("publication").text(adSpace.getPublication().getExternalID()).endTag(true);
        xml.startTag("name").text(adSpace.getName()).endTag(true);
        xml.startTag("formats");
        int fmtCount = 0;
        for (Format format : adSpace.getFormats()) {
            if (fmtCount++ > 0) {
                xml.text(",");
            }
            xml.text(format.getSystemName());
        }
        xml.endTag(true); // formats
        xml.endTag(true); // adslot
    }
    
    protected void writePublication(Publication pub, XmlWriter xml) {
        xml.startTag("publication").newLine();
        xml.startTag("id").text(pub.getExternalID()).endTag(true);
        xml.startTag("name").text(pub.getName()).endTag(true);
        xml.startTag("description");
        if (pub.getDescription() != null && !"".equals(pub.getDescription())) {
            xml.text(pub.getDescription());
        }
        xml.endTag(true); // description
        xml.startTag("type").text(pub.getPublicationType().getSystemName()).endTag(true);
        xml.startTag("reference");
        if (pub.getReference() != null) {
            xml.text(pub.getReference());
        }
        xml.endTag(true); // reference
        xml.startTag("url");
        if (pub.getURLString() != null && !"".equals(pub.getURLString())) {
            xml.text(pub.getURLString());
        }
        xml.endTag(true); // url
        xml.startTag("transparent").text(pub.getTransparentNetwork() != null).endTag(true);
        xml.startTag("requests");
        // MAD-918 requests and users are not used anymore and the columns are deleted from PUBLICATION table
        // once we review the process of versioning we should create new service for this
        xml.endTag(true); // requests
        xml.startTag("uniques");
        // MAD-918 requests and users are not used anymore and the columns are deleted from PUBLICATION table
        // once we review the process of versioning we should create new service for this
        xml.endTag(true); // uniques
        xml.startTag("languages");
        int langCount = 0;
        for (Language language : pub.getLanguages()) {
            if (langCount++ > 0) {
                xml.text(",");
            }
            xml.text(language.getISOCode());
        }
        xml.endTag(true); // languages
        xml.startTag("autoapprove").text(pub.isAutoApproval()).endTag(true);
        xml.startTag("status").text(pub.getStatus()).endTag(true);
        xml.endTag(true); // publication
    }

    /*
    protected void writeUser(User usr, XmlWriter xml) {
    	xml.startTag("user").newLine();
        xml.startTag("email").text(usr.getEmail()).endTag(true);
        xml.startTag("firstName").text(StringUtils.isEmpty(usr.getFirstName()) ? "" : usr.getFirstName()).endTag(true);
        xml.startTag("lastName").text(StringUtils.isEmpty(usr.getLastName()) ? "" : usr.getLastName()).endTag(true);
        xml.startTag("phoneNumber").text(StringUtils.isEmpty(usr.getPhoneNumber()) ? "" : usr.getPhoneNumber()).endTag(true);
        xml.startTag("country").text(usr.getCountry() == null ? "" : usr.getCountry().getName()).endTag(true);
        xml.startTag("status").text(usr.getStatus().toString()).endTag(true);
        xml.startTag("lastLogin").text(usr.getLastLogin() == null ? "" : DATE_TIME_FORMAT.get().format(usr.getLastLogin())).endTag(true);
    	xml.endTag(true); // user
    }
    */
    
    protected void writeAdvertiser(Advertiser adv, XmlWriter xml) {
        xml.startTag("advertiser").newLine();
        xml.startTag("id").text(adv.getExternalID()).endTag(true);
        xml.startTag("name").text(adv.getName()).endTag(true);
        xml.startTag("status").text(adv.getStatus().toString()).endTag(true);
        xml.startTag("dailyBudget").text(adv.getDailyBudget() == null ? "" : CURRENCY_FORMAT.get().format(adv.getDailyBudget())).endTag(true);
        xml.startTag("balance").text(CURRENCY_FORMAT.get().format(adv.getAccount().getBalance())).endTag(true);
        xml.startTag("notifyLimit").text(adv.getNotifyLimit() == null ? "" : CURRENCY_FORMAT.get().format(adv.getNotifyLimit())).endTag(true);
        /*
        xml.startTag("users");
        Set<User> users = adv.getUsers();
        if(users != null) {
        	for(User user : users) {
        		writeUser(user, xml);
        	}
        }
        xml.endTag(true); //users
        */
        xml.endTag(true); // advertiser
    }
    
    protected void writeCampaign(Campaign cam, XmlWriter xml) {
        xml.startTag("campaign").newLine();
        xml.startTag("id").text(cam.getExternalID()).endTag(true);
        xml.startTag("name").text(cam.getName()).endTag(true);
        xml.startTag("status").text(cam.getStatus().toString()).endTag(true);
        xml.startTag("description").text(StringUtils.isEmpty(cam.getDescription()) ? "" : cam.getDescription()).endTag(true);
        xml.startTag("advertiser").text(cam.getAdvertiser().getExternalID()).endTag(true);
        xml.startTag("startDate").text(cam.getStartDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getStartDate())).endTag(true);
        xml.startTag("endDate").text(cam.getEndDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getEndDate())).endTag(true);
        xml.startTag("activationDate").text(cam.getActivationDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getActivationDate())).endTag(true);
        xml.startTag("deactivationDate").text(cam.getDeactivationDate() == null ? "" : DATE_TIME_FORMAT.get().format(cam.getDeactivationDate())).endTag(true);
        BudgetType budgetType = (BudgetType) ObjectUtils.defaultIfNull(cam.getBudgetType(), BudgetType.MONETARY);
        xml.startTag("budgetType").text(budgetType.toString()).endTag(true);
        xml.startTag("dailyBudget").text(formattedBudget(cam.getDailyBudget(), budgetType)).endTag(true);
        xml.startTag("overallBudget").text(formattedBudget(cam.getOverallBudget(), budgetType)).endTag(true);
        xml.startTag("dailyBudgetWeekday").text(formattedBudget(cam.getDailyBudgetWeekday())).endTag(true);
        xml.startTag("dailyBudgetWeekend").text(formattedBudget(cam.getDailyBudgetWeekend())).endTag(true);
        
        xml.endTag(true); // campaign
    }

    protected void writeStatisticsForPublisherInner(Object[] statistics, String country, XmlWriter xml) {
        xml.startTag("statistics").newLine();
        if (country != null) {
            xml.startTag("country").text(country).endTag(true);
        }
        xml.startTag("requests").text(makeInt(statistics[0])).endTag(true);
        xml.startTag("impressions").text(makeInt(statistics[1])).endTag(true);
        xml.startTag("clicks").text(makeInt(statistics[2])).endTag(true);
        xml.startTag("earnings").newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(statistics[3])).endTag(true);
        xml.endTag(true); // earnings
        xml.endTag(true); // statistics
    }

    public void writeStatisticsForPublisher(Object object, XmlWriter xml) {
        if (object instanceof Map) {
            Map<?,?> map = (Map) object;
            for (Map.Entry<?,?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof Publication) {
                    xml.startTag("publication").newLine();
                    xml.startTag("id").text(((Publication) key).getExternalID()).endTag(true);
                    writeStatisticsForPublisher(entry.getValue(), xml);
                    xml.endTag(true); // publication
                } else if (key instanceof AdSpace) {
                    xml.startTag("adslot").newLine();
                    xml.startTag("id").text(((AdSpace) key).getExternalID()).endTag(true);
                    writeStatisticsForPublisher(entry.getValue(), xml);
                    xml.endTag(true); // adslot
                } else if (key instanceof Country) {
                    writeStatisticsForPublisherInner((Object[]) entry.getValue(), ((Country) key).getIsoCode(), xml);
                } else if (key instanceof String) {
                    String keyS = (String) key;
                    if (keyS.startsWith(Reporting.getIdPfx(true))) {
                        xml.startTag("publication").newLine();
                    } else if (keyS.startsWith(Reporting.getIdPfx(false))) {
                        xml.startTag("adslot").newLine();
                    } else {
                        continue;
                    }
                    xml.startTag("id").text(keyS.substring(Reporting.getPfxIdLen())).endTag(true);
                    writeStatisticsForPublisher(entry.getValue(), xml);
                    xml.endTag(true);
                } // if you are adding on, look at the continue; din't invent it myself
            }
        } else if (object instanceof Object[]) {
            writeStatisticsForPublisherInner((Object[]) object, null, xml);
        }
    }
    

    public void writeStatisticsForPublisher(Collection<Tagged> taggedList, XmlWriter xml) {
        for (Tagged tagged : taggedList) {
            Tag tag = tagged.getTag();
            if (tagged instanceof TaggedTagGroup) {
                startTag(tag, xml);
                    writeStatisticsForPublisher(((TaggedTagGroup) tagged).getTaggedSet(), xml);
                xml.endTag(true);
            } else if (tagged instanceof StatisticsBasicDto) {
                if (tag.getKey() == TAG.COUNTRY) { // country has special xml serialization
                    writeStatisticsForPublisherInner(((StatisticsBasicDto) tagged).getStatistics().asObjectArray(), tag.getValue(), xml);
                } else {
                    startTag(tag, xml);
                        writeStatisticsForPublisherInner(((StatisticsBasicDto) tagged).getStatistics().asObjectArray(), null, xml);
                    xml.endTag(true);
                }
            }

        }
    }

    
    private void startTag(Tag tag, XmlWriter xml){
        xml.startTag(tag.getKey().displayElement()).newLine();
        xml.startTag(tag.getKey().displayId()).text(tag.getValue()).endTag(true);
    }
    
    protected void writeStatisticsForCampaignInner(Object[] statistics, XmlWriter xml) {
        xml.startTag("statistics").newLine();
        xml.startTag("impressions").text(makeInt(statistics[0])).endTag(true);
        xml.startTag("clicks").text(makeInt(statistics[1])).endTag(true);
        xml.startTag("conversions").text(makeInt(statistics[2])).endTag(true);
        xml.startTag("conversionsPercent").text(PERCENTAGE_FORMAT.get().format(makeDouble(statistics[3]) * 100.0)).endTag(true);

        xml.startTag("CTR").newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(statistics[4])).endTag(true);
        xml.endTag(true); // CTR
        
        xml.startTag("ECPM").newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(statistics[5])).endTag(true);
        xml.endTag(true); // ECPM

        xml.startTag("costPerConversion").newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(statistics[6])).endTag(true);
        xml.endTag(true); // cost per conversion
        
        xml.startTag("spend").newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(statistics[7])).endTag(true);
        xml.endTag(true); // spend
        
        xml.endTag(true); // statistics
    }

    public void writeStatisticsForCampaign(Object object, XmlWriter xml, List<Metric> metrics) {
        if (object instanceof Map) {
            Map<?,?> map = (Map) object;
            for (Map.Entry<?,?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof Campaign) {
                    xml.startTag("campaign").newLine();
                    xml.startTag("id").text(((Campaign) key).getExternalID()).endTag(true);
                    writeStatisticsForCampaign(entry.getValue(), xml, metrics);
                    xml.endTag(true); // campaign
                }
            }
        } else if (object instanceof Object[]) {
        	writeStatisticsForCampaignInner((Object[]) object, xml);
        }
    }

    protected void writeStatisticsForCampaignInner(OrderedStatistics orderedStats, XmlWriter xml, List<Metric> metrics) {
        Object[] statistics=orderedStats.asObjectArray();
        xml.startTag("statistics").newLine();
        if(metrics==null) {
            xml.startTag("impressions").text(makeInt(statistics[0])).endTag(true);
            xml.startTag("clicks").text(makeInt(statistics[1])).endTag(true);
            xml.startTag("conversions").text(makeInt(statistics[2])).endTag(true);
            writeCurrency("spend", statistics[7], xml);
            writePercentage("CTR", statistics[4], xml);
            writePercentage("conversionsPercent", statistics[3], xml);
            writeCurrency("ECPM", statistics[5], xml);
            writeCurrency("ECPC", statistics[8], xml); // *new_stat*
            writeCurrency("costPerConversion", statistics[6], xml);            
        } else {
            for (Metric metric : metrics) {
                String metricName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metric.name());
                // TODO - encapsulate Metric into OrderedStatistics
                Object value = orderedStats.getStatisticByName(metricName);
                switch (metric.getType()) {
                    case INTEGER:
                        xml.startTag(metricName).text(makeInt(value)).endTag(true);
                        break;
                    case PERCENT:
                        writePercentage(metricName, value, xml);
                        break;
                    case CURRENCY:
                        writeCurrency(metricName, value, xml);
                        break;
                    default:
                        xml.startTag(metricName).text(value.toString()).endTag(true);
                        break;
                }
            }
        }

        xml.endTag(true); // statistics
    }
    
    private void writeCurrency(String stat, Object value, XmlWriter xml){
        xml.startTag(stat).newLine();
        xml.startTag("currency").text("USD").endTag(true);
        xml.startTag("amount").text(CURRENCY_FORMAT.get().format(value)).endTag(true);
        xml.endTag(true); // spend
    }
    
    private void writePercentage(String stat, Object value, XmlWriter xml){
        xml.startTag(stat).text(PERCENTAGE_FORMAT.get().format(makeDouble(value) * 100.0)).endTag(true);
    }
    
    // TODO - similar to writeStatisticsForPublisher - merge both
    public void writeStatisticsForCampaign(Collection<Tagged> taggedList, XmlWriter xml, List<Metric> metrics) {
        
        for (Tagged tagged : taggedList) {
            Tag tag = tagged.getTag();
            
            if (tagged instanceof TaggedTagGroup) {
                startTag(tag, xml);
                writeStatisticsForCampaign(((TaggedTagGroup) tagged).getTaggedSet(), xml, metrics);
                xml.endTag(true);
            } else if (tagged instanceof StatisticsBasicDto) {
                startTag(tag, xml);
                writeStatisticsForCampaignInner(((StatisticsBasicDto) tagged).getStatistics(), xml, metrics);
                xml.endTag(true);
            }
        }
    }

}
