package com.adfonic.adserver.rtb.dec;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adfonic.adserver.rtb.PriceDecrypter;
import com.adfonic.domain.RtbConfig.DecryptionScheme;

/*
 * Not associated with any scheme. Infact can contain multiple sets of values that
 * can be used for multiple type of schemes simulataneously
 */
public final class SecurityAlias {

    private final String name;

    public static final String ENC_KEY_64 = "eKey64";
    public static final String INT_KEY_64 = "iKey64";
    public static final String ENC_KEY_0X = "eKeyX";
    public static final String INT_KEY_0X = "iKeyX";
    public static final String PASS_KEY = "pass";

    private final String encKeyStr64;
    private final String intKeyStr64;

    private final String encKeyStr0x;
    private final String intKeyStr0x;

    private final String passKeyStr;

    private PriceDecrypter priceDecrypter;

    public String encryptionKey64() {
        return encKeyStr64;
    }

    public String integrityKey64() {
        return intKeyStr64;
    }

    public String encryptionKey0x() {
        return encKeyStr0x;
    }

    public String integrityKey0x() {
        return intKeyStr0x;
    }

    public String passKey() {
        return passKeyStr;
    }

    public String getName() {
        return name;
    }

    public static class GenBuilder {

        private String name;
        private Map<String, String> propMap = new HashMap<>();

        public GenBuilder(String name) {
            this.name = name;
        }

        public GenBuilder set(String name, String value) {
            propMap.put(name, value);
            return this;
        }

        public SecurityAlias build() {
            return new SecurityAlias(this);
        }
    }

    private static ConcurrentMap<String, SecurityAlias> aliasMap = new ConcurrentHashMap<>();

    public static SecurityAlias valueOfCached(String alias) {
        return alias == null ? null : aliasMap.get(alias);
    }

    public static void rebuildCache(Properties properties) {
        Map<String, SecurityAlias.GenBuilder> aliasBldMap = new HashMap<>();
        Matcher aliasE = Pattern.compile("Rtb\\.Enc\\.(.*)\\.(.*)").matcher("");

        Set<String> propKeySet = properties.stringPropertyNames();
        for (String propKey : propKeySet) {
            if (aliasE.reset(propKey).matches()) {
                String aliasN = aliasE.group(1);
                SecurityAlias.GenBuilder aliasB = aliasBldMap.get(aliasN);
                if (aliasB == null) {
                    aliasBldMap.put(aliasN, aliasB = new SecurityAlias.GenBuilder(aliasN));
                }

                aliasB.set(aliasE.group(2), properties.getProperty(propKey).trim());
            }
        }

        aliasMap.clear();
        for (Entry<String, SecurityAlias.GenBuilder> entry : aliasBldMap.entrySet()) {
            aliasMap.put(entry.getKey(), entry.getValue().build());
        }

    }

    private SecurityAlias(GenBuilder builder) {
        name = builder.name;

        // use only props from map for which we've props
        encKeyStr64 = builder.propMap.get(ENC_KEY_64);
        intKeyStr64 = builder.propMap.get(INT_KEY_64);

        encKeyStr0x = builder.propMap.get(ENC_KEY_0X);
        intKeyStr0x = builder.propMap.get(INT_KEY_0X);

        passKeyStr = builder.propMap.get(PASS_KEY);

        //vdate
    }

    public PriceDecrypter getPriceCrypter(DecryptionScheme decryptionScheme) {
        if (priceDecrypter == null) {
            priceDecrypter = build(decryptionScheme);
        }
        return priceDecrypter;
    }

    private PriceDecrypter build(DecryptionScheme decryptionScheme) {
        switch (decryptionScheme) {
        case ADX:
            return new AdXEncUtil(encryptionKey64(), integrityKey64());
        case OPENX:
            return new OpenXUtil(encryptionKey0x(), integrityKey0x());
        case RBCN_BF:
            return new RubiconEncUtil(passKey());
        default:
            throw new IllegalStateException("Unsupported DecryptionScheme value: " + decryptionScheme);
        }
    }

}
