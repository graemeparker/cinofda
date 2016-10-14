package com.byyd.middleware.creative.service.jpa;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.ClickTokenReference;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeTypeMacro;
import com.adfonic.domain.MediaType;
import com.byyd.middleware.creative.dao.ClickTokenReferenceDao;
import com.byyd.middleware.creative.dao.ExtendedCreativeTemplateDao;
import com.byyd.middleware.creative.dao.ExtendedCreativeTypeDao;
import com.byyd.middleware.creative.dao.ExtendedCreativeTypeMacroDao;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.creative.service.ExtendedCreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("extendedCreativeManager")
public class ExtendedCreativeManagerJpaImpl extends BaseJpaManagerImpl implements ExtendedCreativeManager {

    private static final transient Logger LOG = Logger.getLogger(ExtendedCreativeManagerJpaImpl.class.getName());

    @Autowired(required = false)
    private ExtendedCreativeTypeDao extendedCreativeTypeDao;

    @Autowired(required = false)
    ExtendedCreativeTemplateDao extendedCreativeTemplateDao;

    @Autowired(required = false)
    private ExtendedCreativeTypeMacroDao extendedCreativeTypeMacroDao;

    @Autowired(required = false)
    private ClickTokenReferenceDao clickTokenReferenceDao;

    // ------------------------------------------------------------------------------------------
    // ExtendedCreativeType
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeType newExtendedCreativeType(String name, MediaType mediaType, FetchStrategy... fetchStrategy) {
        ExtendedCreativeType ect = new ExtendedCreativeType(name, mediaType);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(ect);
        } else {
            ect = create(ect);
            return getExtendedCreativeTypeById(ect.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeType getExtendedCreativeTypeById(String id, FetchStrategy... fetchStrategy) {
        return this.getExtendedCreativeTypeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeType getExtendedCreativeTypeById(Long id, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public ExtendedCreativeType create(ExtendedCreativeType extendedCreativeType) {
        return extendedCreativeTypeDao.create(extendedCreativeType);
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeType update(ExtendedCreativeType extendedCreativeType) {
        return extendedCreativeTypeDao.update(extendedCreativeType);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ExtendedCreativeType extendedCreativeType) {
        extendedCreativeTypeDao.delete(extendedCreativeType);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteExtendedCreativeTypes(List<ExtendedCreativeType> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ExtendedCreativeType entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllExtendedCreativeTypes() {
        return extendedCreativeTypeDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(Sorting sort, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(Pagination page, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.getAll(page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter) {
        return extendedCreativeTypeDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.findAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.findAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeType> getAllExtendedCreativeTypes(ExtendedCreativeTypeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeDao.findAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // ExtendedCreativeTemplate
    //------------------------------------------------------------------------------------------
    @Override
    public String makeReplacementMacroName(String target) {
        String macroName = StringUtils.remove(target, '%');
        return "${macros." + macroName + "}";
    }

    @Override
    public String getSubstitutionStringForClickTokenReference(ClickTokenReference token) {
        String substitutionString = token.getSubstitutionString();
        String replacement = null;
        if (StringUtils.isEmpty(substitutionString)) {
            replacement = makeReplacementMacroName(token.getToken());
        } else {
            replacement = substitutionString;
        }
        return replacement;
    }

    public String processUrls(String templatePreprocessed, String protocolName, char quote, List<String> exceptions) throws UnsupportedEncodingException {
        LOG.fine("Unprocessed string: " + templatePreprocessed);
        String encodingName = "UTF-8";
        String target = "<a href=" + quote + protocolName + ":";
        LOG.fine("Target: " + target);
        while (true) {
            int index = templatePreprocessed.toLowerCase().indexOf(target);
            if (index == -1) {
                break;
            }
            LOG.fine("Found target at " + index);
            StringBuilder templateBuffer = new StringBuilder();
            templateBuffer.append(templatePreprocessed.substring(0, index));
            templateBuffer.append("<a href=" + quote + ExtendedCreativeTemplate.htmlMediaTypeDynamiTemplateHrefPrependString);
            LOG.fine("templateBuffer: " + templateBuffer.toString());
            StringBuilder urlBuffer = new StringBuilder();
            urlBuffer.append(protocolName + ":");
            index += target.length();
            while (templatePreprocessed.charAt(index) != quote) {
                urlBuffer.append(templatePreprocessed.charAt(index));
                index++;
            }
            index++; // Skip the quote
            LOG.fine("urlBuffer: " + urlBuffer.toString());
            templatePreprocessed = templateBuffer.toString() + URLEncoder.encode(urlBuffer.toString(), encodingName) + quote + templatePreprocessed.substring(index);
            LOG.fine("Processed String after this iteration: " + templatePreprocessed);
        }
        LOG.fine("No more matches for " + target);
        LOG.fine("Reverting encoding of tags");
        for (String exception : exceptions) {
            templatePreprocessed = StringUtils.replace(templatePreprocessed, URLEncoder.encode(exception, encodingName), exception);
        }
        return templatePreprocessed;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public String processExtendedCreativeTemplateContent(ExtendedCreativeTemplate template, Creative creative) {
        // Reload the Creative locally, so we can traverse its relationships
        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
        creative = creativeManager.getCreativeById(creative.getId());

        // First the AdFonic ClickTokens
        String templateOriginal = template.getTemplateOriginal();
        if (StringUtils.isEmpty(templateOriginal)) {
            return templateOriginal;
        }
        String templatePreprocessed = templateOriginal;

        List<String> urlsEscapingExceptions = new ArrayList<>();

        List<ClickTokenReference> tokens = getAllClickTokenReferences();
        if (!CollectionUtils.isEmpty(tokens)) {
            for (ClickTokenReference token : tokens) {
                String target = token.getToken();
                String replacement = getSubstitutionStringForClickTokenReference(token);
                templatePreprocessed = StringUtils.replace(templatePreprocessed, target, replacement);
                urlsEscapingExceptions.add(replacement);
            }
        }

        // Then the 3rd party tags based on ExtendedCreativeType
        ExtendedCreativeType extendedCreativeType = creative.getExtendedCreativeType();
        if (extendedCreativeType != null) {
            for (ExtendedCreativeTypeMacro macro : extendedCreativeType.getMacros()) {
                String matchString = macro.getMatchString();
                String replacementString = macro.getReplacementString();
                templatePreprocessed = StringUtils.replace(templatePreprocessed, matchString, replacementString);
                urlsEscapingExceptions.add(replacementString);
            }

            // Also check for media type HTML and useDynamicTemplates for href replacements
            if (extendedCreativeType.getMediaType() == MediaType.HTML && extendedCreativeType.isUseDynamicTemplates()) {
                try {
                    templatePreprocessed = processUrls(templatePreprocessed, "http", '\'', urlsEscapingExceptions);
                    templatePreprocessed = processUrls(templatePreprocessed, "http", '"', urlsEscapingExceptions);
                    templatePreprocessed = processUrls(templatePreprocessed, "https", '\'', urlsEscapingExceptions);
                    templatePreprocessed = processUrls(templatePreprocessed, "https", '"', urlsEscapingExceptions);
                } catch (UnsupportedEncodingException e) {
                    LOG.severe("Error URL escaping template " + template.getId() + "\n" + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }

        if (extendedCreativeType != null && extendedCreativeType.getMediaType() == MediaType.VAST_XML_2_0) {
            // VAST tracker integration is in adserver
            return templatePreprocessed;
        } else {
            LOG.fine("Classic HTML trackers applied to creative " + creative.getId());
            return templatePreprocessed + ExtendedCreativeTemplate.beaconTemplate;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTemplate newExtendedCreativeTemplate(Creative creative, ContentForm contentForm, String templateOriginal, FetchStrategy... fetchStrategy) {
        ExtendedCreativeTemplate template = new ExtendedCreativeTemplate();
        template.setCreative(creative);
        template.setContentForm(contentForm);
        template.setTemplateOriginal(templateOriginal);
        template.setTemplatePreprocessed(processExtendedCreativeTemplateContent(template, creative));
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(template);
        } else {
            template = create(template);
            return getExtendedCreativeTemplateById(template.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTemplate newExtendedCreativeTemplate(Creative creative, ContentForm contentForm, String templateOriginal, String templatePreprocessed,
            FetchStrategy... fetchStrategy) {
        ExtendedCreativeTemplate template = new ExtendedCreativeTemplate();
        template.setCreative(creative);
        template.setContentForm(contentForm);
        template.setTemplateOriginal(templateOriginal);
        template.setTemplatePreprocessed(templatePreprocessed);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(template);
        } else {
            template = create(template);
            return getExtendedCreativeTemplateById(template.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeTemplate getExtendedCreativeTemplateById(String id, FetchStrategy... fetchStrategy) {
        return getExtendedCreativeTemplateById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeTemplate getExtendedCreativeTemplateById(Long id, FetchStrategy... fetchStrategy) {
        return extendedCreativeTemplateDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTemplate create(ExtendedCreativeTemplate extendedCreativeTemplate) {
        return extendedCreativeTemplateDao.create(extendedCreativeTemplate);
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTemplate update(ExtendedCreativeTemplate extendedCreativeTemplate) {
        extendedCreativeTemplate.setTemplatePreprocessed(processExtendedCreativeTemplateContent(extendedCreativeTemplate, extendedCreativeTemplate.getCreative()));
        return extendedCreativeTemplateDao.update(extendedCreativeTemplate);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ExtendedCreativeTemplate extendedCreativeTemplate) {
        extendedCreativeTemplateDao.delete(extendedCreativeTemplate);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteExtendedCreativeTemplates(List<ExtendedCreativeTemplate> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ExtendedCreativeTemplate entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllExtendedCreativeTemplatesForCreative(Creative creative) {
        return extendedCreativeTemplateDao.countAllForCreative(creative);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, FetchStrategy... fetchStrategy) {
        return extendedCreativeTemplateDao.getAllForCreative(creative, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, Sorting sort, FetchStrategy... fetchStrategy) {
        return extendedCreativeTemplateDao.getAllForCreative(creative, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTemplate> getAllExtendedCreativeTemplatesForCreative(Creative creative, Pagination page, FetchStrategy... fetchStrategy) {
        return extendedCreativeTemplateDao.getAllForCreative(creative, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ContentForm, ExtendedCreativeTemplate> getExtendedCreativeTemplatesMapForCreative(Creative creative, FetchStrategy... fetchStrategy) {
        List<ExtendedCreativeTemplate> list = extendedCreativeTemplateDao.getAllForCreative(creative, fetchStrategy);
        Map<ContentForm, ExtendedCreativeTemplate> map = new HashMap<>();
        for (ExtendedCreativeTemplate template : list) {
            map.put(template.getContentForm(), template);
        }
        return map;
    }

    //------------------------------------------------------------------------------------------
    // ExtendedCreativeTypeMacro
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeTypeMacro getExtendedCreativeTypeMacroById(String id, FetchStrategy... fetchStrategy) {
        return getExtendedCreativeTypeMacroById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedCreativeTypeMacro getExtendedCreativeTypeMacroById(Long id, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeMacroDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTypeMacro create(ExtendedCreativeTypeMacro extendedCreativeTypeMacro) {
        extendedCreativeTypeMacroDao.create(extendedCreativeTypeMacro);
        return extendedCreativeTypeMacro;
    }

    @Override
    @Transactional(readOnly = false)
    public ExtendedCreativeTypeMacro update(ExtendedCreativeTypeMacro extendedCreativeTypeMacro) {
        return extendedCreativeTypeMacroDao.update(extendedCreativeTypeMacro);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ExtendedCreativeTypeMacro extendedCreativeTypeMacro) {
        extendedCreativeTypeMacroDao.delete(extendedCreativeTypeMacro);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteExtendedCreativeTypeMacros(List<ExtendedCreativeTypeMacro> list) {
        for (ExtendedCreativeTypeMacro macro : list) {
            delete(macro);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
        return extendedCreativeTypeMacroDao.countAllForExtendedCreativeType(extendedCreativeType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeMacroDao.getAllForExtendedCreativeType(extendedCreativeType, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeMacroDao.getAllForExtendedCreativeType(extendedCreativeType, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedCreativeTypeMacro> getAllExtendedCreativeTypeMacroForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Pagination page,
            FetchStrategy... fetchStrategy) {
        return extendedCreativeTypeMacroDao.getAllForExtendedCreativeType(extendedCreativeType, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------
    // ClickTokenReference
    // ------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public ClickTokenReference newClickTokenReference(String token, String type, String exampleOutput, String description, FetchStrategy... fetchStrategy) {
        ClickTokenReference ref = new ClickTokenReference();
        ref.setToken(token);
        ref.setType(type);
        ref.setExampleOutput(exampleOutput);
        ref.setDescription(description);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(ref);
        } else {
            ref = create(ref);
            return this.getClickTokenReferenceById(ref.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClickTokenReference getClickTokenReferenceById(String id, FetchStrategy... fetchStrategy) {
        return this.getClickTokenReferenceById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public ClickTokenReference getClickTokenReferenceById(Long id, FetchStrategy... fetchStrategy) {
        return clickTokenReferenceDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public ClickTokenReference create(ClickTokenReference clickTokenReference) {
        return clickTokenReferenceDao.create(clickTokenReference);
    }

    @Override
    @Transactional(readOnly = false)
    public ClickTokenReference update(ClickTokenReference clickTokenReference) {
        return clickTokenReferenceDao.update(clickTokenReference);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ClickTokenReference clickTokenReference) {
        clickTokenReferenceDao.delete(clickTokenReference);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteClickTokenReferences(List<ClickTokenReference> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ClickTokenReference entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllClickTokenReferences() {
        return clickTokenReferenceDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClickTokenReference> getAllClickTokenReferences(FetchStrategy... fetchStrategy) {
        return clickTokenReferenceDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClickTokenReference> getAllClickTokenReferences(Sorting sort, FetchStrategy... fetchStrategy) {
        return clickTokenReferenceDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClickTokenReference> getAllClickTokenReferences(Pagination page, FetchStrategy... fetchStrategy) {
        return clickTokenReferenceDao.getAll(page, fetchStrategy);
    }

    @Transactional(readOnly = true)
    public ClickTokenReference getClickTokenReferenceByName(String name, FetchStrategy... fetchStrategy) {
        return clickTokenReferenceDao.getByName(name, fetchStrategy);
    }
}
