package com.adfonic.presentation.reporting.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.math.NumberUtils;

/**
 * 
 * 
 * Responsibility: To provide easy access to the row class fields
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public class FieldIterator<T> {
    
    /** Column details information */
    private ColumnsDetails columnsDetails = null;
    
    /** Current object to be iterated*/
    private T object = null;
    
    /** Current iterator position*/
    private Integer currentFieldIndex = 0;
    
    /**
     * Constructor
     * 
     * @param columnsDetail Column details information
     * @param object Object to iterate
     */
    public FieldIterator(ColumnsDetails columnsDetail, T object){
        this.object = object;
        this.currentFieldIndex = 0;
        this.columnsDetails = columnsDetail;
    }
    
    /**
     * Indicates if there are other field to iterate
     * 
     * @return true if there are more fields to iterate, otherwise it returns false
     */
    public boolean hasNext() {
        boolean hasNext = false;
        if (currentFieldIndex<=columnsDetails.getColumns().size()-1){
            hasNext = true;
        }
        return hasNext;
    }
    
    /**
     * Returns the next field to iterate
     * 
     * @return Object value
     */
    public Object next(){
        Object value = null;
        if (this.object!=null){
            Column currentColumn = columnsDetails.getColumns().get(currentFieldIndex);
            value = getValueFromObject(this.object, currentColumn.getColumnNameKey());
            currentFieldIndex++;
        }
        return value;
    }
    
    /**
     * This method use reflection to invoke the get method the field specified as parameter  
     * 
     * @param instance Instance of the object
     * @param fieldName Field name to execute the get method
     * @return Getter value or null in case that the field does not exist
     */
    private Object getValueFromObject(Object instance, String fieldName) {
        Object valueObject = null;
        if (instance!=null){
            Class<?> clazz = instance.getClass();
            
            if (clazz != null){
                valueObject = getValueObject(instance, fieldName, clazz);
            }
        }    
        return valueObject;
    }

    @SuppressWarnings("rawtypes")
    private Object getValueObject(Object instance, String fieldName, Class<?> clazz) {
        Object valueObject = null;
        
        String getterName = null;
        Class[] paramsClasses = null;
        Object[] paramsValues = null;
        if (NumberUtils.isNumber(fieldName)){
            int index = NumberUtils.toInt(fieldName);
            getterName = "getData";
            paramsClasses = new Class[1];
            paramsClasses[0] = int.class;
            paramsValues = new Object[1];
            paramsValues[0] = index;
        }else{
            // get object value using reflection
            getterName = "get" + fieldName;
        }
        
        try {
            Method method = clazz.getMethod(getterName, paramsClasses);
            valueObject = method.invoke(instance, paramsValues);
            if (this.columnsDetails.getValueTransformer()!=null){
                valueObject = this.columnsDetails.getValueTransformer().transform(fieldName, valueObject);
            }
        } catch (NoSuchMethodException e) {
            // ignore reflection errors
        } catch (InvocationTargetException e) {
            // ignore reflection errors 
        } catch (IllegalAccessException e) {
            // ignore reflection errors
        }

        return valueObject;
    }
}
