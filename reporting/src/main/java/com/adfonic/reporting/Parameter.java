package com.adfonic.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import mondrian.olap.Member;

import org.apache.commons.collections.CollectionUtils;

import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Format;
import com.adfonic.domain.HasPrimaryKeyId;
import com.adfonic.domain.Model;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.Vendor;
import com.adfonic.util.Range;

/**
 * Parameters to be used either as columns or filters in an OLAP query.
 * Used by the OLAPQuery class among others.  This probably needs some
 * work to better differentiate the ones that are used with the Aggregate()
 * function as artificial members that can then be used in the where clause,
 * and the ones that perform the simpler function of enabling a CrossJoin.
 * Right now the same class hierarchy is used for both types and the naming
 * conventions leave a bit to be desired.
 *
 * This class also allows each parameter to do some marshaling and demarshaling
 * of arguments in order to convert between the Java object model and the cube
 * date model, particularly for report formatting purposes.
 */
public abstract class Parameter {
    protected String mdx;
    protected String dimension;

    @Override
    public String toString() {
        return "Parameter[mdx=" + mdx + ",dimension=" + dimension + "]";
    }

    public String getMDX() { return mdx; }
    public String getDimension() { return dimension; }
    public void addColumn(Report report, Locale locale) {
        report.addColumn(dimension);
    }

    /**
     * Override this if you need custom behavior
     */
    public Object extractValue(Member member) {
        return member.getCaption();
    }

    public String getWithMember() { return null; }

    public abstract static class Time extends Parameter {
        protected Calendar calendar;

        public Time(TimeZone timeZone) {
            this.calendar = Calendar.getInstance(timeZone);
        }

        public Calendar getCalendar(){
            return calendar;
        }
    }

    public static class TimeByDay extends Time {
        public TimeByDay(TimeZone timeZone, Range<Date> dateRange, String timeDimensionName) {
            super(timeZone);

            SimpleDateFormat mdxDateFormat = new SimpleDateFormat("[yyyy].[M].[d]");
            mdxDateFormat.setTimeZone(calendar.getTimeZone());

            mdx = new Formatter()
            .format("%s.%s:%s.%s",
                    timeDimensionName,
                    mdxDateFormat.format(dateRange.getStart()),
                    timeDimensionName,
                    mdxDateFormat.format(dateRange.getEnd()))
                    .toString();
            dimension = timeDimensionName;
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            df.setCalendar(calendar);
            report.addColumn("Date", Date.class, df, false);
        }

        @Override
        public Object extractValue(Member member) {
            calendar.clear();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(member.getName()));
            member = member.getParentMember();
            calendar.set(Calendar.MONTH, Integer.parseInt(member.getName()) - 1); // Calendar.MONTH starts with January = 0
            member = member.getParentMember();
            calendar.set(Calendar.YEAR, Integer.parseInt(member.getName()));
            return calendar.getTime();
        }
    } // Parameter.TimeByDay

    public static class AdvertiserTimeByDay extends TimeByDay {
        public AdvertiserTimeByDay(Company company, Range<Date> dateRange) {
            super(company.getDefaultTimeZone(), dateRange, "AdvertiserTime");
            dimension = "AdvertiserTime";
        }
        public AdvertiserTimeByDay(TimeZone companyTimeZone, Range<Date> dateRange) {
            super(companyTimeZone, dateRange, "AdvertiserTime");
            dimension = "AdvertiserTime";
        }
    }

    public static class PublisherTimeByDay extends TimeByDay {
        public PublisherTimeByDay(Company company, Range<Date> dateRange) {
            super(company.getDefaultTimeZone(), dateRange, "PublisherTime");
            dimension = "PublisherTime";
        }
    }

    public static class GmtTimeByDay extends TimeByDay {
        public GmtTimeByDay(Company company, Range<Date> dateRange) {
            super(company.getDefaultTimeZone(), dateRange, "GmtTime");
            dimension = "GmtTime";
        }

        public GmtTimeByDay(TimeZone timeZone, Range<Date> dateRange) {
            super(timeZone, dateRange, "GmtTime");
            dimension = "GmtTime";
        }
    }

    public abstract static class TimeByHour extends Time {
        public TimeByHour(TimeZone timeZone, Range<Date> dateRange, String timeDimensionName) {
            super(timeZone);
            SimpleDateFormat mdxHourlyFormat = new SimpleDateFormat("[yyyy].[M].[d].[H]");
            mdxHourlyFormat.setTimeZone(timeZone);
            mdx = new Formatter()
            .format("%s.%s:%s.%s",
                    timeDimensionName,
                    mdxHourlyFormat.format(dateRange.getStart()),
                    timeDimensionName,
                    mdxHourlyFormat.format(dateRange.getEnd()))
                    .toString();
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Hour");
        }

        @Override
        public Object extractValue(Member member) {
            String temp = "0" + member.getName() + ":00";
            if (temp.length() > 5) { temp = temp.substring(1); }
            return temp;
        }
    } // Parameter.TimeByHour

    public static class AdvertiserTimeByHour extends TimeByHour {
        public AdvertiserTimeByHour(TimeZone timeZone, Range<Date> dateRange) {
            super(timeZone, dateRange, "AdvertiserTime");
            dimension = "AdvertiserTime";
        }
    }

    public static class PublisherTimeByHour extends TimeByHour {
        public PublisherTimeByHour(TimeZone timeZone, Range<Date> dateRange) {
            super(timeZone, dateRange, "PublisherTime");
            dimension = "PublisherTime";
        }
    }

    public static class GmtTimeByHour extends TimeByHour {
        public GmtTimeByHour(TimeZone timeZone, Range<Date> dateRange) {
            super(timeZone, dateRange, "GmtTime");
            dimension = "GmtTime";
        }
    }

    public static class Operators extends Parameter {
        public Operators() {
            mdx = "Operator.Operator.members";
            dimension = "Operator";
        }

        public void addColumn(Report report, Locale locale) {
            report.addColumn("Operator");
        }
    }

    public static class Devices extends Parameter {
        private boolean groupByVendor;

        public Devices(Set<Vendor> vendors, Set<Model> models, boolean groupByVendor) {
            this.groupByVendor = groupByVendor;
            StringBuilder qbuf = new StringBuilder();
            if (CollectionUtils.isNotEmpty(vendors) || CollectionUtils.isNotEmpty(models)) {
                int i = 0;
                qbuf.append("{");
                for (Vendor vendor : vendors) {
                    if (i++ > 0) { qbuf.append(","); }
                    qbuf.append("Device.Vendor.[")
                    .append(vendor.getId())
                    .append("]");
                    if (!groupByVendor) {
                        qbuf.append(".children");
                    }
                }
                for (Model model : models) {
                    if (i++ > 0) { qbuf.append(","); }
                    qbuf.append("Device.Model.[")
                    .append(model.getId())
                    .append("]");
                    if (groupByVendor) {
                        qbuf.append(".parent");
                    }
                }
                qbuf.append("}");
            } else if (!groupByVendor) {
                qbuf.append("Device.Model.members");
            } else {
                qbuf.append("Device.Vendor.members");
            }
            mdx = qbuf.toString();
            dimension = "Device";
        }
        
        public Devices(boolean groupByVendor) {
            this.groupByVendor = groupByVendor;
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            if (groupByVendor) {
                report.addColumn("Vendor");
            } else {
                report.addColumn("Vendor");
                report.addColumn("Model");
            }
        }

        @Override
        public Object extractValue(Member member) {
            if (groupByVendor) {
                return member.getCaption();
            } else {
                return new Object[] { member.getParentMember().getCaption(), member.getCaption() };
            }
        }
    } // Parameter.Devices

    // Parameter Device this is for a SQL Query
    public static class Device extends Parameter {
    	
    	public Device(){
    		
    	}
    	
    	public void addColumn(Report report, Locale locale){
    		report.addColumn("Model");
    	}
    	
    }
    
    
    
    public static class PlatformByPlatforms extends Parameter {
        public PlatformByPlatforms(Collection<Platform> platforms) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Platform platform : platforms) {
                if (i++ > 0) {
                    sb.append(",");
                }
                sb.append("Platform.Platform.[").append(platform.getId()).append("]");
            }
            sb.append("}");
            mdx = sb.toString();
            dimension = "Platform";
        }

        public PlatformByPlatforms() {
            mdx = "Platform.Platform.members";
            dimension = "Platform";
        }

        public void addColumn(Report report, Locale locale) {
            report.addColumn("Platform");
        }
    }

    public static class LocationByCountries extends Parameter {
        public LocationByCountries(Collection<Country> countries) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Country country : countries) {
                if (i++ > 0) {
                    sb.append(",");
                }
                sb.append("Location.Country.[").append(country.getId()).append("]");
            }
            sb.append("}");
            mdx = sb.toString();
            dimension = "Location";
        }

        public LocationByCountries() {
            mdx = "Location.Country.members";
            dimension = "Location";
        }

        public void addColumn(Report report, Locale locale) {
            report.addColumn("Country");
        }
    }

    public static class LocationByRegions extends Parameter {
        public LocationByRegions() {
            mdx = "Location.Region.members";
            dimension = "Location";
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Region");
        }
    }

    public static class Campaigns extends Parameter {
        private final EntityResolver<Campaign> campaignResolver;

        public Campaigns(Collection<Campaign> campaigns, EntityResolver<Campaign> campaignResolver) {
            this.campaignResolver = campaignResolver;
            dimension = "Advertiser";
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Campaign c : campaigns) {
                if (i++ > 0) { sb.append(","); }
                sb.append("Advertiser.Campaign.[")
                .append(c.getId())
                .append("]");
            }
            sb.append("}");
            mdx = sb.toString();
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Campaign");
        }

        @Override
        public Object extractValue(Member member) {
            return campaignResolver.getEntityById(Long.valueOf(member.getName()));
        }
    }

    public static class Advertisers extends Parameter {
        private final EntityResolver<Advertiser> advertiserResolver;

        public Advertisers(Collection<Advertiser> advertisers, EntityResolver<Advertiser> advertiserResolver) {
            this.advertiserResolver = advertiserResolver;
            dimension = "Advertiser";
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Advertiser a : advertisers) {
                if (i++ > 0) {
                    sb.append(",");
                }
                sb.append("Advertiser.Advertiser.[")
                    .append(a.getId())
                    .append("]");
            }
            sb.append("}");
            mdx = sb.toString();
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Advertiser");
        }

        @Override
        public Object extractValue(Member member) {
            return advertiserResolver.getEntityById(Long.valueOf(member.getName()));
        }
    }

    public static class Publications extends Parameter {
        private final EntityResolver<Publication> publicationResolver;

        public Publications(EntityResolver<Publication> publicationResolver, Boolean isKeyOnly, Company.PublisherCategory publisherCategory) {
            this.publicationResolver = publicationResolver;
            mdx = "Publisher.Publication.Members";
            dimension = "Publisher";

            StringBuilder filterSb = new StringBuilder();
            if (isKeyOnly != null && isKeyOnly.equals(Boolean.TRUE)) {
                filterSb.append("CAST(Publisher.CurrentMember.Parent.Properties(\"isKey\") AS STRING) = 'true'");
            }

            if (publisherCategory != null) {
                if (filterSb.length() > 0) {
                    filterSb.append(" AND ");
                }
                filterSb.append("Publisher.Publication.CurrentMember.Parent.Parent.Properties(\"publisherCategory\") = '" + publisherCategory.toString() + "'");
            }

            if (filterSb.length() > 0) {
                mdx = "Filter(" + mdx + "," + filterSb.toString() + ")";
            }
        }

        public Publications(Collection<Publication> publications, EntityResolver<Publication> publicationResolver) {
            this.publicationResolver = publicationResolver;
            dimension = "Publisher";
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Publication c : publications) {
                if (i++ > 0) { sb.append(","); }
                sb.append("Publisher.Publication.[")
                .append(c.getId())
                .append("]");
            }
            sb.append("}");
            mdx = sb.toString();
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Publication");
        }

        @Override
        public Object extractValue(Member member) {
            return publicationResolver.getEntityById(Long.valueOf(member.getName()));
        }
    }

    public static class Creatives extends Parameter  {
        private final EntityResolver<Format> formatResolver;
        private final EntityResolver<Creative> creativeResolver;

        public Creatives(Company company, Advertiser advertiser, Campaign campaign, Creative creative, Format format, EntityResolver<Format> formatResolver, EntityResolver<Creative> creativeResolver) {
            this(company, advertiser, campaign, creative, format, null, formatResolver, creativeResolver);
        }

        public Creatives(Company company, Advertiser advertiser, Campaign campaign, Creative creative, Format format, Boolean houseAds, EntityResolver<Format> formatResolver, EntityResolver<Creative> creativeResolver) {
            this.formatResolver = formatResolver;
            this.creativeResolver = creativeResolver;
            dimension = "Advertiser";
            boolean filterHouseAd = false;

            if (creative != null) {
                mdx = "{Advertiser.Creative.[" + creative.getId() + "]}";
            } else if (campaign != null) {
                mdx = "Distinct(Descendants(Advertiser.Campaign.[" + campaign.getId() + "],1))";
            } else {
                if (advertiser != null) {
                    mdx = "Distinct(Descendants(Advertiser.Advertiser.[" + advertiser.getId() + "],2))";
                } else {
                    mdx = "Distinct(Descendants(Advertiser.Company.[" + company.getId() + "],3))";
                }
                // the house ads filter is only needed for a by advertiser or by company query
                if (houseAds != null) {
                    filterHouseAd = true;
                }
            }

            if (format != null || filterHouseAd) {
                boolean hasClause = false;
                StringBuilder sb = new StringBuilder();

                sb.append("Filter(").append(mdx).append(", ");

                if (format != null) {
                    sb.append("Advertiser.CurrentMember.Properties(\"format\")=").append(format.getId());
                    hasClause = true;
                }
                if (filterHouseAd) {
                    if (hasClause) {
                        sb.append(" AND ");
                    }
                    sb.append("CAST(Advertiser.CurrentMember.Parent.Properties(\"houseAd\") AS STRING) = '").append(houseAds.toString()).append("'");
                }

                sb.append(")");
                mdx = sb.toString();
            }

        }

        public Creatives(String company, String advertiser, String campaign, String creative, String format, Boolean houseAds, EntityResolver<Format> formatResolver, EntityResolver<Creative> creativeResolver) {
            this.formatResolver = formatResolver;
            this.creativeResolver = creativeResolver;
            dimension = "Advertiser";
            boolean filterHouseAd = false;

            if (creative != null) {
                mdx = "{Advertiser.Creative.[" + creative + "]}";
            } else if (campaign != null) {
                mdx = "Distinct(Descendants(Advertiser.Campaign.[" + campaign + "],1))";
            } else {
                if (advertiser != null) {
                    mdx = "Distinct(Descendants(Advertiser.Advertiser.[" + advertiser + "],2))";
                } else {
                    mdx = "Distinct(Descendants(Advertiser.Company.[" + company + "],3))";
                }
                // the house ads filter is only needed for a by advertiser or by company query
                if (houseAds != null) {
                    filterHouseAd = true;
                }
            }

            if (format != null || filterHouseAd) {
                boolean hasClause = false;
                StringBuilder sb = new StringBuilder();

                sb.append("Filter(").append(mdx).append(", ");

                if (format != null) {
                    sb.append("Advertiser.CurrentMember.Properties(\"format\")=").append(format);
                    hasClause = true;
                }
                if (filterHouseAd) {
                    if (hasClause) {
                        sb.append(" AND ");
                    }
                    sb.append("CAST(Advertiser.CurrentMember.Parent.Properties(\"houseAd\") AS STRING) = '").append(houseAds.toString()).append("'");
                }

                sb.append(")");
                mdx = sb.toString();
            }

        }
        
        
        
        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Campaign");
            report.addColumn("Creative");
            report.addColumn("Format");
        }

        // Can  return either a single Object or an Object[] for multiple columns
        @Override
        public Object extractValue(Member member) {
            return new Object[] {
                    member.getParentMember().getCaption(), // Campaign
                    creativeResolver.getEntityById(Long.valueOf(member.getName())), // Creative Object
                    formatResolver.getEntityById(Long.valueOf(member.getPropertyValue("format").toString())).getName() // Format
            };
        }
    }
    
    public static class CreativesZero extends Parameter  {

        public CreativesZero() {
            dimension = "Advertiser";
            mdx = "Creatives";
        }
        
        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Campaign");
            report.addColumn("Creative");
            report.addColumn("Format");
        }

        // Can  return either a single Object or an Object[] for multiple columns
        @Override
        public Object extractValue(Member member) {
            return (Object) member.getName(); // Campaign
        }
    }    
    

    public static class AdSpaces extends Parameter  {
        public AdSpaces(Company company, Publisher publisher, Publication publication, AdSpace adSpace) {
            dimension = "Publisher";
            if (adSpace != null) {
                mdx = "{Publisher.AdSpace.[" + adSpace.getId() + "]}";
            } else if (publication != null) {
                mdx = "Distinct(Descendants(Publisher.Publication.[" + publication.getId() + "],1))";
            } else if (publisher != null) {
                mdx = "Distinct(Descendants(Publisher.Publisher.[" + publisher.getId() + "],2))";
            } else {
                mdx = "Distinct(Descendants(Publisher.Company.[" + company.getId() + "],3))";
            }
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Publication");
            report.addColumn("AdSpace");
        }

        // Can  return either a single Object or an Object[] for multiple columns
        @Override
        public Object extractValue(Member member) {
            return new Object[] {
                    member.getParentMember().getCaption(), // Publication
                    member.getCaption() // AdSpace
            };
        }
    }

    public abstract static class StringParameter extends Parameter {
        protected String header;
        public StringParameter(String header) {
            this.header = header;
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn(header);
        }
    }

    public static class AdvertiserByAdvertiser extends StringParameter {
        public AdvertiserByAdvertiser(Advertiser advertiser) {
            this(advertiser, null);
        }

        public AdvertiserByAdvertiser(Advertiser advertiser, Boolean houseAds) {
            super("Company");
            mdx = "Advertiser.Advertiser.[" + advertiser.getId() + "]";
            dimension = "Advertiser";

            if (houseAds != null) {
                mdx = "Filter(" + mdx + ".Children, CAST(Advertiser.CurrentMember.Properties(\"houseAd\") AS STRING) = '" + houseAds.toString() + "')";
            }
        }
    }

    public static class AdvertiserByCampaign extends StringParameter {
        public AdvertiserByCampaign(Campaign campaign) {
            super("Campaign");
            mdx = "Advertiser.Campaign.[" + campaign.getId() + "]";
            dimension = "Advertiser";
        }
    }

    public static class AdvertiserByCreative extends StringParameter {
        public AdvertiserByCreative(Creative creative) {
            super("Creative");
            mdx = "Advertiser.Creative.[" + creative.getId() + "]";
            dimension = "Advertiser";
        }
    }

    public static class PublisherByCompany extends StringParameter {
        public PublisherByCompany(Company company) {
            super("Company");
            mdx = "Publisher.Company.[" + company.getId() + "]";
            dimension = "Publisher";
        }
    }
    public static class PublisherByPublisher extends StringParameter {
        public PublisherByPublisher(Publisher publisher) {
            super("Publisher");
            mdx = "Publisher.Publisher.[" + publisher.getId() + "]";
            dimension = "Publisher";
        }
    }
    public static class PublisherByPublication extends StringParameter {
        public PublisherByPublication(Publication publication) {
            super("Publication");
            mdx = "Publisher.Publication.[" + publication.getId() + "]";
            dimension = "Publisher";
        }
    }
    public static class PublisherByAdSpace extends StringParameter {
        public PublisherByAdSpace(AdSpace adSpace) {
            super("AdSpace");
            mdx = "Publisher.AdSpace.[" + adSpace.getId() + "]";
            dimension = "Publisher";
        }
    }

    public static class NetworkByTransparentNetwork extends StringParameter {
        public NetworkByTransparentNetwork(TransparentNetwork transparentNetwork) {
            super("Network");

            if (transparentNetwork.getName().equals(TransparentNetwork.PERFORMANCE_NETWORK_NAME)) {
                // The <> 0 is sort of a hack.  The filter MDX forces
                // Mondrian to look at the property value, which therefore
                // excludes anything with a NULL for transparentNetwork, as
                // MDX doesn't do nulls.

                mdx = "Except(Publisher.Publication.Members, Filter(Publisher.Publication.Members, Publisher.Publication.CurrentMember.Properties(\"transparentNetwork\") <> 0))";
                dimension = "Publisher";
            } else {
                mdx = "Network.TransparentNetwork.[" + transparentNetwork.getId() + "]";
                dimension = "Network";
            }
        }
    }

    public static class GroupByNetwork extends Parameter {
        public GroupByNetwork() {
            mdx = "{Network.TransparentNetwork.Members, Network.[All Networks].[0]}";
            dimension = "Network";
        }

        public String getWithMember() {
            return "Network.[All Networks].[0] as 'Aggregate(Except(Publisher.Publication.Members, Filter(Publisher.Publication.Members, Publisher.Publication.CurrentMember.Properties(\"transparentNetwork\") <> 0)))'";
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Publisher");
        }

        @Override
        public Object extractValue(Member member) {
            Long value = Long.valueOf(member.getName());
            if (value.longValue() == 0L) {
                return "Performance Network"; // FIXME
            }
            return super.extractValue(member);
        }

    }

    public static class GroupByCategory extends Parameter {
    	public GroupByCategory() {
    		//not used - values aren't correct
    		mdx = "Category.children";
    		dimension = "Category";
    	}
    	
    	public void addColumn(Report report, Locale locale) {
    		report.addColumn("Category");
    	}
    }
    
    public static class GroupByChannel extends Parameter {
        public GroupByChannel() {
            mdx = "Channel.children";
            dimension = "Channel";
        }

        public void addColumn(Report report, Locale locale) {
            report.addColumn("Channel");
        }
    }
    
    public static class GroupByInventory extends StringParameter {
		public GroupByInventory() {
			super("Inventory");
		}
		
		public void addColumn(Report report, Locale locale) {
			report.addColumn("Inventory");
		}
    }

    public static class GroupByPlatform extends Parameter {
        public GroupByPlatform() {
            mdx = "Platform.children";
            dimension = "Platform";
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Platform");
        }
    }
    
    public static class Geotarget extends Parameter {
        public Geotarget() {
        }
        
        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Geotarget");
        }
    }

    public static class ByAdAction extends Parameter {
        public ByAdAction(AdAction adAction) {
            mdx = "AdAction.[" + adAction.toString() + "]";
            dimension = "AdAction";
        }
    }

    public static class ByDimension<T> extends Parameter {
        private final EntityResolver<T> entityResolver;
        protected Class resultClass;

        public ByDimension(Dimension dimensionObj, Object qualifier, Class<T> resultClass, EntityResolver<T> entityResolver) {
            this.entityResolver = entityResolver;
            this.dimension = dimensionObj.name();
            this.resultClass = resultClass;

            int d1 = dimensionObj.getDepth(qualifier);
            int d2 = dimensionObj.getDepth(resultClass);
            int diff = d2 - d1;

            String qualifierMDX = qualifierToMDX(dimensionObj, qualifier);

            if (diff == 0) {
                mdx = "{" + qualifierMDX + "}";
            } else {
                mdx  = "Distinct(Descendants(" + qualifierMDX + "," + diff + "))";
            }
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn(resultClass.getSimpleName());
        }

        @Override
        public Object extractValue(Member member) {
            return entityResolver.getEntityById(Long.valueOf(member.getName()));
        }
    }

    /**
    * Returns a MDX-syntax identifier for one of our domain objects.
    * For example, Publisher.Company.[23] or Advertiser.Creative.[77]
    */
    private static String qualifierToMDX(Dimension dimension, Object qualifier) {
        return (qualifier == null) ?
                dimension.name() + ".[All]" :
                    dimension.name() + "." + qualifier.getClass().getSimpleName()
                    + ".[" + ((HasPrimaryKeyId)qualifier).getId() + "]";
    }

    public static class GroupByPublication extends Parameter {
        private final EntityResolver<Publication> publicationResolver;

        public GroupByPublication(EntityResolver<Publication> publicationResolver) {
            this.publicationResolver = publicationResolver;
            mdx = "Publisher.Publication.Members";
            dimension = "Publisher";
        }

        @Override
        public void addColumn(Report report, Locale locale) {
            report.addColumn("Publication");
            report.addColumn("Company");
        }

        @Override
        public Object extractValue(Member member) {
            return new Object[] {
                    publicationResolver.getEntityById(Long.valueOf(member.getName())), // Publication
                    member.getParentMember().getCaption() // Company
            };
        }
    }

    public static class GroupByCampaign extends Parameter {
        private final EntityResolver<Campaign> campaignResolver;

        public GroupByCampaign(EntityResolver<Campaign> campaignResolver, Boolean isKeyOnly) {
            this.campaignResolver = campaignResolver;
            mdx = "Advertiser.Campaign.Members";
            if (isKeyOnly != null && isKeyOnly.equals(Boolean.TRUE)) {
                mdx = "Filter(" + mdx + ", CAST(Advertiser.CurrentMember.Parent.Properties(\"isKey\") AS STRING) = 'true')";
            }

            dimension = "Advertiser";
        }

        public void addColumn(Report report, Locale locale) {
            report.addColumn("Campaign");
        }

        public Object extractValue(Member member) {
            return new Object[] {
                    campaignResolver.getEntityById(Long.valueOf(member.getName())), // Campaign
            };
        }
    }

    /**
     * Functions take m regular parameters, and yield n FunctionParameters.  A function parameter calls back to the Function for all calls.  This allows you to have (for example) 3 parameters in the select statement strings, but only 2 in the query object as objects (such as for the MDX extract function).  It also allows you to wrap 2 or more regular parameters in a function other than NonEmptyCrossJoin.
     * Usage: Normall, you will add parameters to a function, and get function parameters to add to the OLAPQuery. See the test com.adfonic.dao.TestExtractFunction for an example.
     */
    public static class FunctionParameter extends Parameter {
        private Function function;
        private Parameter parameter;

        public FunctionParameter(Function function, Parameter parameter) {
            this.function = function;
            this.parameter = parameter;
        }

        public String getMDX() { return function.getMDX(this, parameter); }
        public String getDimension() { return function.getDimension(this, parameter); }

        public void addColumn(Report report, Locale locale) {
            function.addColumn(this, parameter, report, locale);
        }

        public Object extractValue(Member member) {
            return function.extractValue(this, parameter, member);
        }

        public String getWithMember() { return function.getWithMember(this,parameter); }

    }

    /**
     * Wrap two or more parameters in an MDX function other than NonEmptyCorssJon. If you want to embed a function inside a single parameter's MDX, you don't need this (see Parameter.Creatives for an example of internal functions).
     * This class defines the contract that FunctionParameter depends on for callbacks.  Functions will take "regular" parameters, and yield FunctionParameters.  See Parameter.ExtractFunction for an example.
     */

    public abstract static class Function {
        public abstract String getMDX(FunctionParameter functionParameter, Parameter wrappedParameter);
        public abstract String getDimension(FunctionParameter functionParameter, Parameter wrappedParameter);

        public abstract void addColumn(FunctionParameter functionParameter, Parameter wrappedParameter, Report report, Locale locale);

        public abstract Object extractValue(FunctionParameter functionParameter, Parameter wrappedParameter, Member member);
        public abstract String getWithMember(FunctionParameter functionParameter, Parameter wrappedParameter);
        public abstract FunctionParameter[] getFunctionParameters();

    }

    /**
     * Takes mulitple parameters, and wraps them in an MDX extract call.  Only parameters added with extract=true, or a non null hierarchy will be in the final result.  Example:
     * extractFunction.addParameter(param1,true).addParameter(param2,false).addParameter(param3,true);
     * olapQuery.addParameters(extractFunction.getFunctionParameters());
     * will yield MDX something like:
     * Extract(NonEmptyCrossJoin(NonEmptyCrossJoin(param1,param2),param3), param1Dimension, param2Dimension)
     *
     * Order is important.  The most inner NECJ will have param1, and param2.  The result will be NECJ'ed with param3, etc.  Extracts will operate in the same order.
     * How does this work?
         * The fucntion parameters returned by the function will only "represent" parameters with extract values of "true" (or non empty hierarchys).  The "false" values will not be function parameters.  It is a logical error to have no "true" values (not checked).  The first functional param returned will render ALL the MDX, and the others will render NO mdx (OLAPQuery supports this now).  This allows addColumn and extractValue to still work correctly.
         */
        public static class ExtractFunction extends Function {
            //Parameter list.  Keys are parameters, values are strings, representing the hierarchy to use to extract the specified paramter (null means don't extract).
            //Use a linked hashmap to maintain insertion order
            private LinkedHashMap<Parameter,String> parameterMap;
            private Parameter firstParam = null;
            public ExtractFunction() {
                parameterMap = new LinkedHashMap<Parameter,String>();
            }

            /**
             * Add a parameter, and extract it if hierarchy is not null
             */
            public ExtractFunction addParameter(Parameter p, String hierarchy) {
                return addParameter(p, hierarchy, (hierarchy == null));
            }

            /**
             * Add a parameter, and extract it if extract is true, by using Parameter.getDimension as the hierarchy.
             */
            public ExtractFunction addParameter(Parameter p, boolean extract) {
                return addParameter(p, null, extract);
            }

            protected ExtractFunction addParameter(Parameter p, String hierarchy, boolean extract) {
                if(p == null) {
                    throw new IllegalArgumentException("ExtractFunction does not support null Parameter objects");
                }
                String realHierarchy = null;
                if(extract) {
                    if(hierarchy == null) {
                        realHierarchy = p.getDimension();
                    } else {
                        realHierarchy = hierarchy;
                    }
                }
                parameterMap.put(p, realHierarchy);
                if(firstParam == null && realHierarchy != null) {
                    firstParam = p;
                }
                return this;
            }

            public FunctionParameter[] getFunctionParameters() {
                List<FunctionParameter> parameters = new ArrayList<FunctionParameter>();
                for(Map.Entry<Parameter,String> me : parameterMap.entrySet()) {
                    if(me.getValue() != null) { //non null hierarchy
                        FunctionParameter fp = new FunctionParameter(this, me.getKey());
                        parameters.add(fp);
                    }
                }
                return parameters.toArray(new FunctionParameter[parameters.size()]);
            }

            public String getMDX(FunctionParameter fp, Parameter wrapped) {
                //render it all on the first call
                if(wrapped != null && wrapped.equals(firstParam)) {
                    StringBuilder mdxClause = null;
                    StringBuilder extractClause = null;
                    for(Map.Entry<Parameter,String> me : parameterMap.entrySet()) {
                        Parameter p = me.getKey();
                        String hierarchy = me.getValue();
                        if(mdxClause == null) {
                            mdxClause = new StringBuilder(p.getMDX());
                        } else {
                            mdxClause.insert(0,"NonEmptyCrossJoin(")
                                //then the existing value
                                .append(",")
                                .append(p.getMDX())
                                .append(")");
                        }
                        if(hierarchy != null) {
                            if(extractClause == null) {
                                extractClause = new StringBuilder(hierarchy);
                            } else {
                                extractClause.append(",").append(hierarchy);
                            }
                        }
                    }
                    //short circuit if mdx clause is somehow empty
                    if(mdxClause == null) return null;
                    return new StringBuilder("Extract(")
                        .append(mdxClause)
                        .append(",")
                        .append(extractClause)
                        .append(")")
                        .toString();
                } else {
                    return null;
                }
            }

            public String getDimension(FunctionParameter fp, Parameter wrapped) {
                return wrapped.getDimension();
            }

            public String getWithMember(FunctionParameter fp, Parameter wrapped) {
                if(wrapped != null && wrapped.equals(firstParam)) {
                    StringBuilder withClause = null;
                    //first "member" is added at the report level
                    for(Parameter p : parameterMap.keySet()) {
                        String withMember = p.getWithMember();
                        if(withMember != null) {
                            if(withClause == null) {
                                withClause = new StringBuilder(withMember);
                            } else {
                                withClause.append(' ')
                                    .append("member ")
                                    .append(withMember);
                            }
                        }
                    }
                    return (withClause == null ? null : withClause.toString());
                } else {
                    return null;
                }
            }

            public void addColumn(FunctionParameter functionParameter, Parameter wrappedParameter, Report report, Locale locale) {
                //don't add columns for non-extracted parameters
                String hierarchy = parameterMap.get(wrappedParameter);
                if(hierarchy != null) {
                    wrappedParameter.addColumn(report, locale);
                }
            }

            public Object extractValue(FunctionParameter functionParameter, Parameter wrappedParameter, Member member) {
            return wrappedParameter.extractValue(member);
        }
    }
}
