package com.adfonic.beans;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Browser;
import com.adfonic.domain.Capability;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Country;
import com.adfonic.domain.CreativeAttribute;
import com.adfonic.domain.Format;
import com.adfonic.domain.Language;
import com.adfonic.domain.Model;
import com.adfonic.domain.Named;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.domain.Platform;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.Region;
import com.adfonic.domain.Vendor;
import com.adfonic.presentation.FacesUtils;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.ibm.icu.util.TimeZone;

/**
 * A read-only bean that retrieves and caches the system's reference
 * data.  Each type is lazy loaded and can be flushed if necessary.
 *
 * Access as a map:  rootBean.selectItems['Model']
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class RootBean extends BaseBean {
    
    @ManagedProperty(value="#{tools2AdminURL}")
    private String tools2AdminURL;
    
    @ManagedProperty(value="#{adserverApiURL}")
    private String adserverApiURL;
    
    @ManagedProperty(value="#{adserverApiAdSpace}")
    private String adserverApiAdSpace;
    
    @ManagedProperty(value="#{madisonAdminURL}")
    private String madisonAdminURL;
    
    @ManagedProperty(value="#{companyName}")
    private String companyName;
    
    @ManagedProperty(value="#{companyLegalName}")
    private String companyLegalName;

    // This is the list of choices
    public static final Class<?>[] TYPES = new Class[] {
        Browser.class,
        Channel.class,
        Capability.class,
        Category.class,
        CreativeAttribute.class,
        Country.class,
        Format.class,
        Language.class,
        Model.class,
        Operator.class,
        Platform.class,
        PublicationType.class,
        Region.class,
        Vendor.class
    };

    private static final FetchStrategy MULTI_PURPOSE_FS;
    static {
        FetchStrategyBuilder bld = new FetchStrategyBuilder();
        for (Class<?> clazz : TYPES) {
            bld.addAllLeft(clazz)
                .recursive(clazz, false);
        }
        MULTI_PURPOSE_FS = bld.build();
    }

    // publi-sher/cation blockable bid types
    private static final List<BidType> PUB_BLOCKABLE_BID_TYPES = new ArrayList<BidType>();
    static {
        List<BidType> bidTypes = new ArrayList<BidType>(Arrays.asList(BidType.values()));
        // don't provide CPM as a choice
        bidTypes.remove(BidType.CPM);
        PUB_BLOCKABLE_BID_TYPES.addAll(bidTypes);
    }
    
    private static final List<OptimisationReportFields> OPTIMISATION_REPORT_FIELDS = new ArrayList<>();
    static {
    	OPTIMISATION_REPORT_FIELDS.addAll(Arrays.asList(OptimisationReportFields.values()));
    }
    
    // Prefix to prepend the map accessor argument in order
    // to derive class names
    public static final String PREFIX = "com.adfonic.domain.";

    private Map<Class<?>,List<SelectItem>> selectItemsMap;
    private List<SelectItem> allTimeZones;
    private Map<Country,List<SelectItem>> timeZoneMap;
    private Map<Country,Map<String,BigDecimal>> countryMinBidMap;

    // This is a virtual map used as the accessor.
    private Map<String,List<SelectItem>> selectItemsVirtual;

    private static RootBean instance;
    public static RootBean getInstance() { return instance; }

    public RootBean() {
        instance = this;
    }

    @PostConstruct
    public void init() {
        selectItemsVirtual = new AbstractMap<String, List<SelectItem>>() {
            @SuppressWarnings("unchecked")
			@Override public List<SelectItem> get(Object classNameObj) {
                List<SelectItem> result = null;
                String className = PREFIX + classNameObj.toString();
                try {
                    Class<Named> clazz = (Class<Named>) Class.forName(className).asSubclass(Named.class);
                    if (selectItemsMap.containsKey(clazz)) {
                        synchronized (clazz) {
                            while ((result = selectItemsMap.get(clazz)) == null) {
                                loadType(clazz);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                            "Failed to load " + className,
                            e);
                }
                return result;
            }

            @Override public Set<Map.Entry<String, List<SelectItem>>> entrySet() {
                // Not worth implementing at the moment;  this Map is only
                // accessed from
                return Collections.emptySet();
            }
        };

        selectItemsMap = new HashMap<Class<?>,List<SelectItem>>();

        // Initialize map keys; only classes in map are eligible for
        // lazy loading.
        reloadAll();

        initAllTimeZones();
        initTimeZoneMap();
        initCountryMinBidMap();
    }

    private void initAllTimeZones() {
        allTimeZones = initTimeZonesForIDs(TimeZone.getAvailableIDs());
    }

    private void initCountryMinBidMap() {
        Map<Country,Map<String,BigDecimal>> minBidMap = new HashMap<Country,Map<String,BigDecimal>>();
        loadType(Country.class);

        selectItemsMap.put(BidType.class, FacesUtils.makeEnumSelectItems(BidType.values(), true));

        for (SelectItem countrySelectItem : selectItemsMap.get(Country.class)) {
            Country country = (Country) countrySelectItem.getValue();
            Map<String,BigDecimal> bidMap = new HashMap<String,BigDecimal>();
            for (SelectItem bidTypeSelectItem : selectItemsMap.get(BidType.class)) {
                BidType bidType = (BidType) bidTypeSelectItem.getValue();
                RateCard rateCard = getPublicationManager().getRateCardByBidType(bidType);
                BigDecimal minBid = rateCard.getDefaultMinimum();
                minBid = minBid.max(rateCard.getMinimumBid(country));
                bidMap.put(bidType.getName(), minBid);
            }
            minBidMap.put(country, bidMap);
        }
        this.countryMinBidMap = minBidMap;
    }

    private static List<SelectItem> initTimeZonesForIDs(String[] tzIds) {
        Set<String> canonical = new HashSet<String>();
        Set<TimeZone> timeZonesSet = new TreeSet<TimeZone>(new Comparator<TimeZone>() {
            public int compare(TimeZone lhs, TimeZone rhs) {
                int raw = lhs.getRawOffset() - rhs.getRawOffset();
                if (raw == 0) {
                    return lhs.getID().compareTo(rhs.getID());
                }
                return raw;
            }
        });

        TimeZone.setDefaultTimeZoneType(TimeZone.TIMEZONE_JDK);
        // Populate timeZones
        for (String tzId : tzIds) {
            String c = TimeZone.getCanonicalID(tzId);
            if (canonical.add(c)) {
                TimeZone tz = TimeZone.getTimeZone(c);
                timeZonesSet.add(tz);
            }
        }

        List<SelectItem> timeZones = new ArrayList<SelectItem>(timeZonesSet.size());

        for (TimeZone tz : timeZonesSet) {
            // Convert back to standard JDK time zones
            java.util.TimeZone zt = java.util.TimeZone.getTimeZone(tz.getID());

            timeZones.add(new SelectItem(zt, zt.getID() + " (" + zt.getDisplayName(false, TimeZone.SHORT) + ": " + zt.getDisplayName(false, TimeZone.LONG) + ")"));
        }
        return timeZones;
    }

    private void initTimeZoneMap() {
        timeZoneMap = new HashMap<Country,List<SelectItem>>();
        loadType(Country.class);
        for (SelectItem si : selectItemsMap.get(Country.class)) {
            Country country = (Country) si.getValue();
            timeZoneMap.put(country, initTimeZonesForIDs(TimeZone.getAvailableIDs(country.getIsoCode())));
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Named> void loadType(Class<T> clazz) {

        List<T> list = null;
        List<SelectItem> selectItemList = null;

        if (clazz.getCanonicalName().equals(Browser.class.getCanonicalName())) {
            list = (List<T>)getDeviceManager().getAllBrowsers(
                    new Sorting(SortOrder.asc("browserOrder")), 
                    MULTI_PURPOSE_FS);
            selectItemList = FacesUtils.makeSelectItems(list, false);
        } 
        else {
            if (clazz.getCanonicalName().equals(Channel.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllChannels(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Capability.class.getCanonicalName())) {
                list = (List<T>)getDeviceManager().getAllCapabilities(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Category.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllCategories(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(CreativeAttribute.class.getCanonicalName())) {
                list = (List<T>)getCreativeManager().getAllCreativeAttributes(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Country.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllCountries(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Format.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllFormats(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Language.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllLanguages(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Model.class.getCanonicalName())) {
                list = (List<T>)getDeviceManager().getAllModels(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Operator.class.getCanonicalName())) {
                list = (List<T>)getDeviceManager().getAllOperators(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Platform.class.getCanonicalName())) {
                list = (List<T>)getDeviceManager().getAllPlatforms(MULTI_PURPOSE_FS);
            }
            else if(clazz.getCanonicalName().equals(Region.class.getCanonicalName())) {
                list = (List<T>)getCommonManager().getAllRegions(MULTI_PURPOSE_FS);
            } 
            else if(clazz.getCanonicalName().equals(Vendor.class.getCanonicalName())) {
                list = (List<T>)getDeviceManager().getAllVendors(MULTI_PURPOSE_FS);
            }
            
            Set<T> set = new HashSet<T>();
            set.addAll(list);
            list = new ArrayList<T>();
            list.addAll(set);

            selectItemList = FacesUtils.makeSelectItems(list, true);
        }
        selectItemsMap.put(clazz, selectItemList);
    }

    // Java accessor
    public List<SelectItem> getSelectItems(Class<? extends Named> clazz) {
        List<SelectItem> result = null;
        if (selectItemsMap.containsKey(clazz)) {
            synchronized (clazz) {
                while ((result = selectItemsMap.get(clazz)) == null) {
                    loadType(clazz);
                }
            }
        }
        return result;
    }

    // Accessor for JSF
    public Map<String,List<SelectItem>> getSelectItems() {
        return selectItemsVirtual;
    }

    // For cache management
    public void reload(Class<?> clazz) {
        synchronized (clazz) {
            selectItemsMap.put(clazz, null);
        }
    }

    public void reloadAll() {
        for (Class<?> c : TYPES) {
            reload(c);
        }
    }

    // Time Zone list
    public List<SelectItem> getAllTimeZones() {
        return allTimeZones;
    }

    public Map<Country,List<SelectItem>> getTimeZones() {
        return timeZoneMap;
    }

    public Map<Country,Map<String,BigDecimal>> getCountryMinBids() {
        return countryMinBidMap;
    }
    
    public String getTools2AdminURL() {
        return tools2AdminURL;
    }

    public void setTools2AdminURL(String tools2AdminURL) {
        this.tools2AdminURL = tools2AdminURL;
    }

	// added for admin page access
    public Class<?>[] getClasses() {
        return TYPES;
    }
    
    public List<BidType> getPubBlockableBidTypes() {
        return PUB_BLOCKABLE_BID_TYPES;
    }
    
    public List<OptimisationReportFields> getOptimisationReportFields() {
    	return OPTIMISATION_REPORT_FIELDS;
    }
    
    public String getAdserverApiURL() {
        return adserverApiURL;
    }

    public void setAdserverApiURL(String adserverApiURL) {
        this.adserverApiURL = adserverApiURL;
    }

    public String getAdserverApiAdSpace() {
        return adserverApiAdSpace;
    }

    public void setAdserverApiAdSpace(String adserverApiAdSpace) {
        this.adserverApiAdSpace = adserverApiAdSpace;
    }

    public String getMadisonAdminURL() {
        return madisonAdminURL;
    }

    public void setMadisonAdminURL(String madisonAdminURL) {
        this.madisonAdminURL = madisonAdminURL;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }
    
}
