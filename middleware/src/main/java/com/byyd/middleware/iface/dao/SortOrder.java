package com.byyd.middleware.iface.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.iface.exception.SortOrderException;

public class SortOrder {

    public static enum Direction {

        ASC("asc"), DESC("desc");

        private final String direction;

        private Direction(String direction) {
            this.direction = direction;
        }

        @Override
        public String toString() {
            return direction;
        }

    }

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    private final Direction direction;
    private final String fieldName;
    private final Class<?> clazz;
    
    public SortOrder(String property) {
        this(DEFAULT_DIRECTION, property);
    }
    public SortOrder(Class<?> clazz, String property) {
        this(DEFAULT_DIRECTION, clazz, property);
    }
    public SortOrder(Direction direction, String property) {
        this(direction, null, property);
    }

    public SortOrder(Direction direction, Class<?> clazz, String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName cannot be empty.");
        }

        this.direction = direction == null ? DEFAULT_DIRECTION : direction;
        this.fieldName = fieldName;
        this.clazz = clazz;
    }

    public static SortOrder asc(String field) {
        return asc(null, field);
    }
    public static SortOrder asc(Class<?> clazz, String field) {
         return new SortOrder(Direction.ASC, clazz, field);
    }

    public static SortOrder desc(String field) {
        return desc(null, field);
    }
    public static SortOrder desc(Class<?> clazz, String field) {
         return new SortOrder(Direction.DESC, clazz, field);
    }

    public static List<SortOrder> create(Direction direction, Iterable<String> properties) {
        return create(direction, null, properties);
    }

    public static List<SortOrder> create(Direction direction, Class<?> clazz, Iterable<String> properties) {
        List<SortOrder> orders = new ArrayList<SortOrder>();
        for (String property : properties) {
            orders.add(new SortOrder(direction, clazz, property));
        }
        return orders;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public int hashCode() {

        int result = 17;

        result = 31 * result + direction.hashCode();
        result = 31 * result + fieldName.hashCode();

        return result;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SortOrder)) {
            return false;
        }

        SortOrder that = (SortOrder) obj;

        return this.direction.equals(that.getDirection()) && this.fieldName.equals(that.getFieldName());
    }
    
    public static String getDatabaseFieldNameForEntityVariableName(Class<?> clazz, String variableName) {
        try {
            Field field = clazz.getDeclaredField(variableName);
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class); 
                return column.name();
            }
        } catch(Exception e) {
            throw new SortOrderException(e);
        }
        return variableName;
    }

    @Override
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean translateVariableNames) {
        StringBuilder buffer = new StringBuilder();
        
        String localFieldName = getFieldName();
        if(localFieldName.indexOf('.') != -1 && getClazz() != null && StringUtils.split(localFieldName, '.').length > 1) {
            // Dotted notation. We need to locate the last token in the dotted string
            // and determine the class of the entity containing it. That will be used
            // to generate a string using the same assumptions as below. We need a Class
            // to start the processing of course, so if none was passed, we dont branch here.
            
            String[] tokens = StringUtils.split(localFieldName, '.');
            Class<?> currentClazz = getClazz();
            for(int t = 0;t < tokens.length;t++) {
                String token = tokens[t];
                // we need to see what Class is its type
                Field field = null;
                try {
                    field = currentClazz.getDeclaredField(token);
                } catch (NoSuchFieldException e){
                    throw new SortOrderException("Could not retrieve field \"" + token + "\" from Class \"" + currentClazz.getCanonicalName() + "\"");
                } catch(Exception e) {
                    throw new SortOrderException("Error retrieving field \"" + token + "\" from Class \"" + currentClazz.getCanonicalName() + "\"", e);
                } 
                
                if(t == tokens.length - 1) {
                    appendLastToken(translateVariableNames, buffer, currentClazz, token, field);
                } else {
                    // Just a token in the list, carry on
                    currentClazz = field.getType();
                }
            }
        } else {
            if(getClazz() != null) {
                // Assumptions are made as to the table name that holds rows represented by
                // the Clazz object
                buffer.append(clazz.getSimpleName() + ".");
                if(translateVariableNames) {
                    buffer.append(getDatabaseFieldNameForEntityVariableName(clazz, getFieldName()));
                } else {
                     buffer.append(getFieldName());
                }
            } else {
                 buffer.append(getFieldName());
            }
            buffer.append(" " + getDirection().toString());
        }
        return buffer.toString();
    }
    
    private void appendLastToken(boolean translateVariableNames, StringBuilder buffer, Class<?> currentClazz, String token, Field field) {
        // This is the last entry in the token list, meaning the actual field to
        // sort by. If it is an entity, sort by its table name.ID, if not, 
        // grab the last currentClazz recorded and create the ORDER BY String based on it
        if(BusinessKey.class.isAssignableFrom(field.getType())) {
            buffer.append(field.getType().getSimpleName() + ".ID");
        } else {
            buffer.append(currentClazz.getSimpleName() + ".");
            if(translateVariableNames) {
                buffer.append(getDatabaseFieldNameForEntityVariableName(currentClazz, token));
            } else {
                 buffer.append(getFieldName());
            }
        }
        buffer.append(" " + getDirection().toString());
    }

}
