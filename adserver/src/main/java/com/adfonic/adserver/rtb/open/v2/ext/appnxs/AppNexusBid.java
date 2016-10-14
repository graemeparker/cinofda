package com.adfonic.adserver.rtb.open.v2.ext.appnxs;

import java.util.ArrayList;
import java.util.List;

public class AppNexusBid extends com.adfonic.adserver.rtb.open.v2.Bid {

    private AppNexusBidExt ext;

    public AppNexusBidExt getExt() {
        return ext != null ? ext : (ext = new AppNexusBidExt());
    }

    public void setExt(AppNexusBidExt ext) {
        this.ext = ext;
    }

    public static class AppNexusBidExt {

        private AppNexusExtWrap appnexus;

        public AppNexusExtWrap getAppnexus() {
            return appnexus != null ? appnexus : (appnexus = new AppNexusExtWrap());
        }

        public void setAppnexus(AppNexusExtWrap appnexus) {
            this.appnexus = appnexus;
        }

    }

    public static class AppNexusExtWrap {

        private String crcode;

        private List<CustomMacro> custom_macros;

        public String getCrcode() {
            return crcode;
        }

        public void setCrcode(String crcode) {
            this.crcode = crcode;
        }

        public List<CustomMacro> getCustom_macros() {
            return custom_macros == null ? (custom_macros = new ArrayList<CustomMacro>()) : custom_macros;
        }

        public void setCustom_macros(List<CustomMacro> custom_macros) {
            this.custom_macros = custom_macros;
        }

    }

    public static class CustomMacro {
        private final String name;
        private final String value;

        public CustomMacro(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }
}
