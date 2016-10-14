package com.adfonic.webservices.view.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

/**
 * Mainly for XML generation of list
 * 
 * Based on - http://blog.bdoughan.com/2012/11/creating-generic-list-wrapper-in-jaxb.html
 * 
 */
public class ListWrapper<T> {

    private List<T> items;

    public ListWrapper(){
        
    }

    public ListWrapper(List<T> items) {
        this.items = items;
    }


    @XmlAnyElement(lax = true)
    public List<T> getItems() {
        return items;
    }

}
