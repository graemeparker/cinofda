package com.adfonic.presentation.format.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Format;
import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.presentation.format.FormatService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.service.CommonManager;

@Service("formatService")
public class FormatServiceImpl extends GenericServiceImpl implements FormatService {
    
    @Autowired
    private CommonManager commonManager;
    
    public List<FormatDto> getFormatDtos(PublicationtypeDto publicationType){
        List<FormatDto> result = new ArrayList<FormatDto>();
        
        Map<String,Format> map = fillFormatMap(commonManager.getAllFormats());

        if (isStandardPubType(publicationType)) {
            result.add(getObjectDto(FormatDto.class,map.get("banner")));
            if(publicationType.getMedium().equals("SITE")){
                result.add(getObjectDto(FormatDto.class,map.get("iab300x250")));
                result.add(getObjectDto(FormatDto.class,map.get("image468x60")));
                result.add(getObjectDto(FormatDto.class,map.get("image728x90")));
                result.add(getObjectDto(FormatDto.class,map.get("image120x600")));
                result.add(getObjectDto(FormatDto.class,map.get("image160x600")));
                result.add(getObjectDto(FormatDto.class,map.get("image1024x90")));
            }
        }
        else {
            if (getAdSlotType(publicationType).equals("tablet")) {

                result.add(getObjectDto(FormatDto.class,map.get("banner")));
                result.add(getObjectDto(FormatDto.class,map.get("iab300x250")));
                result.add(getObjectDto(FormatDto.class,map.get("image320x480")));
                result.add(getObjectDto(FormatDto.class,map.get("image480x320")));
                result.add(getObjectDto(FormatDto.class,map.get("image468x60")));
                result.add(getObjectDto(FormatDto.class,map.get("image728x90")));
                result.add(getObjectDto(FormatDto.class,map.get("image120x600")));
                result.add(getObjectDto(FormatDto.class,map.get("image160x600")));
                result.add(getObjectDto(FormatDto.class,map.get("image1024x90")));
                result.add(getObjectDto(FormatDto.class,map.get("image1024x768")));
                result.add(getObjectDto(FormatDto.class,map.get("image768x1024")));
            }else{
                result.add(getObjectDto(FormatDto.class,map.get("banner")));
                result.add(getObjectDto(FormatDto.class,map.get("iab300x250")));

                if ("iphone".equals(getAdSlotType(publicationType)) || "android".equals(getAdSlotType(publicationType)) || "ipad".equals(getAdSlotType(publicationType))) {
                    result.add(getObjectDto(FormatDto.class,map.get("image320x480")));
                    result.add(getObjectDto(FormatDto.class,map.get("image480x320")));
                }
                
                if ("android".equals(getAdSlotType(publicationType))) {
                    result.add(getObjectDto(FormatDto.class,map.get("image1024x768")));
                    result.add(getObjectDto(FormatDto.class,map.get("image768x1024")));
                    result.add(getObjectDto(FormatDto.class,map.get("image120x600")));
                    result.add(getObjectDto(FormatDto.class,map.get("image160x600")));
                }

                if (getAdSlotType(publicationType).equals("ipad")) {
                    result.add(getObjectDto(FormatDto.class,map.get("image468x60")));
                    result.add(getObjectDto(FormatDto.class,map.get("image728x90")));
                    result.add(getObjectDto(FormatDto.class,map.get("image120x600")));
                    result.add(getObjectDto(FormatDto.class,map.get("image160x600")));
                    result.add(getObjectDto(FormatDto.class,map.get("image1024x90")));
                    result.add(getObjectDto(FormatDto.class,map.get("image1024x768")));
                    result.add(getObjectDto(FormatDto.class,map.get("image768x1024")));
                }
            }

        }
        return result;
    }
    
    public boolean isStandardPubType(PublicationtypeDto dto) {
        return ("other".equals(getAdSlotType(dto)));
    }

    // for display of previews etc
    public String getAdSlotType(PublicationtypeDto dto) {
        String adSlotType = "other";
        if(dto!=null){
            String pubTypeName = dto.getName().toLowerCase();
            if (pubTypeName.contains("java")) {
                adSlotType = "java";
            }else if(pubTypeName.contains("android")){
                adSlotType = "android";
            }else if (pubTypeName.contains("iphone")) {
                adSlotType = "iphone";
            } else if (pubTypeName.contains("ipad")) {
                adSlotType = "ipad";
            } else if (pubTypeName.contains("tablet")) {
                adSlotType = "tablet";
            } else if (pubTypeName.equalsIgnoreCase("RIM Application")) {
                adSlotType = "rim";
            } else if (pubTypeName.equalsIgnoreCase("Windows Application")) {
                adSlotType = "windows";
            }
        }
        
        return adSlotType;
    }
    
    public FormatDto getFormatByName(final String name){
        Format format = commonManager.getFormatByName(name);
        return getObjectDto(FormatDto.class, format);
    }
    
    public FormatDto getFormatById(final Long id){
        Format format = commonManager.getFormatById(id);
        return getObjectDto(FormatDto.class, format);
    }    
    
    public FormatDto getFormatBySystemName(final String name){
        Format format = commonManager.getFormatBySystemName(name);
        return getObjectDto(FormatDto.class, format);
    }
    
    public List<FormatDto> getAllFormats(){
        List<FormatDto> res = new ArrayList<FormatDto>();
        
        for(Format f : commonManager.getAllFormats()){
            FormatDto dto = getObjectDto(FormatDto.class, f);
            dto.setEnabled(true);
            res.add(dto);
        }
        
        return res;
    }
    
    public FormatDto getFormat(int width,int height){
        if(isBannerSize(width, height)){
            return getFormatBySystemName("banner");
        }
        else if(width==300 && height==75){
            return getFormatBySystemName("image300x75");
        }
        else if(width==320 && height==75){
            return getFormatBySystemName("image320x75");
        }
        else if(width==300 && height==250){
            return getFormatBySystemName("iab300x250");
        }
        else if(width==728 && height==90){
            return getFormatBySystemName("image728x90");
        }
        else if(width==468 && height==60){
            return getFormatBySystemName("image468x60");
        }
        else if(width==120 && height==600){
            return getFormatBySystemName("image120x600");
        }
        else if(width==160 && height==600){
            return getFormatBySystemName("image160x600");
        }
        else if(width==320 && height==480){
            return getFormatBySystemName("image320x480");
        }
        else if(width==480 && height==320){
            return getFormatBySystemName("image480x320");
        }
        else if(width==1024 && height==90){
            return getFormatBySystemName("image1024x90");
        }
        else if(width==1024 && height==768){
            return getFormatBySystemName("image1024x768");
        }
        else if(width==768 && height==1024){
            return getFormatBySystemName("image768x1024");
        }
        return null;
    }
    
    public FormatDto getVideoFormat(int width, int height) {
        List<FormatDto> allFormats = getAllFormats();
        int smallestWidth = Integer.MAX_VALUE;
        int smallestHeight = Integer.MAX_VALUE;
        FormatDto smallestDto = null;
        for (FormatDto formatDto : allFormats) {
            String fSystemName = formatDto.getSystemName();
            if (fSystemName.startsWith("video") && fSystemName.contains("x")) {
                String[] dimensions = formatDto.getSystemName().substring("video".length()).split("x");
                int fWidth = Integer.parseInt(dimensions[0]);
                int fHeight = Integer.parseInt(dimensions[1]);
                boolean orienationOk = (fWidth > fHeight && width > height) || (fWidth <= fHeight && width <= height);
                if (orienationOk && fWidth >= width && fHeight >= height) { //same orientation and smaller dimensions
                    if (smallestDto == null || smallestWidth > fWidth || smallestHeight > fHeight) {
                        smallestDto = formatDto;
                        smallestWidth = fWidth;
                        smallestHeight = fHeight;
                    }
                }
            }
        }
        return smallestDto;
    }
    
    //PRIVATE METHODS
    private boolean isBannerSize(int width,int height){
        return (width==120 && height==20) ||
               (width==168 && height==28) ||
               (width==216 && height==36) ||
               (width==300 && height==50) ||
               (width==300 && height==150) ||
               (width==320 && height==50);
               
    }
    
    private Map<String,Format> fillFormatMap(List<Format> formats){
        Map<String,Format> map = new HashMap<String, Format>();
        
        for(Format f : formats){
            map.put(f.getSystemName(), f);
        }
        
        return map;
    }
}
