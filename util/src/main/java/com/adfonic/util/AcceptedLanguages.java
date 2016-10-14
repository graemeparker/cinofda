package com.adfonic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AcceptedLanguages {

    private static final Logger LOGGER = Logger.getLogger(AcceptedLanguages.class.getName());

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    // Pick apart the Accept-Language header format, which we expect to appear
    // as a comma-separated list of languages along with implied or specified
    // quality values, such as:
    //
    // da, en-gb;q=0.8, en;q=0.7
    private static final Pattern LANG_QUALITY_PAIR_PATTERN = Pattern.compile("([^;,]+)(;\\s*q=([^,]+))?");
    
    private static final int QUALITY_GROUP_POSITION = 3;
    
    private boolean any;
    // Use TreeSet here so that it sorts for us
    private final SortedSet<AcceptedLanguage> langs = new TreeSet<AcceptedLanguage>();
    // This is a lazily-populated cache of quality by ISO code
    private final Map<String, Double> qualityCache = new HashMap<String, Double>();

    /**
     * Parse the content of an "Accept-Language" header, or equivalent, and
     * return an object that can indicate which languages are accepted, along
     * with the relative quality.
     */
    public static AcceptedLanguages parse(String langSpec) {
        AcceptedLanguages acceptedLanguages = new AcceptedLanguages();
        if (StringUtils.isEmpty(langSpec)) {
            return acceptedLanguages; // No languages accepted
        }

        // This value will get reverted back to false if/when we encounter
        // the first language spec that isn't "*". But in case we don't
        // encounter a specific language, assume any=true for now.
        acceptedLanguages.setAny(true);

        // Snag each language/quality pair in the accepted languages
        Matcher matcher = LANG_QUALITY_PAIR_PATTERN.matcher(langSpec);
        while (matcher.find()) {
            String langCode = matcher.group(1).trim();
            double quality = 1.0; // the default if not specified
            if (matcher.group(QUALITY_GROUP_POSITION) != null) {
                try {
                    quality = Double.parseDouble(matcher.group(QUALITY_GROUP_POSITION).trim());
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Unexpected quality value (" + matcher.group(QUALITY_GROUP_POSITION) + ") for langSpec: " + langSpec);
                    }
                    continue;
                }
            }
            AcceptedLanguage acceptedLanguage = new AcceptedLanguage(langCode, quality, acceptedLanguages.getLanguages().size());
            acceptedLanguages.getLanguages().add(acceptedLanguage);
            if (acceptedLanguages.isAny() && !acceptedLanguage.isAny()) {
                acceptedLanguages.setAny(false);
            }
        }

        return acceptedLanguages;
    }

    public boolean isAny() {
        return any;
    }

    public void setAny(boolean any) {
        this.any = any;
    }

    public boolean isNone() {
        return !any && langs.isEmpty();
    }

    public SortedSet<AcceptedLanguage> getLanguages() {
        return langs;
    }

    /**
     * Return just the sorted list of lang codes for the accepted languages.
     * This method is simpler than getLanguages in case the caller doesn't care
     * about quality values and doesn't want to have to deal with the static
     * inner class
     */
    public List<String> getLangCodes() {
        List<String> langCodes = new ArrayList<String>(langs.size()); // initialCapacity
                                                                      // for
                                                                      // ++performance
        for (AcceptedLanguage lang : langs) {
            langCodes.add(lang.getLangCode());
        }
        return langCodes;
    }

    /**
     * Return just the sorted list of ISO codes for the accepted languages. This
     * method is simpler than getLanguages in case the caller doesn't care about
     * quality values and doesn't want to have to deal with the static inner
     * class. This method strips off the "-country" suffix, i.e. "en-us" becomes
     * simply "en". If the list of languages is something like: de, en-gb,
     * en-us, en, then the resulting list of ISO codes will only contain two
     * elements, de and en.
     */
    public List<String> getIsoCodes() {
        List<String> isoCodes = new ArrayList<String>(langs.size()); // conservative
                                                                     // but
                                                                     // still
                                                                     // helpful
        for (AcceptedLanguage lang : langs) {
            if (!isoCodes.contains(lang.getIsoCode())) {
                isoCodes.add(lang.getIsoCode());
            }
        }
        return isoCodes;
    }

    /**
     * Return the quality value (between 0.0 and 1.0) for a given language. The
     * value is cached for subsequent inquiries for the same ISO code. This is
     * done because the normal usage pattern for this object is: 1. Parse
     * Accept-Language into the AcceptableLanguages instance. 2. Call
     * getQuality(isoCode) for creative.language numerous times. There are bound
     * to be numerous identical calls made, so caching is definitely going to
     * help performance.
     */
    public double getQuality(String isoCode) {
        Double quality = qualityCache.get(isoCode);
        if (quality == null) {
            Double qualityForAny = null;
            for (AcceptedLanguage lang : langs) {
                if (lang.getIsoCode().equals(isoCode)) {
                    // Direct match
                    quality = lang.getQuality();
                    break;
                } else if (lang.isAny()) {
                    // This isn't a direct match, but we'll fall back on
                    // using it if no other language matches directly.
                    qualityForAny = lang.getQuality();
                }
            }
            if (quality == null) {
                // See if we have an "any" fallback...
                if (qualityForAny != null) {
                    quality = qualityForAny;
                } else {
                    quality = 0.0;
                }
            }
            qualityCache.put(isoCode, quality);
        }
        return quality;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("any", any).append("langs", langs).toString();
    }

    public static final class AcceptedLanguage implements Comparable<AcceptedLanguage> {
        private boolean any;
        private String langCode;
        private volatile String isoCode;
        private double quality;
        private int orderOfAppearance;

        private AcceptedLanguage(String langCode, double quality, int orderOfAppearance) {
            this.any = "*".equals(langCode) || "-".equals(langCode);
            this.langCode = langCode;
            this.quality = quality;
            this.orderOfAppearance = orderOfAppearance;
        }

        public boolean isAny() {
            return any;
        }

        public String getLangCode() {
            return langCode;
        }

        public String getIsoCode() {
            if (isoCode == null) {
                // We've seen both en-us and en_US variants.
                // The latter is non-standard, but it does happen.
                String[] toks = langCode.split("[-_]");
                if (toks.length > 0) {
                    isoCode = toks[0];
                } else {
                    isoCode = "";
                }
            }
            return isoCode;
        }

        public double getQuality() {
            return quality;
        }

        public int getOrderOfAppearance() {
            return orderOfAppearance;
        }

        @Override
        public int compareTo(AcceptedLanguage other) {
            // Sort by quality, high to low
            if (this.quality > other.quality) {
                return -1;
            } else if (this.quality < other.quality) {
                return 1;
            } else if (this.orderOfAppearance < other.orderOfAppearance) {
                // Sort by order of appearance, first to last
                return -1;
            } else if (this.orderOfAppearance > other.orderOfAppearance) {
                return 1;
            } else {
                // Huh?! Whatever, we can code for this wack-ass case
                return this.langCode.compareTo(other.langCode);
            }
        }

        @Override
        public int hashCode(){
            return super.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            } else if (obj.getClass() != this.getClass()) {
                return false;
            } else {
                final AcceptedLanguage other = (AcceptedLanguage) obj;
                return this.any == other.any && StringUtils.equals(this.langCode, other.langCode) && this.quality == other.quality;
            }
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("any", any).append("langCode", langCode).append("isoCode", getIsoCode())
                    .append("quality", quality).append("orderOfAppearance", orderOfAppearance).toString();
        }
    }
}
