package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.adfonic.util.TimeZoneUtils;

public class TaxUtils {
    private static final BigDecimal VAT_15_PERCENT = new BigDecimal("0.15");
    private static final BigDecimal VAT_17_5_PERCENT = new BigDecimal("0.175");
    private static final BigDecimal VAT_20_PERCENT = new BigDecimal("0.2");
    private static final Date JAN_1_2010_GMT;
    private static final Date JAN_4_2011_GMT;

    static {
        Calendar calendar = Calendar.getInstance(TimeZoneUtils.getTimeZoneNonBlocking("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 2010);
        JAN_1_2010_GMT = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 4);
        calendar.set(Calendar.YEAR, 2011);
        JAN_4_2011_GMT = calendar.getTime();
    }

    @SuppressWarnings("serial")
	private static final Map<String,Pattern> VAT_MAP = new HashMap<String,Pattern>() {{
            // U followed by 8 digits
            put("AT", Pattern.compile("^U\\d{8}$"));
            // 9 or 10 digits
            put("BE", Pattern.compile("^\\d{9,10}$"));
            put("BG", Pattern.compile("^\\d{9,10}$"));
            // 8 digits and one alpha
            put("CY", Pattern.compile("^\\d{8}[A-Z]$"));
            // 8 to 10 digits
            put("CZ", Pattern.compile("^\\d{8,10}$"));

            put("DE", Pattern.compile("^\\d{9}$"));
            put("DK", Pattern.compile("^\\d{8}$"));
            put("EE", Pattern.compile("^\\d{9}$"));

            // 9 total, first OR last OR first and last alpha
            put("ES", Pattern.compile("^[A-Z]\\d{8}$|^\\d{8}[A-Z]$|^[A-Z]\\d{7}[A-Z]$", Pattern.CASE_INSENSITIVE));

            put("FI", Pattern.compile("^\\d{8}$"));

            // 11 total, first and second may be alpha or digit but never O or I
            put("FR", Pattern.compile("^[A-HJ-NP-Z0-9]{2}\\d{9}$"));

            put("GB", Pattern.compile("^\\d{9}$"));

            put("GR", Pattern.compile("^\\d{9}$"));
            put("HU", Pattern.compile("^\\d{8}$"));

            // 8 total, last OR second and last alpha
            put("IE",
                Pattern.compile("^\\d{7}[A-Z]$|^\\d[A-Z]\\d{5}[A-Z]$"));

            put("IT", Pattern.compile("^\\d{11}$"));

            // 9 or 12 digits
            put("LT", Pattern.compile("^\\d{9}|\\d{12}$"));

            put("LV", Pattern.compile("^\\d{11}$"));
            put("LU", Pattern.compile("^\\d{8}$"));
            put("MT", Pattern.compile("^\\d{8}$"));

            // 12 total, position 10 always B
            put("NL", Pattern.compile("^\\d{9}B\\d{2}$"));

            put("PL", Pattern.compile("^\\d{10}$"));
            put("PT", Pattern.compile("^\\d{9}$"));

            // 2 to 10 digits
            put("RO", Pattern.compile("^\\d{2,10}$"));

            put("SE", Pattern.compile("^\\d{12}$"));
            put("SI", Pattern.compile("^\\d{8}$"));
            put("SK", Pattern.compile("^\\d{10}$"));
        }};

    /** Returns the VAT rate for the current time. */
    public static BigDecimal getTaxRate() {
        return getTaxRate(new Date());
    }

    /** Returns the VAT rate as of the given time. */
    public static BigDecimal getTaxRate(Date date) {
        if (date.before(JAN_1_2010_GMT)) {
            return VAT_15_PERCENT;
        }
        if (date.before(JAN_4_2011_GMT)) {
            return VAT_17_5_PERCENT;
        }
        return VAT_20_PERCENT;
    }

    /**
     * Is an advertiser taxable in a given country with a given tax code
     */
    public static boolean isAdvertiserTaxable(Country country, String taxCode) {
        Country.TaxRegime tr = country.getTaxRegime();
        if (tr == Country.TaxRegime.UK) {
            return true;
        } else if (tr == Country.TaxRegime.EU) {
            return org.apache.commons.lang.StringUtils.isEmpty(taxCode);
        } else {
            return false;
        }
    }

    /**
     * Is a publisher taxable in a given country with a given tax code
     */
    public static boolean isPublisherTaxable(Country country, String taxCode) {
        // Publishers are taxable only if they're in the UK (tax regime)
        // and have specified a taxCode.
        return Country.TaxRegime.UK.equals(country.getTaxRegime()) && org.apache.commons.lang.StringUtils.isNotEmpty(taxCode);
    }
    
    /**
     * @param isoCode
     * @param value
     * @return boolean indicating if the value is a valid VAT number
     * format for the country iso code
     */
    public static boolean isValidVatNumber(String isoCode, String value) {
        if (isoCode == null || value == null) return false;
        Pattern pattern = VAT_MAP.get(isoCode);

        if (pattern != null &&
            pattern.matcher(value).matches()) {
            return true;
        }

        return false;
    }

    /**
     * Calculate advertiser VAT for a given cost
     * @param cost the amount that the advertiser is being charged
     * @param eventTime the time at which the event (impression, click) occurred
     * @param taxableAdvertiser whether or not the advertiser is taxable
     * @return the VAT amount to apply to the advertiser
     */
    public static BigDecimal calculateAdvertiserVat(BigDecimal cost, Date eventTime, boolean taxableAdvertiser) {
        return taxableAdvertiser ? cost.multiply(getTaxRate(eventTime)) : null;
    }

    /**
     * Calculate publisher VAT for a given payout
     * @param payout the payout amount
     * @param eventTime the time at which the event (impression, click) occurred
     * @param taxablePublisher whether or not the publisher is taxable
     * @return the VAT amount to apply to the publisher
     */
    public static BigDecimal calculatePublisherVat(BigDecimal payout, Date eventTime, boolean taxablePublisher) {
        return taxablePublisher ? payout.multiply(getTaxRate(eventTime)) : null;
    }
}
