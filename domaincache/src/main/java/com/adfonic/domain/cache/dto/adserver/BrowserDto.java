package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.util.HttpRequestContext;

public class BrowserDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private String name;
    private Map<String, Pattern> headerPatternMap = new HashMap<String, Pattern>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Pattern> getHeaderPatternMap() {
        return headerPatternMap;
    }

    public boolean isMatch(HttpRequestContext context) {
        for (Map.Entry<String, Pattern> entry : headerPatternMap.entrySet()) {
            String value = context.getHeader(entry.getKey());
            if (value == null) {
                // https://tickets.adfonic.com/browse/AF-549
                // https://tickets.adfonic.com/browse/AF-585
                // Treat an absent header as the empty string so it will match a
                // regex such as "^$" if we need to enforce "absent or empty."
                value = "";
            }
            if (!entry.getValue().matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "BrowserDto {" + getId() + ", name=" + name + ", headerPatternMap=" + headerPatternMap + "}";
    }

}
