package com.adfonic.util;

import java.util.Iterator;
import java.util.logging.Logger;

import org.junit.Test;

public class TestAcceptedLanguages {
    private static final transient Logger LOG = Logger.getLogger(TestAcceptedLanguages.class.getName());

    /*
     * @Test public void speedTest() { AcceptedLanguages a =
     * AcceptedLanguages.parse("en;q=1.0, esn;q=0.5, it;q=0.3, fr;q=0.2");
     * java.util.Collection<String> isoCodes; StopWatch stopWatch = new
     * StopWatch(); stopWatch.start(); for (int k = 0; k < 10000000; ++k) {
     * isoCodes = a.getIsoCodes(); } stopWatch.stop(); LOG.info("Elapsed: " +
     * stopWatch); }
     */

    @Test
    public void test() throws Exception {
        AcceptedLanguages a;
        AcceptedLanguages.AcceptedLanguage lang;
        String langSpec;
        int errorCount = 0;
        Iterator<AcceptedLanguages.AcceptedLanguage> iter;

        a = AcceptedLanguages.parse((langSpec = "*"));
        if (!a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("xx") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check xx");
        }

        a = AcceptedLanguages.parse((langSpec = "-"));
        if (!a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("xx") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check xx");
        }

        a = AcceptedLanguages.parse((langSpec = "*; q=0.5"));
        if (!a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed overall any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("xx") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check xx");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!lang.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        a = AcceptedLanguages.parse((langSpec = "-; q=0.5"));
        if (!a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed overall any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("xx") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check xx");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!lang.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        a = AcceptedLanguages.parse((langSpec = "en-us"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        a = AcceptedLanguages.parse((langSpec = "en_US"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        a = AcceptedLanguages.parse((langSpec = "en-us;q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        a = AcceptedLanguages.parse((langSpec = "en_US;q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check");
        }

        // space after the semicolon
        a = AcceptedLanguages.parse((langSpec = "en-us; q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality-following-space check");
        }

        a = AcceptedLanguages.parse((langSpec = "es;q=1.0, en;q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        } else if (a.getQuality("fr") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check fr");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"es".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 0");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 0");
        }
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 1");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 1");
        }

        // no space after comma
        a = AcceptedLanguages.parse((langSpec = "es;q=1.0,en;q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        } else if (a.getQuality("fr") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check fr");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"es".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 0");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 0");
        }
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 1");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 1");
        }

        // Try it in reverse order, should sort out properly
        a = AcceptedLanguages.parse((langSpec = "en;q=0.5, es;q=1.0"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        } else if (a.getQuality("fr") != 0.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check fr");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"es".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 0");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 0");
        }
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 1");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 1");
        }

        // "one preferred, but any fallback" case
        a = AcceptedLanguages.parse((langSpec = "en-gb, *; q=0.5"));
        if (a.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check");
        } else if (a.getQuality("en") != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check en");
        } else if (a.getQuality("es") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check es");
        } else if (a.getQuality("fr") != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check fr");
        }
        iter = a.getLanguages().iterator();
        lang = iter.next();
        if (!"en".equals(lang.getIsoCode())) {
            ++errorCount;
            LOG.severe(langSpec + " failed iso code check 0");
        } else if (lang.getQuality() != 1.0) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 0");
        }
        lang = iter.next();
        if (!lang.isAny()) {
            ++errorCount;
            LOG.severe(langSpec + " failed any check 1");
        } else if (lang.getQuality() != 0.5) {
            ++errorCount;
            LOG.severe(langSpec + " failed quality check 1");
        }

        if (errorCount > 0) {
            throw new Exception(errorCount + " error(s) occurred");
        }
    }
}
