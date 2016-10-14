package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.Source;

import com.adfonic.domain.ContentForm;
import com.adfonic.dto.BusinessKeyDTO;

public class ExtendedCreativeTemplateDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "contentForm")
    private ContentForm contentForm;

    @Source(value = "templateOriginal")
    private String templateOriginal;

    @Source(value = "templatePreprocessed")
    private String templatePreprocessed;

    public ContentForm getContentForm() {
        return contentForm;
    }

    public void setContentForm(ContentForm contentForm) {
        this.contentForm = contentForm;
    }

    public String getTemplateOriginal() {
        return templateOriginal;
    }

    public void setTemplateOriginal(String templateOriginal) {
        this.templateOriginal = templateOriginal;
    }

    public String getTemplatePreprocessed() {
        return templatePreprocessed;
    }

    public void setTemplatePreprocessed(String templatePreprocessed) {
        this.templatePreprocessed = templatePreprocessed;
    }
}
