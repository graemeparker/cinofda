package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;

import org.junit.Test;

public class TestIntegrationTypeUtils {
    private static final class TestCase {
        private final String full;
        private final String prefix;
        private final Integer expectedValue;

        private TestCase(String full, String prefix, Integer expectedValue) {
            this.full = full;
            this.prefix = prefix;
            this.expectedValue = expectedValue;
        }
    }

    @Test
    public void test() {
        for (TestCase testCase : TEST_CASES) {
            Matcher matcher = IntegrationTypeUtils.INTEGRATION_TYPE_PATTERN.matcher(testCase.full);
            if (matcher.matches()) {
                if (testCase.prefix == null) {
                    fail("For " + testCase.full + ", unexpected match!");
                }
                String prefix = matcher.group(1);
                assertEquals("Prefix for " + testCase.full, testCase.prefix, prefix);
                String version = matcher.group(2);
                Integer value = IntegrationTypeUtils.parseVersionValue(version);
                assertEquals(testCase.full, testCase.expectedValue, value);
            } else if (testCase.prefix != null) {
                fail("For " + testCase.full + ", expected match with prefix=" + testCase.prefix + " but pattern didn't match");
            }
        }
    }

    private static final TestCase[] TEST_CASES = new TestCase[] {
            // These are all legit values we see for r.client=...
            new TestCase("Adfonic/Android/1.0", "Adfonic/Android", 1000000), new TestCase("Adfonic/Android/1.1", "Adfonic/Android", 1001000),
            new TestCase("Adfonic/Android/1.1.1", "Adfonic/Android", 1001001), new TestCase("Adfonic/Android/1.1.2", "Adfonic/Android", 1001002),
            new TestCase("Adfonic/Android/1.1.3", "Adfonic/Android", 1001003), new TestCase("Adfonic/Android/1.1.4", "Adfonic/Android", 1001004),
            new TestCase("Adfonic/iOS/1.0", "Adfonic/iOS", 1000000), new TestCase("Adfonic/iOS/1.0.1", "Adfonic/iOS", 1000001),
            new TestCase("Adfonic/iOS/1.0.2", "Adfonic/iOS", 1000002), new TestCase("Adfonic/iOS/1.0.3", "Adfonic/iOS", 1000003),
            new TestCase("Adiquity/Web/1.0", "Adiquity/Web", 1000000), new TestCase("Madgic/Smart/1.0", "Madgic/Smart", 1000000),
            new TestCase("Madgic/Web/1.0", "Madgic/Web", 1000000), new TestCase("Mobclix/android/1.0.0", "Mobclix/android", 1000000),
            new TestCase("Mobclix/android/1.1.20", "Mobclix/android", 1001020), new TestCase("Mobclix/android/1.5", "Mobclix/android", 1005000),
            new TestCase("Mobclix/android/1.6", "Mobclix/android", 1006000), new TestCase("Mobclix/android/2.0", "Mobclix/android", 2000000),
            new TestCase("Mobclix/android/2.1", "Mobclix/android", 2001000), new TestCase("Mobclix/android/2.1-update1", "Mobclix/android", 2001000),
            new TestCase("Mobclix/android/2.2", "Mobclix/android", 2002000), new TestCase("Mobclix/android/2.2.1", "Mobclix/android", 2002001),
            new TestCase("Mobclix/android/2.2.2", "Mobclix/android", 2002002), new TestCase("Mobclix/android/2.2.3", "Mobclix/android", 2002003),
            new TestCase("Mobclix/android/2.3.1", "Mobclix/android", 2003001), new TestCase("Mobclix/android/2.3.2", "Mobclix/android", 2003002),
            new TestCase("Mobclix/android/2.3.3", "Mobclix/android", 2003003), new TestCase("Mobclix/android/2.3.4", "Mobclix/android", 2003004),
            new TestCase("Mobclix/android/2.3.5", "Mobclix/android", 2003005), new TestCase("Mobclix/android/2.3.6", "Mobclix/android", 2003006),
            new TestCase("Mobclix/android/2.3.7", "Mobclix/android", 2003007), new TestCase("Mobclix/android/3.0.1", "Mobclix/android", 3000001),
            new TestCase("Mobclix/android/3.1", "Mobclix/android", 3001000), new TestCase("Mobclix/android/3.1.1", "Mobclix/android", 3001001),
            new TestCase("Mobclix/android/3.1.3", "Mobclix/android", 3001003), new TestCase("Mobclix/android/3.1.4", "Mobclix/android", 3001004),
            new TestCase("Mobclix/android/3.2", "Mobclix/android", 3002000), new TestCase("Mobclix/android/3.2.0", "Mobclix/android", 3002000),
            new TestCase("Mobclix/android/3.2.1", "Mobclix/android", 3002001), new TestCase("Mobclix/ipad/3.2", "Mobclix/ipad", 3002000),
            new TestCase("Mobclix/ipad/3.2.2", "Mobclix/ipad", 3002002), new TestCase("Mobclix/ipad/4.2", "Mobclix/ipad", 4002000),
            new TestCase("Mobclix/ipad/4.2.1", "Mobclix/ipad", 4002001), new TestCase("Mobclix/ipad/4.3", "Mobclix/ipad", 4003000),
            new TestCase("Mobclix/ipad/4.3.1", "Mobclix/ipad", 4003001), new TestCase("Mobclix/ipad/4.3.2", "Mobclix/ipad", 4003002),
            new TestCase("Mobclix/ipad/4.3.3", "Mobclix/ipad", 4003003), new TestCase("Mobclix/ipad/4.3.4", "Mobclix/ipad", 4003004),
            new TestCase("Mobclix/ipad/4.3.5", "Mobclix/ipad", 4003005), new TestCase("Mobclix/ipad/5.0", "Mobclix/ipad", 5000000),
            new TestCase("Mobclix/ipad/5.0.1", "Mobclix/ipad", 5000001), new TestCase("Mobclix/iphone/3.0", "Mobclix/iphone", 3000000),
            new TestCase("Mobclix/iphone/3.1", "Mobclix/iphone", 3001000), new TestCase("Mobclix/iphone/3.1.1", "Mobclix/iphone", 3001001),
            new TestCase("Mobclix/iphone/3.1.2", "Mobclix/iphone", 3001002), new TestCase("Mobclix/iphone/3.1.3", "Mobclix/iphone", 3001003),
            new TestCase("Mobclix/iphone/3.2", "Mobclix/iphone", 3002000), new TestCase("Mobclix/iphone/3.2.2", "Mobclix/iphone", 3002002),
            new TestCase("Mobclix/iphone/4.0", "Mobclix/iphone", 4000000), new TestCase("Mobclix/iphone/4.0.1", "Mobclix/iphone", 4000001),
            new TestCase("Mobclix/iphone/4.0.2", "Mobclix/iphone", 4000002), new TestCase("Mobclix/iphone/4.1", "Mobclix/iphone", 4001000),
            new TestCase("Mobclix/iphone/4.2.1", "Mobclix/iphone", 4002001), new TestCase("Mobclix/iphone/4.2.10", "Mobclix/iphone", 4002010),
            new TestCase("Mobclix/iphone/4.2.2", "Mobclix/iphone", 4002002), new TestCase("Mobclix/iphone/4.2.6", "Mobclix/iphone", 4002006),
            new TestCase("Mobclix/iphone/4.2.8", "Mobclix/iphone", 4002008), new TestCase("Mobclix/iphone/4.3", "Mobclix/iphone", 4003000),
            new TestCase("Mobclix/iphone/4.3.1", "Mobclix/iphone", 4003001), new TestCase("Mobclix/iphone/4.3.2", "Mobclix/iphone", 4003002),
            new TestCase("Mobclix/iphone/4.3.3", "Mobclix/iphone", 4003003), new TestCase("Mobclix/iphone/4.3.4", "Mobclix/iphone", 4003004),
            new TestCase("Mobclix/iphone/4.3.5", "Mobclix/iphone", 4003005), new TestCase("Mobclix/iphone/4.4.1rc1", "Mobclix/iphone", 4004001),
            new TestCase("Mobclix/iphone/4.4.2", "Mobclix/iphone", 4004002), new TestCase("Mobclix/iphone/5.0", "Mobclix/iphone", 5000000),
            new TestCase("Mobclix/iphone/5.0.1", "Mobclix/iphone", 5000001), new TestCase("Mobclix/test/0.1", "Mobclix/test", 1000),
            new TestCase("Mobclix/test/0.0.1", "Mobclix/test", 1), new TestCase("Mobclix/test/0.0.0", "Mobclix/test", 0), new TestCase("Mobclix/test/0.0.0", "Mobclix/test", 0),
            new TestCase("Nexage/App/0.0", "Nexage/App", 0), new TestCase("rtb", null, null), new TestCase("Smaato/RichMedia/0.0", "Smaato/RichMedia", 0),
            new TestCase("Smaato/RichMediaMobileWeb/1.0", "Smaato/RichMediaMobileWeb", 1000000), new TestCase("Tiemen/RichMediaMadView/1.0", "Tiemen/RichMediaMadView", 1000000),
            new TestCase("Tiemen/RichMediaOrmmaMM4RM/1.0", "Tiemen/RichMediaOrmmaMM4RM", 1000000), new TestCase("Tiemen/RichMediaWeb/1.0", "Tiemen/RichMediaWeb", 1000000),
            new TestCase("unknown/ANDROID_APP", "unknown", null), new TestCase("unknown/IPAD_APP", "unknown", null), new TestCase("unknown/IPHONE_APP", "unknown", null),
            new TestCase("unknown/IPHONE_SITE", "unknown", null),
            new TestCase("unknown/JAVA_APP", "unknown", null),
            new TestCase("unknown/MOBILE_SITE", "unknown", null),
            new TestCase("unknown/OTHER_APP", "unknown", null),

            // These are just random test cases to test 4 tokens instead of 3
            new TestCase("a/b/1.2.3.4", "a/b", 1002003), new TestCase("a/b/11.22.33.44", "a/b", 11022033), new TestCase("a/b/111.222.333.444", "a/b", 111222333),
            new TestCase("a/b/1.2.3", "a/b", 1002003),
            new TestCase("a/b/11.22.33", "a/b", 11022033),
            new TestCase("a/b/111.222.333", "a/b", 111222333),

            // This one is valid
            new TestCase("Whatever/123.a", "Whatever", 123000000),

            // Negative testing
            new TestCase("Whatever/1.2.1000", "Whatever", null), new TestCase("Whatever/1.2.3333", "Whatever", null), new TestCase("Whatever/1.2222.3", "Whatever", null),
            new TestCase("Whatever/11111.2.3", "Whatever", null), new TestCase("Whatever/.1.2.3", "Whatever", null), new TestCase("Whatever/.anything", "Whatever", null),
            new TestCase("Whatever/1..2.3", "Whatever", null), new TestCase("Whatever/invalid", "Whatever", null), };
}
