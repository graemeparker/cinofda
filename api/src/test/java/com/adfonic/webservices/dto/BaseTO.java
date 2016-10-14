package com.adfonic.webservices.dto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

//make this generic atleast
public class BaseTO {

    public int code = Integer.MIN_VALUE;
    public String description;


    // TODO - on the comparison side; Should be thread-local ideally. for later
    protected String[] exclusionList() {
        return (exclList);
    }

    String[] exclList = { "exclList" };


    public BaseTO exclude(String... newExcls) {
        exclList = (String[]) ArrayUtils.addAll(exclList, newExcls);
        return (this);
    }


    // basic
    @Override
    public boolean equals(Object o) {
        Class curCls = this.getClass();

        if (o == null || !o.getClass().equals(curCls)) {
            return (false);
        }

        List<String> exclList = Arrays.asList(exclusionList());

        for (Field f : curCls.getFields()) {
            try {
                if (!exclList.contains(f.getName())) {
                    assertEqual(this, o, f);
                }
            } catch (Exception e) {
                System.err.println("Mismatch around [class: " + curCls + " field: " + f.getName() + "]");
                return (false);
            }
        }

        return (true);
    }


    private void assertEqual(Object o1, Object o2, Field field) throws Exception {
        Object o1prop = field.get(o1);

        Object o2prop = field.get(o2);

        if ((o1prop == o2prop) || o1prop.equals(o2prop) || (field.getType().isArray() && Arrays.deepEquals((Object[]) o1prop, (Object[]) o2prop)))
            return;

        throw new RuntimeException();
    }


    @Override
    public String toString() {
        Class curCls = this.getClass();
        // String toString="\n"+curCls.getName()+":\n";
        String toString = "";
        for (Field f : curCls.getFields()) {
            try {
                String val = f.getType().isArray() ? Arrays.toString((Object[]) f.get(this)) : "" + f.get(this);
                toString += "\t" + f.getName() + ": " + val + "\n";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (toString);
    }
}
