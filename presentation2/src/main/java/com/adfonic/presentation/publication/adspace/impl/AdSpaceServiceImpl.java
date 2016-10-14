package com.adfonic.presentation.publication.adspace.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.adspace.AdSpaceDto;
import com.adfonic.presentation.publication.adspace.AdSpaceService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@Service("adspaceService")
public class AdSpaceServiceImpl extends GenericServiceImpl implements AdSpaceService {
    
    private FetchStrategy adSpaceFs = new FetchStrategyBuilder()
    .addLeft(AdSpace_.formats)
    .build();

    @Autowired
    private PublicationManager publicationManager;
    
    @Autowired
    private CommonManager commonManager;

    public AdSpaceDto getAdSpaceById(final AdSpaceDto dto){
        AdSpace as = publicationManager.getAdSpaceById(dto.getId(),adSpaceFs);
        
        if(as!=null){
            return getObjectDto(AdSpaceDto.class,as);
        }
        else
            return null;
    }
    
    public AdSpaceDto save(AdSpaceDto dto,PublicationDto publicationDto) throws Exception{
     // Update Adspace
        if (dto.getId() != null && dto.getId().longValue() > 0) {
            //if persisted
            AdSpace adSpace = publicationManager.getAdSpaceById(dto.getId(),adSpaceFs);
            
            adSpace.setName(dto.getName());
            adSpace.setColorScheme(dto.getColorScheme());
            adSpace.getFormats().clear();
            for(FormatDto f : dto.getFormats()){
                Format form = commonManager.getFormatById(f.getId());
                adSpace.getFormats().add(form);
            }
            
            publicationManager.update(adSpace);
                
        } else{
            Publication p = publicationManager.getPublicationById(publicationDto.getId());
            AdSpace adSpace = publicationManager.newAdSpace(p);
            p = publicationManager.update(p);
            
            adSpace.setName(dto.getName());
            adSpace.setColorScheme(dto.getColorScheme());
            adSpace.getFormats().clear();
            for(FormatDto f : dto.getFormats()){
                Format form = commonManager.getFormatById(f.getId());
                adSpace.getFormats().add(form);
            }
            
            publicationManager.update(adSpace);
            
            adSpace = publicationManager.getAdSpaceById(adSpace.getId(), adSpaceFs);
            dto = getObjectDto(AdSpaceDto.class, adSpace);
            if(!CollectionUtils.isEmpty(dto.getFormats()))
                dto.setFormat(dto.getFormats().get(0));
        }
        
        return dto;
    }
    
    public void delete(AdSpaceDto dto) throws Exception{
        AdSpace adSpace = publicationManager.getAdSpaceById(dto.getId(), adSpaceFs);
        publicationManager.delete(adSpace);
    }
}
