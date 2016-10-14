package com.adfonic.tools.beans.optimisation;

import java.util.Set;

import com.adfonic.domain.OptimisationReportFields;

public class OptimisationFields {

    // Equivalent column names based on
    // com.adfonic.domain.OptimisationReportFields enumeration

    /** com.adfonic.domain.OptimisationReportFields.ECPM */
    public static final String COLUMN_KEY_ECPM = "Ecpm";

    /** com.adfonic.domain.OptimisationReportFields.ECPA */
    public static final String COLUMN_KEY_ECPA = "Ecpa";

    /** com.adfonic.domain.OptimisationReportFields.ECPC */
    public static final String COLUMN_KEY_ECPC = "Ecpc";

    /** com.adfonic.domain.OptimisationReportFields.IAB_CATEGORY */
    public static final String COLUMN_KEY_IAB_CATEGORY = "IabCategory";

    /** com.adfonic.domain.OptimisationReportFields.INVENTORY_SOURCE */
    public static final String COLUMN_KEY_INVENTORY_SOURCE = "InventorySource";

    /** com.adfonic.domain.OptimisationReportFields.PID */
    public static final String COLUMN_KEY_PUB_EXT_ID = "PublicationExternalId";
    
    /** com.adfonic.domain.OptimisationReportFields.KEY_PUB_BUNDLE */
    public static final String COLUMN_KEY_PUB_BUNDLE = "PublicationBundle";

    /**
     * com.adfonic.domain.OptimisationReportFields.PUBLICATION_NAME = "Site/App"
     */
    public static final String COLUMN_KEY_PUBLICATION_NAME = "PublicationName";

    /** com.adfonic.domain.OptimisationReportFields.SITE_APP = "Type" */
    public static final String COLUMN_KEY_PUBLICATION_TYPE = "PublicationType";
    


    // Other fields used in the optimization report which must not be hidden
    public static final String COLUMN_KEY_PARTIALY_REMOVED = "PartiallyRemoved";
    public static final String COLUMN_KEY_CREATIVE_NAME = "CreativeName";
    public static final String COLUMN_KEY_BIDS = "Bids";
    public static final String COLUMN_KEY_IMPRESSIONS = "Impressions";
    public static final String COLUMN_KEY_WINRATE = "WinRate";
    public static final String COLUMN_KEY_CLICKS = "Clicks";
    public static final String COLUMN_KEY_CTR = "Ctr";
    public static final String COLUMN_KEY_CONVERSIONS = "Conversions";
    public static final String COLUMN_KEY_CVR = "Cvr";
    public static final String COLUMN_KEY_SPEND = "Spend";

    /**
     * Decide whether the optimisation field should be displayed/exported based
     * on 'Opti Reports Fields' on the account settings admin page.
     */
    public static boolean isFieldVisible(String fieldName, Set<OptimisationReportFields> prefs) {

        // AT-1020 if name and pid are both suppressed id must be displayed
        if (COLUMN_KEY_PUB_EXT_ID.equals(fieldName)) {
            return (prefs != null && !prefs.contains(OptimisationReportFields.PID) && prefs
                    .contains(OptimisationReportFields.PUBLICATION_NAME)) ? false : true;
        }

        if (prefs == null) {
            return false;
        }

        switch (fieldName) {
        case COLUMN_KEY_PUBLICATION_TYPE:
            return (prefs.contains(OptimisationReportFields.SITE_APP)) ? true : false;

        case COLUMN_KEY_PUBLICATION_NAME:
            return (prefs.contains(OptimisationReportFields.PUBLICATION_NAME)) ? true : false;

        case COLUMN_KEY_IAB_CATEGORY:
            return (prefs.contains(OptimisationReportFields.IAB_CATEGORY)) ? true : false;

        case COLUMN_KEY_INVENTORY_SOURCE:
            return (prefs.contains(OptimisationReportFields.INVENTORY_SOURCE)) ? true : false;

        case COLUMN_KEY_ECPM:
            return (prefs.contains(OptimisationReportFields.ECPM)) ? true : false;

        case COLUMN_KEY_ECPC:
            return (prefs.contains(OptimisationReportFields.ECPC)) ? true : false;

        case COLUMN_KEY_ECPA:
            return (prefs.contains(OptimisationReportFields.ECPA)) ? true : false;
            
        case COLUMN_KEY_PUB_BUNDLE:
            return (prefs.contains(OptimisationReportFields.KEY_PUB_BUNDLE)) ? true : false;

        default:
            return true;
        }
    }
}
