package com.adfonic.presentation.campaign.creative;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.adfonic.domain.Creative;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.creative.ContentTypeDto;
import com.adfonic.dto.campaign.creative.CreativeAttributeDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTemplateDto;
import com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.format.ContentSpecDto;
import com.adfonic.presentation.exceptions.BigFileException;
import com.adfonic.presentation.exceptions.FileExtensionNotSupportedException;
import com.adfonic.presentation.exceptions.NotContentTypeException;
import com.adfonic.presentation.exceptions.SizeNotSupportedException;

public interface CreativeService {

    public CreativeDto getCreativeById(Long id);
    
    public CreativeDto newImageForCreative(CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException, NotContentTypeException, IOException, SizeNotSupportedException, BigFileException;
    public CreativeDto newTextCreative(String data);
    public CreativeDto newNativeCreative(String title, String description);
    public CreativeDto newCreative3rdParty(String data,boolean isRichMedia);
    
    public CreativeDto newCreativeVast();
    public MobileAdVastMetadataDto processVastTag(CreativeDto creativeDto);
    public MobileAdVastMetadataDto processVastTag(String templateOriginal);
    
    public CreativeDto uploadIconForNativeAd (CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException, NotContentTypeException, IOException, SizeNotSupportedException, BigFileException;
    public CreativeDto uploadImageForNativeAd (CreativeDto creativeDto, InputStream is, String ct, byte[] data, long size) throws FileExtensionNotSupportedException, NotContentTypeException, IOException, SizeNotSupportedException, BigFileException;
    
    public List<ContentSpecDto> getAllContentSpecs();
    
    public List<ContentSpecDto> getContentSpecsForImages();
    
    public ContentSpecDto getContentSpecByName(String name);
    public ContentTypeDto getContentTypeForMime(String mimeType,boolean animated);
    
    public CreativeDto save(CreativeDto dto, CampaignCreativeDto campaign, boolean isReApprovalNeeded);
    public CreativeDto copyCreative(CreativeDto oldCreative, CampaignCreativeDto campaign);
    public Creative getCreativeForSubmission(Creative creative); 
    public CreativeDto submitCreative(CreativeDto creative, boolean directApproval);
    public void deleteCreative(CreativeDto dto);
    public List<DestinationType> getDestinationTypes(boolean isAndroidOnly,boolean isIosOnly);
    public List<ExtendedCreativeTypeDto> getExtendedCreativeTypes(Boolean isRichMedia, Boolean alsoShowHidden);
    public ExtendedCreativeTypeDto getExtendedCreativeTypeById(Long id);
    public List<ExtendedCreativeTemplateDto> getTemplates(ExtendedCreativeTypeDto type);
    
    List<CreativeAttributeDto> getAllCreativeAttributes();
    CreativeAttributeDto getCreativeAttributeById(Long id);
    CreativeAttributeDto getCreativeAttributeByName(String name);
    
    /**
     * Determine whether the creative is SSL Compliant or not sure (null). 
     */
    Boolean isSslCompliant(CreativeDto dto);
}
