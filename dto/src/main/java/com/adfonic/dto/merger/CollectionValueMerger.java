package com.adfonic.dto.merger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdto.BeanModifier;
import org.jdto.BeanModifierAware;
import org.jdto.MultiPropertyValueMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is MultiPropertyValueMerger implementation used by JDTO to merge a List
 * of values into one list. Values can be an Object and/or a Collection. You can
 * also define a extraParam to obtain the bean value from the Values, or provide
 * <tt>null</tt> to just get the actual value in the returning list.
 *
 * For example,
 *
 * To get properties from two source beans, roles from Domain object and roles
 * from Domain object property Company which has a property roles.
 *
 * @Sources(value = { @Source("roles"), @Source("company.roles") }, merger =
 *                CollectionValueMerger.class, mergerParam = "name")
 *
 *                This annotation will inform jDTOBinder to get roles from the
 *                Domain object and roles from Domain object using property
 *                company.roles. Once the values from both of these sources are
 *                retrieved this Merger class will use the MergeParam value to
 *                get the property values. So in this case, whatever Object type
 *                is returned from Sources the values returned to populate the
 *                DTO will be the property value of name. If do not provide a
 *                MergeParam it will pass the raw value to the returning list.
 *
 * @author antonysohal
 *
 */
public class CollectionValueMerger implements MultiPropertyValueMerger<Collection<Object>>, BeanModifierAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValueMerger.class);
    
    private static final long serialVersionUID = 1L;

    protected BeanModifier modifier;

    @Override
    public Collection<Object> mergeObjects(List<Object> values, String[] extraParam) {

        boolean rawObject = true;
        boolean extraParamPerValues = false;

        if (extraParam == null) {
            LOGGER.debug("No extraParam passed, so just merging value in to one Collections");
        } else if (extraParam.length == 1) {
            rawObject = false;
            extraParamPerValues = false;
            LOGGER.debug("Using property of {} from extraParam passed", extraParam[0]);
        } else if (extraParam.length == values.size()) {
            extraParamPerValues = true;
        }

        Collection<Object> result = new ArrayList<Object>();
        int curr = 0;

        for (Object value : values) {
            if (value == null) {
                LOGGER.debug("value of index [{}] passed was null", curr);
            } else {
                if (value instanceof Collection<?>) {
                    curr = mergeCollection(extraParam, rawObject, extraParamPerValues, result, curr, value);
                } else {
                    mergeObject(extraParam, rawObject, extraParamPerValues, result, curr, value);
                    curr++;
                }
                
            }
        }
        return result;
    }

    private int mergeCollection(String[] extraParam, boolean rawObject, boolean extraParamPerValues, Collection<Object> result, int curr, Object value) {
        int currResult = curr;
        Collection<?> collection = (Collection<?>) value;
        if (rawObject) {
            result.addAll(collection);
        }else{
            for (Object object : collection) {
                if (extraParamPerValues) {
                    result.add(modifier.readPropertyValue(extraParam[curr], object));
                } else {
                    result.add(modifier.readPropertyValue(extraParam[0], object));
                }
            }
            currResult++;
        }
        return currResult;
    }
    
    private void mergeObject(String[] extraParam, boolean rawObject, boolean extraParamPerValues, Collection<Object> result, int curr, Object value) {
        if (rawObject) {
            result.add(value);
        } else if (extraParamPerValues) {
            result.add(modifier.readPropertyValue(extraParam[curr], value));
        } else {
            result.add(modifier.readPropertyValue(extraParam[0], value));
        }
    }

    @Override
    public void setBeanModifier(BeanModifier modifier) {
        this.modifier = modifier;
    }

}
