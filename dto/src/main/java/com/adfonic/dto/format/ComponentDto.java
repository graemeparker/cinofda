package com.adfonic.dto.format;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class ComponentDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "formatOrder")
    private int formatOrder;

    @Source(value = "required")
    private boolean required;

    private ContentSpecDto contentSpec;

    public ContentSpecDto getContentSpec() {
        return contentSpec;
    }

    public void setContentSpec(ContentSpecDto contentSpec) {
        this.contentSpec = contentSpec;
    }

    public int getFormatOrder() {
        return formatOrder;
    }

    public void setFormatOrder(int formatOrder) {
        this.formatOrder = formatOrder;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComponentDto [");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
