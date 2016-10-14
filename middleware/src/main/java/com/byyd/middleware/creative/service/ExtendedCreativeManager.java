package com.byyd.middleware.creative.service;

import java.util.List;
import java.util.Map;

import com.adfonic.domain.ClickTokenReference;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeTypeMacro;
import com.adfonic.domain.MediaType;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface ExtendedCreativeManager extends BaseManager {

    //------------------------------------------------------------------------------
    // ExtendedCreativeType
    //------------------------------------------------------------------------------
    ExtendedCreativeType newExtendedCreativeType(String name, MediaType mediaType, FetchStrategy... fetchStrategy);
    ExtendedCreativeType getExtendedCreativeTypeById(String id, FetchStrategy... fetchStrategy);
    ExtendedCreativeType getExtendedCreativeTypeById(Long id, FetchStrategy... fetchStrategy);
    ExtendedCreativeType update(ExtendedCreativeType extendedCreativeType);
    void delete(ExtendedCreativeType extendedCreativeType);
    void deleteExtendedCreativeTypes(List<ExtendedCreativeType> list);

    Long countAllExtendedCreativeTypes();
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(FetchStrategy ... fetchStrategy);
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(Sorting sort, FetchStrategy ... fetchStrategy);
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(Pagination page, FetchStrategy ... fetchStrategy);
    
    Long countAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter);
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // ExtendedCreativeTemplate
    //------------------------------------------------------------------------------------------
    String makeReplacementMacroName(String target);
    String getSubstitutionStringForClickTokenReference(ClickTokenReference token);
    String processExtendedCreativeTemplateContent(ExtendedCreativeTemplate template, Creative creative);
    ExtendedCreativeTemplate newExtendedCreativeTemplate(Creative creative, ContentForm contentForm, String templateOriginal, FetchStrategy... fetchStrategy);
    ExtendedCreativeTemplate newExtendedCreativeTemplate(Creative creative, ContentForm contentForm, String templateOriginal, String templatePreprocessed, FetchStrategy... fetchStrategy);

    ExtendedCreativeTemplate getExtendedCreativeTemplateById(String id, FetchStrategy... fetchStrategy);
    ExtendedCreativeTemplate getExtendedCreativeTemplateById(Long id, FetchStrategy... fetchStrategy);
    ExtendedCreativeTemplate create(ExtendedCreativeTemplate extendedCreativeTemplate);
    ExtendedCreativeTemplate update(ExtendedCreativeTemplate extendedCreativeTemplate);
    void delete(ExtendedCreativeTemplate extendedCreativeTemplate);
    void deleteExtendedCreativeTemplates(List<ExtendedCreativeTemplate> list);

    Long countAllExtendedCreativeTemplatesForCreative(Creative creative);
    List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy);
    
    Map<ContentForm, ExtendedCreativeTemplate> getExtendedCreativeTemplatesMapForCreative(Creative creative, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // ExtendedCreativeTypeMacro
    //------------------------------------------------------------------------------------------

    ExtendedCreativeTypeMacro getExtendedCreativeTypeMacroById(String id, FetchStrategy... fetchStrategy);
    ExtendedCreativeTypeMacro getExtendedCreativeTypeMacroById(Long id, FetchStrategy... fetchStrategy);
    ExtendedCreativeTypeMacro create(ExtendedCreativeTypeMacro extendedCreativeTypeMacro);
    ExtendedCreativeTypeMacro update(ExtendedCreativeTypeMacro extendedCreativeTypeMacro);
    void delete(ExtendedCreativeTypeMacro extendedCreativeTypeMacro);
    void deleteExtendedCreativeTypeMacros(List<ExtendedCreativeTypeMacro> list);
    
    Long countAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType);
    List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // ClickTokenReference
    //------------------------------------------------------------------------------
    ClickTokenReference newClickTokenReference(String token, String type, String exampleOutput, String description, FetchStrategy... fetchStrategy);
    ClickTokenReference getClickTokenReferenceById(String id, FetchStrategy... fetchStrategy);
    ClickTokenReference getClickTokenReferenceById(Long id, FetchStrategy... fetchStrategy);
    ClickTokenReference create(ClickTokenReference clickTokenReference);
    ClickTokenReference update(ClickTokenReference clickTokenReference);
    void delete(ClickTokenReference clickTokenReference);
    void deleteClickTokenReferences(List<ClickTokenReference> list);

    Long countAllClickTokenReferences();
    List<ClickTokenReference> getAllClickTokenReferences(FetchStrategy ... fetchStrategy);
    List<ClickTokenReference> getAllClickTokenReferences(Sorting sort, FetchStrategy ... fetchStrategy);
    List<ClickTokenReference> getAllClickTokenReferences(Pagination page, FetchStrategy ... fetchStrategy);

}
