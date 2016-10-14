package com.adfonic.webservices.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.adfonic.domain.Company;
import com.adfonic.domain.Role;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.ServiceException;

public class DSPetc {
    
    public static final String PUB_VIEW_SFX = "PublicationView";
    public static final String PUB_LIST_VIEW_SFX = "PublicationListView";
    public static final String PROF_SPECIFC_EXTR_SFX = "ProfileOrTemplSpecificExtractor";
    public static final String VOLTARI_CPNY_NAME = "voltari";
    public static final String WEVE_CPNY_NAME = "Weve";
    public static final String WEVE_DISCR = "Weve";
    public static final String SMV_CPNY_NAME = "Starcom MediaVest Group";
    public static final String VIEW_SFX_VOLTARI_TEMPL = "TemplVolt";

    // Enum name should match Company name. Add when support is added
    public enum DSP_PROFILE {
        _(null, null, ""), 
        VOLTARI(VOLTARI_CPNY_NAME, null, VIEW_SFX_VOLTARI_TEMPL, true, "json"), 
        WEVE(WEVE_CPNY_NAME, null, VIEW_SFX_VOLTARI_TEMPL, false, "xml"),
        SMV(SMV_CPNY_NAME, null, VIEW_SFX_VOLTARI_TEMPL, false, "xml");
        
        private String companyName;
        private String companyRole;
        private String viewPrefix;
        private Set<String> formatWhitelist;
        public boolean isRtbOnly;


        DSP_PROFILE(String companyName, String companyRole, String viewPrefix) {
            this.companyName = companyName;
            this.companyRole = companyRole;
            this.viewPrefix = viewPrefix;
        }

        DSP_PROFILE(String companyName, String companyRole, String viewPrefix, boolean isRtbOnly, String... formatsSupported) {
            this(companyName, companyRole, viewPrefix);
            this.isRtbOnly = isRtbOnly;
            this.formatWhitelist = new HashSet<>(Arrays.asList(formatsSupported));
        }

        public static DSP_PROFILE getByNameOrRole(Company company) {
            DSP_PROFILE found = _;

            for (final DSP_PROFILE profile : DSP_PROFILE.values()) {
                if (company.getName().equalsIgnoreCase(profile.companyName)) {// proper match; return
                    return profile;
                }

                if (CollectionUtils.find(company.getRoles(), new Predicate() {
                    @Override
                    public boolean evaluate(Object role) {
                        return ((Role) role).getName().equals(profile.companyRole);
                    }
                }) != null) {// if
                    found = profile;
                }
            }

            return found;
        }

        private String allowedFormat(String format) {
            if (formatWhitelist == null || formatWhitelist.contains(format)) {
                return format;
            }

            throw new ServiceException(ErrorCode.NOT_SUPPORTED, "Requested format not supported for data on your profile");
        }

        public String viewPrefix(String format) {
            return allowedFormat(format) + viewPrefix;
        }

    }
    
    private static final ThreadLocal<DspAccess> effectiveDspAccess = new ThreadLocal<DspAccess>();


    public static DspAccess getDspAccessOnThread() {
        return effectiveDspAccess.get();
    }


    public static void setDspAccessOnThread(DspAccess access) {
        effectiveDspAccess.set(access);
    }

}
