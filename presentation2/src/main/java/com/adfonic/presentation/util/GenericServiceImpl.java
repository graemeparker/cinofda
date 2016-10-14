package com.adfonic.presentation.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jdto.DTOBinder;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.dto.BusinessKeyDTO;

public abstract class GenericServiceImpl {
    
    @Autowired
    private DTOBinder dtoBinder;
    
    @Autowired
    private org.dozer.Mapper mapper;
    
    public <T> List<T> getList(final Class<T> type, final Collection<?> col) {
        List<T> result = null;
    	if(!CollectionUtils.isEmpty(col)) {
    	    result = new ArrayList<T>(col.size());
    		for(Object obj :col){
    			result.add(getObjectDto(type,obj));
    		}
    	}else{
    	    result = new ArrayList<T>(0);
    	}
		return result;
    }
    
    public <T> Set<T> getSet(final Class<T> type, final Collection<?> col) {
        Set<T> result = null;
        if(!CollectionUtils.isEmpty(col)) {
            result = new HashSet<T>(col.size());
            for(Object obj :col){
                result.add(getObjectDto(type,obj));
            }
        }else{
            result = new HashSet<T>(0);
        }
        return result;
    }
    
    public <T> List<T> getDtoList(final Class<T> type, final Collection<?> col) {
        List<T> result = new ArrayList<T>(col.size());
    		for(Object obj :col){
    			T typ = mapper.map(obj, type);
    			result.add(typ);
    		}
        return result;
    }
    
    public <T> Set<T> getDtoSet(final Class<T> type, final Collection<?> col) {
        Set<T> result = new HashSet<T>(col.size());
            for(Object obj :col){
                T typ = mapper.map(obj, type);
                result.add(typ);
            }
        return result;
    }

    public <T> T getObjectDto(final Class<T> type, final Object source) {
    	return dtoBinder.bindFromBusinessObject(type, source);
    }
    
    public <T> T getDtoObject(final Class<T> type, final Object source) {
		return  mapper.map(source, type);
    }
    
    public <T> T getDtoFromObject(final Class<T> type, final Object source) {
        return dtoBinder.extractFromDto(type, source);
    }    

    public List<Long> getIdsForBusinessKeyDtos(List<? extends BusinessKeyDTO> objs) {
        List<Long> result = new ArrayList<Long>();
        for (BusinessKeyDTO businessKeyDTO : objs) {
            result.add(businessKeyDTO.getId());
        }
        return result;
    }
	
    protected <T> List<T> makeListFromCollection(Collection<T> collection) {
    	if(collection instanceof List) {
    		return (List<T>)collection;
    	} else {
    		return new ArrayList<T>(collection);
    	}
    }
}
