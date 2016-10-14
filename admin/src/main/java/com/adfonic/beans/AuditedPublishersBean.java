package com.adfonic.beans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.adfonic.domain.Publisher;

@SessionScoped
@ManagedBean
public class AuditedPublishersBean extends BaseBean {
    
    private Collection<Long> appNexusPublisherIds;
    private Collection<Long> adxPublisherIds;
    
    Collection<Publisher> adxPublishers;
    Collection<Publisher> apnPublishers;
    
    public AuditedPublishersBean(){
    }
    
	@PostConstruct
    private void init() {
        // Getting Publisher Ids for Adx
        adxPublishers = getPublishers(adxPublisherIds);
        
        // Getting Publisher Ids for AppNexus
        apnPublishers = getPublishers(appNexusPublisherIds);
    }
    
    public void setAppNexusPublisherIds(Set<Long> appNexusPublisherIds) {
        this.appNexusPublisherIds = appNexusPublisherIds;
    }
    
    public void setAdxPublisherIds(Set<Long> adxPublisherIds) {
        this.adxPublisherIds = adxPublisherIds;
    }
    
    private Collection<Publisher> getPublishers(Collection<Long> publisherIds) {
        Collection<Publisher> publishers = new HashSet<Publisher>();
        for (Long id : publisherIds){
            publishers.add(getPublisherManager().getPublisherById(id));
        }
        return publishers;
    }

    public Collection<Publisher> getAdxPublishers() {
        return adxPublishers;
    }

    public Collection<Publisher> getApnPublishers() {
        return apnPublishers;
    }
}