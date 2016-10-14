package com.adfonic.adserver.rtb.rubicon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.adfonic.adserver.rtb.nativ.APIFramework;

public class RubiconBanner extends com.adfonic.adserver.rtb.open.v2.Banner {

    // need only this for now, given the supported stuff - to avoid object creation
    private static final Set<APIFramework> APIS_MRAID_ONLY = Collections.singleton(APIFramework.MRAID);

    // capturing for later use, once you start using this remove the above map
    enum RubiconVendor {
        POINTROLL, GOLDSPOT_MEDIA, MEDIALETS, MILLENIAL, CELTRA
    };

    enum RubiconContentForm {
        MRAID, MRAID_V1, MRAID_V2
    };

    enum RubiconApiFramework {
        INSTANCE;
        @SuppressWarnings("serial")
		private Map<Integer, RubiconContentForm> cfMap = Collections.unmodifiableMap(new HashMap<Integer, RubiconContentForm>() {
            {
                cfMap.put(3, RubiconContentForm.MRAID);
                cfMap.put(1000, RubiconContentForm.MRAID_V1);
                cfMap.put(1001, RubiconContentForm.MRAID_V2);
            }
        });

        @SuppressWarnings("serial")
		private Map<Integer, RubiconVendor> vendorMap = Collections.unmodifiableMap(new HashMap<Integer, RubiconVendor>() {
            {
                vendorMap.put(1002, RubiconVendor.POINTROLL);
                vendorMap.put(1003, RubiconVendor.GOLDSPOT_MEDIA);
                vendorMap.put(1004, RubiconVendor.MEDIALETS);
                vendorMap.put(1005, RubiconVendor.MILLENIAL);
                vendorMap.put(1006, RubiconVendor.CELTRA);
            }
        });


        RubiconVendor getNativeVendor(int frameworkId) {
            return vendorMap.get(frameworkId);
        }


        RubiconContentForm getNativeContentForm(int frameworkId) {
            return cfMap.get(frameworkId);
        }
    }

}
