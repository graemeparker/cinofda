package com.adfonic.webservices.dto.mapping;

import org.apache.commons.beanutils.PropertyUtils;
import org.dozer.CustomConverter;

import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.ServiceException;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

public abstract class BaseReferenceEntityConverter<R> implements CustomConverter {

    private String identifyingAttribute = "name";
    private Class<R> clazz;

    public BaseReferenceEntityConverter(Class<R> clazz, String identifyingAttribute) {
        this.clazz = clazz;
        this.identifyingAttribute = identifyingAttribute;
    }

    public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {

        if (source == null) {
            return null;
        }

        R dest = null;

        if (source instanceof String) {
            if (destination == null) {
                dest = findReferenceEntityByAttribute((String) source);
            } else {
                throw new RuntimeException("Not expecting such a mapping");
            }
            return dest;

        } else if (clazz.isAssignableFrom(sourceClass)) {
            try {
                return (PropertyUtils.getProperty(source, identifyingAttribute));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new RuntimeException("Mapping usage incorrect");
        }
    }

    public R findReferenceEntityByAttribute(String identifyingValue) {
        R r = resolveEntity(identifyingValue);
        if (r != null) {
            return r;
        } else {
            throw new ServiceException(ErrorCode.REFERENCE_ENTITY_NOT_FOUND, "Found no " + clazz.getSimpleName() + " with [ " + identifyingAttribute + " = " + identifyingValue + " ]");
        }            
    }

    protected abstract R resolveEntity(String identifyingValue);
    
    protected final CommonManager getCommonManager() {
        return AdfonicBeanDispatcher.getBean(CommonManager.class);
    }
    
    protected final CampaignManager getCampaignManager() {
        return AdfonicBeanDispatcher.getBean(CampaignManager.class);
    }
    
    protected final DeviceManager getDeviceManager() {
        return AdfonicBeanDispatcher.getBean(DeviceManager.class);
    }
    
    protected final PublisherManager getPublisherManager() {
        return AdfonicBeanDispatcher.getBean(PublisherManager.class);
    }
}
