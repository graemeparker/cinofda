package com.adfonic.util.stats;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class exposes all Counters created in CounterManager class Object
 * 
 * @author ravi
 *
 */
public class CounterJmxManager implements DynamicMBean {
    private final CounterManager counterManager;

    @Autowired
    public CounterJmxManager(CounterManager counterManager) {
        this.counterManager = counterManager;
    }

    @Override
    public synchronized String getAttribute(String name) throws AttributeNotFoundException {
        AtomicLong counter = counterManager.getCounters().get(name);
        if (counter != null) {
            return counter.toString();
        } else {
            throw new AttributeNotFoundException("No such Counter: " + name);
        }
    }

    // All counters are set as readonly, so this function call will not work,
    // but still
    // implementing the function in case in future we want to reset the counters
    @Override
    public synchronized void setAttribute(Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
        String name = attribute.getName();
        AtomicLong counter = counterManager.getCounters().get(name);
        if (counter == null) {
            throw new AttributeNotFoundException(name);
        }
        Object value = attribute.getValue();
        Long longValue = null;
        try {
            longValue = Long.parseLong(value.toString());
        } catch (Exception ex) {
            throw new InvalidAttributeValueException("Attribute value not a valid Long value: " + value);
        }
        counter.set(longValue);
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
            AtomicLong value = counterManager.getCounters().get(name);
            if (value != null) {
                list.add(new Attribute(name, value.get()));
            }
        }
        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList list) {
        Attribute[] attrs = list.toArray(new Attribute[list.size()]);
        AttributeList retlist = new AttributeList();
        Long longValue = null;
        for (Attribute attr : attrs) {
            String name = attr.getName();
            Object value = attr.getValue();
            AtomicLong counter = counterManager.getCounters().get(name);
            if (counter != null) {
                try {
                    longValue = Long.parseLong(value.toString());
                } catch (Exception ex) {
                    continue;
                    // Continue the loop and set what u can set
                }
                counter.set(longValue);
                retlist.add(new Attribute(name, value));
            }
        }
        return retlist;
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig) throws MBeanException, ReflectionException {
        /*
         * if (name.equals("reload") && (args == null || args.length == 0) &&
         * (sig == null || sig.length == 0)) { //Nothing to do }
         */
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        SortedSet<String> names = new TreeSet<String>();
        for (String counterName : counterManager.getCounters().keySet()) {
            names.add(counterName);
        }
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
        Iterator<String> it = names.iterator();
        for (int i = 0; i < attrs.length; i++) {
            String name = it.next();
            attrs[i] = new MBeanAttributeInfo(name, "java.lang.String", "Counter : " + name, true, // isReadable
                    false, // isWritable
                    false); // isIs
        }
        MBeanOperationInfo[] opers = { new MBeanOperationInfo("reload", "Reload properties from file", null, // no
                                                                                                             // parameters
                "void", MBeanOperationInfo.ACTION) };
        return new MBeanInfo(this.getClass().getName(), "Various Adfonic Counters", attrs, null, // constructors
                opers, null); // notifications
    }
}
