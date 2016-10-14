package com.adfonic.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

public class TestConstraintsHelper {
    private static final transient Logger LOG = Logger.getLogger(TestConstraintsHelper.class.getName());

    private static Map<String, String> props;
    private static Map<String, Boolean> tests;
    static {
        props = new LinkedHashMap<String, String>();
        props.put("osRim", "true");
        props.put("osWindows", "false");
        props.put("midp", "2.1");
        props.put("developerPlatform", "Android");
        props.put("osProprietary", "webOS");

        tests = new LinkedHashMap<String, Boolean>();
        tests.put("midp", true);
        tests.put("!midp", false);
        tests.put("midp;osRim", true);
        tests.put("midp;osRim", true);
        tests.put("midp;osRim = true", true);
        tests.put("midp;osRim==true", true);
        tests.put("midp > 1.0; osWindows != true", true);
        tests.put("osRim; osWindows != true", true);
        tests.put("osRim; !osWindows", true);
        tests.put("osRim; osWindows", false);
        tests.put("osRim; osWindows = true", false);
        tests.put("developerPlatform=Android", true);
        tests.put("developerPlatform == Android", true);
        tests.put("developerPlatform='Android'", true);
        tests.put("developerPlatform=\"Android\"", true);
        tests.put("developerPlatform == \"Android\"", true);
        tests.put("developerPlatform=\"Not Android\"", false);
        tests.put("missing=blah; nobody=cares", false);
        tests.put("osRim;midp;missing", false);
        tests.put("!missing", true);
        tests.put("!osOsx;!osAndroid;!osWindows;!osRim;!osSymbian;!midp;osProprietary!='webOS'", false);
        tests.put("!osOsx;!osWindows;osProprietary!='blah'", true);
        tests.put("123", true);
        tests.put("!123", false);
        tests.put("0", false);
        tests.put("!0", true);
        // Note: literal standalone 0.0 is known to fail since it !equals("0")
        tests.put("9.999", true);
        tests.put("!9.999", false);
    }

    @Test
    public void test() throws Exception {
        LOG.info("Properties: " + props);
        ConstraintsHelper.PropertySource source = new ConstraintsHelper.MapPropertySource(props);
        int failureCount = 0;
        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            boolean result = ConstraintsHelper.eval(entry.getKey(), source);
            if (result != entry.getValue().booleanValue()) {
                ++failureCount;
                throw new Exception("FAILURE!  Constraints: " + entry.getKey() + ", expected " + entry.getValue() + ", got " + result);
            }
        }
        if (failureCount > 0) {
            throw new RuntimeException("Test failure count: " + failureCount);
        }
        LOG.info("All tests passed");
    }

    @Test
    public void test2() throws Exception {
        Map<String, String> props = new LinkedHashMap<String, String>();
        props.put("_matched", "Mozilla/5.0 (do");
        props.put("_unmatched",
                "pod-T5399/1.28.706.6;U;Windows Mobile/6.5;Profile/MIDP-2.0 Configuration/CLDC-1.1;480*640;CTC/2.0) MSIE/6.0 (compatible; MSIE 6.0; Windows CE; IEMobile 8.12; MSIEMobile 6.0)");
        props.put("id", "1981590");
        props.put("isEReader", "0");
        props.put("isGamesConsole", "0");
        props.put("isMobilePhone", "1");
        props.put("isTablet", "0");
        props.put("jqm", "0");
        props.put("mobileDevice", "1");
        props.put("model", "Twin 10000");
        props.put("osSymbian", "0");
        props.put("vendor", "HTC");

        String constraints = "osOsx";
        ConstraintsHelper.PropertySource source = new ConstraintsHelper.MapPropertySource(props);
        if (ConstraintsHelper.eval(constraints, source)) {
            throw new Exception("Failed: " + constraints + " against " + props);
        }

        props.put("osOsx", "1");
        if (!ConstraintsHelper.eval(constraints, source)) {
            throw new Exception("Failed: " + constraints + " against " + props);
        }

        LOG.info("Okeydoke");
    }
}
