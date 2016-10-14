package com.adfonic.webservices.dto.mapping;

import java.util.Set;

import org.dozer.CustomConverter;

import com.adfonic.domain.Browser;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

public class ExcludeOperaMiniConverter implements CustomConverter {

    @Override
    public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {

        if (source == null) {
            return destination;
        }

        // TODO: change this back to initialize-once...for now we can tolerate the extra query
        Browser excludeOperaMiniBrowser = AdfonicBeanDispatcher.getBean(DeviceManager.class).getBrowserByName("Exclude Opera Mini");

        if (source instanceof Set) {
            Set<Browser> browsers = (Set<Browser>) source;
            return Boolean.valueOf(browsers.contains(excludeOperaMiniBrowser));
        }

        boolean excludeOperaMini = (Boolean) source;
        Set<Browser> browsers = (Set<Browser>) destination;

        if (excludeOperaMini && !browsers.contains(excludeOperaMiniBrowser)) {
            browsers.add(excludeOperaMiniBrowser);
        } else if (!excludeOperaMini && browsers.contains(excludeOperaMiniBrowser)) {
            browsers.remove(excludeOperaMiniBrowser);
        }

        return browsers;
    }

}
