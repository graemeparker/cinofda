package com.adfonic.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Named;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment;
import com.adfonic.domain.User;
import com.adfonic.presentation.UserInterfaceUtils;
import com.adfonic.util.AdfonicTimeZone;
import com.adfonic.util.DateUtils;
import com.adfonic.util.ValidationUtils;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

/**
 * Helper class to do things that JSF makes difficult to do. This class is a simple wrapper around methods living
 * in Managers or in Utils classes. Any change to the logic of the methods or their actual locations can be done
 * without impacting the Preesentation layer, as this class is all the Presentation concerns itself with.
 *
 * See also adfonic.tld.
 */
public class Functions {

    public static String concat(String lhs, String rhs) {
        return com.adfonic.util.StringUtils.concat(lhs, rhs);
    }

    public static Date now() {
        return DateUtils.now();
    }

    public static int currentYear() {
        return DateUtils.currentYear();
    }

    public static Date exampleDate() {
        return UserInterfaceUtils.exampleDate();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Enum enumValue(String className, String name) {
        try {
            Class<Enum> clazz = (Class<Enum>) Class.forName(className);
            return enumValue(clazz, name);
        } catch (Exception ignored) {}
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T enumValue(Class<T> clazz, String name) {
        Enum[] constants = (Enum[]) clazz.getEnumConstants();
        for (Enum en : constants) {
            if (name.equals(en.name())) return (T) en;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Set keySet(Map map) {
        if (map == null) return null;
        return map.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Set sort(Set set) {
        Set ss = new TreeSet();
        ss.addAll(set);
        return ss;
    }

    public static Set<Named> sortByName(Set<Named> set) {
        Set<Named> ss = new TreeSet<Named>(Named.COMPARATOR);
        ss.addAll(set);
        return ss;
    }

    public static String colorGradient(String colorStart, String colorEnd, double percent) {
        return UserInterfaceUtils.colorGradient(colorStart, colorEnd, percent);
    }

    public static String assetExternalID(Creative creative, String displayTypeSystemName, String componentSystemName) {
        AssetManager assetManager = AdfonicBeanDispatcher.getBean(AssetManager.class);
        if(assetManager == null) {
            throw new IllegalStateException("Cannot retrieve CreativeManager from AdfonicBeanDispatcher");
        } else {
            return assetManager.getAssetExternalIdForCreative(creative, displayTypeSystemName, componentSystemName);
        }
    }

    public static String assetText(Creative creative, String displayTypeSystemName, String componentSystemName) {
        AssetManager assetManager = AdfonicBeanDispatcher.getBean(AssetManager.class);
        if(assetManager == null) {
            throw new IllegalStateException("Cannot retrieve CreativeManager from AdfonicBeanDispatcher");
        } else {
            return assetManager.getAssetTextForCreative(creative, displayTypeSystemName, componentSystemName);
        }
    }

    public static boolean isValidEmail(String value) {
        return ValidationUtils.isValidEmailAddress(value);
    }

    public static boolean isValidClickToCallNumber(String value) {
        return ValidationUtils.isValidClickToCallNumber(value);
    }

    public static boolean isValidPhoneNumber(String value)  {
        return ValidationUtils.isValidPhoneNumber(value);
    }

    public static boolean isValidURL(String url) {
        return ValidationUtils.isValidURL(url);
    }

    /**
     * Encodes the URL relative to the context.  For use where the c:url
     * tag can't be used.
     */
    // TODO: decide where this should be consolidated. Didnt want to put it into anything
    // in core or middleware, since it depends on Faces' stuff.
    public static String url(String path) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        return sc.getContextPath() + path;
    }

    public static String urlEncode(String input, String encoding) {
        return UserInterfaceUtils.urlEncode(input, encoding);
    }

    public static String capitalize(String input) {
        return com.adfonic.util.StringUtils.capitalize(input);
    }

    public static BigDecimal budgetSpend(Campaign campaign, Date date) {
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        if(campaignManager == null) {
            throw new IllegalStateException("Cannot retrieve CampaignManager from AdfonicBeanDispatcher");
        } else {
            return campaignManager.getBudgetSpendAmountForCampaign(campaign, date);
        }
    }

    public static String adfonicTimeZoneDescription(TimeZone tz) {
        return AdfonicTimeZone.getAdfonicTimeZoneDescription(tz);
    }

    public static String toThousandsString(Integer value) {
        return com.adfonic.util.StringUtils.toThousandsString(value);
    }

    public static boolean hasRole(User user, String roleName) {
        UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        if(userManager == null) {
            throw new IllegalStateException("Cannot retrieve CompanyManager from AdfonicBeanDispatcher");
        } else {
            return userManager.userHasRole(user, roleName);
        }
    }

    public static boolean hasAdminRole(AdfonicUser user, String roleName) {
        UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
        if(userManager == null) {
            throw new IllegalStateException("Cannot retrieve InternalOperationsManager from AdfonicBeanDispatcher");
        } else {
            return userManager.adfonicUserHasAdminRole(user, roleName);
        }
    }

    public static String getCalendarIcon(Date date, TimeZone timezone, String datePart) {
        return DateUtils.getCalendarIcon(date, timezone, datePart);
    }

    public static String getUICampaignStatus(Campaign c) {
        if (c == null) return null;
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        if(campaignManager == null) {
            throw new IllegalStateException("Cannot retrieve CampaignManager from AdfonicBeanDispatcher");
        } else {
            // The CampaignManager method takes care of the timePeriods hydration as well as the logic
            // that tests status then time periods.
            if(campaignManager.isCampaignCurrentlyScheduled(c)) {
                return "SCHEDULED";
            }
            return c.getStatus().toString();
        }
    }

    public static String getPlatformTargetDescription(Segment s) {
        DeviceManager deviceManager = AdfonicBeanDispatcher.getBean(DeviceManager.class);
        if(deviceManager == null) {
            throw new IllegalStateException("Cannot retrieve DeviceManager from AdfonicBeanDispatcher");
        } else {
            return deviceManager.getPlatformDeviceTargetDescription(s);
        }
    }

    public static String getCountryNamesBySegment(Segment s){
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        if(commonManager == null) {
            throw new IllegalStateException("Cannot retrieve CommonManager from AdfonicBeanDispatcher");
        }else{
            return commonManager.getCountryNamesBySegment(s);
        }
    }

    public static String getExcludedCategoryNamesBySegment (Segment s){
        CommonManager commonManager = AdfonicBeanDispatcher.getBean(CommonManager.class);
        if(commonManager == null) {
            throw new IllegalStateException("Cannot retrieve CommonManager from AdfonicBeanDispatcher");
        }else{
            return commonManager.getExcludedCategoryNamesBySegment(s);
        }
    }

    /*
     * Publishers don't have emails, snag one for menu display from company.users
     */
    public static String getPublisherEmail(Publisher publisher) {
        if (publisher != null && CollectionUtils.isNotEmpty(publisher.getCompany().getUsers())) {
            return publisher.getCompany().getUsers().iterator().next().getEmail();
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    public static <T> List<T> asList(Collection<T> collection) {
        if (collection instanceof List) {
            return (List<T>)collection;
        } else {
            return new ArrayList<T>(collection);
        }
    }
}
