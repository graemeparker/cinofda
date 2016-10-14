package com.byyd.adx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mvanek
 */
public class AdxCreativeStatus {

    private static final String RESOURCE = "/AdX/creative-status-codes.txt";

    private static AdxCreativeStatus instance = new AdxCreativeStatus();

    public static AdxCreativeStatus instance() {
        return instance;
    }

    private final Map<Integer, String> code2name = new HashMap<Integer, String>();

    private AdxCreativeStatus() {
        InputStream stream = getClass().getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Classpath resource not found: " + RESOURCE);
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("utf-8")))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    int indexOf = line.indexOf(' ');
                    if (indexOf != -1) {
                        int code = Integer.parseInt(line.substring(0, indexOf));
                        String name = line.substring(indexOf + 1);
                        code2name.put(code, name);
                    }
                }
            }
        } catch (IOException iox) {
            throw new IllegalStateException("Failed to load resource: " + RESOURCE, iox);
        }
    }

    public String getName(Integer code) {
        return code2name.get(code);
    }
}
