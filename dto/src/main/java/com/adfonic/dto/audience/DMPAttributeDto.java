package com.adfonic.dto.audience;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

/**
 * NOTE: the List<DMPSelectorDto> collection is not @DTOCascade'ed by design. It
 * will be initialized by the service according to a requested sorting spec and
 * excluding hidden elements.
 *
 * @author pierre
 *
 */
public class DMPAttributeDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    public enum DMPAttributeSortBy {
        NAME, DISPLAY_ORDER
    }

    @DTOTransient
    private List<DMPSelectorDto> dmpSelectors;

    @Source("displayOrder")
    private Integer displayOrder;

    public DMPAttributeDto(){
        this.dmpSelectors = new ArrayList<DMPSelectorDto>();
    }

    public List<DMPSelectorDto> getDMPSelectors() {
        if (dmpSelectors == null) {
            dmpSelectors = new ArrayList<DMPSelectorDto>();
        }
        return dmpSelectors;
    }

    public void setDMPSelectors(List<DMPSelectorDto> dmpSelectors) {
        this.dmpSelectors = dmpSelectors;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

}
