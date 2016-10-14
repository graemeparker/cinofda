package com.adfonic.beans.vendor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.CellEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.beans.AdminGeneralException;
import com.adfonic.beans.BaseBean;
import com.adfonic.dto.audience.DMPAttributeDto;
import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.presentation.audience.service.AudienceService;

@Component
@Scope("view")
public class ThirdPartySegmentsMBean extends BaseBean {
    
    @Autowired
    private AudienceService audienceService;
    
    // MBean properties
    private Map<String, DMPVendorDto> dmpVendors= new HashMap<String, DMPVendorDto>();
    private String selectedDmpVendorName = "";
    private List<EditableDMPSelector> editableDMPSelectors = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Check if user has Superadmin role
        if (isRestrictedUser()) {
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex) {
                throw new AdminGeneralException("Internal error");
            }
        }
        
        // Load vendor list
        List<DMPVendorDto> dmpVendorsList = audienceService.getAllDMPVendors();
        for(DMPVendorDto dmpVendorDto : dmpVendorsList){
            this.dmpVendors.put(dmpVendorDto.getName(), dmpVendorDto);
        }
        this.selectedDmpVendorName = "";
    }
    
    // UI Actions / Listeners
    public void onDmpVendorChange(){
        if(this.selectedDmpVendorName!=null){
            // Load dmp segments for vendor
            DMPVendorDto dmpVendor = this.dmpVendors.get(this.selectedDmpVendorName);
            List<DMPAttributeDto> dmpAttributes = audienceService.getDMPAttributesForDMPVendor(dmpVendor, true);
            this.editableDMPSelectors.clear();
            for (DMPAttributeDto dmpAttribute : dmpAttributes){
                for (DMPSelectorDto dmpSelectorDto : dmpAttribute.getDMPSelectors()){
                    this.editableDMPSelectors.add(new EditableDMPSelector(dmpSelectorDto.getId(),
                                                                          dmpSelectorDto.getExternalID(),
                                                                          dmpAttribute.getName(), 
                                                                          dmpSelectorDto.getName(), 
                                                                          dmpSelectorDto.getDataWholesale(), 
                                                                          dmpSelectorDto.getDataRetail(), 
                                                                          dmpSelectorDto.getHidden()));
                            
                }
            }
        }
    }
    
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        // Process only if the input has changed
        if(newValue != null && !newValue.equals(oldValue)) {
            // Identify the changed row
            int editedRow = event.getRowIndex();
            EditableDMPSelector editableDMPSelector = this.editableDMPSelectors.get(editedRow);
            DMPSelectorDto dmpSelectorDto = audienceService.getDMPSelectorById(editableDMPSelector.getSelectorId());
            
            String cellEditorId = event.getColumn().getCellEditor().getId();
            switch (cellEditorId) {
                case "selectorNameEditor":
                    dmpSelectorDto.setName(editableDMPSelector.getSelectorName());
                    break;
                case "selectorDataWholesaleEditor":
                    if (editableDMPSelector.getDataWholesale().compareTo(new BigDecimal(0)) >= 0){
                        dmpSelectorDto.setDataWholesale(editableDMPSelector.getDataWholesale());
                    }else{
                        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "CPM to Byyd price must be greater than 0");
                        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                        return;
                    }
                    break;
                case "selectorDataRetailEditor":
                    if (editableDMPSelector.getDataRetail().compareTo(new BigDecimal(0)) >= 0){
                        dmpSelectorDto.setDataRetail(editableDMPSelector.getDataRetail());
                    }else{
                        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "CPM to client price must be greater than 0");
                        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                        return;
                    }
                    break;
                case "selectorHiddenEditor":
                    dmpSelectorDto.setHidden(editableDMPSelector.getHidden());
                    break;
                default:
                    break;
            }
            // Persist information
            audienceService.updateDMPSelector(dmpSelectorDto);
        }
    }

    // Getters & Setters
    public String getSelectedDmpVendorName() {
        return selectedDmpVendorName;
    }

    public void setSelectedDmpVendorName(String selectedDmpVendorName) {
        this.selectedDmpVendorName = selectedDmpVendorName;
    }

    public List<String> getDmpVendorsNames() {
        List<String> vendorNames = null;
        if (this.dmpVendors!=null){
            vendorNames = new ArrayList<String>(this.dmpVendors.keySet());
            Collections.sort(vendorNames);
        }
        return vendorNames;
    }
    
    public List<EditableDMPSelector> getEditableDMPSelectors() {
        return editableDMPSelectors;
    }
     
    public class EditableDMPSelector{
        private Long selectorId;
        private String externalId;
        private String attributeName;
        private String selectorName;
        private BigDecimal dataWholesale;
        private BigDecimal dataRetail;
        private Boolean hidden;
        
        public EditableDMPSelector(Long selectorId, String externalId, String attributeName, String selectorName, BigDecimal dataWholesale, BigDecimal dataRetail, Boolean hidden) {
            super();
            this.selectorId = selectorId;
            this.externalId = externalId;
            this.attributeName = attributeName;
            this.selectorName = selectorName;
            this.dataWholesale = dataWholesale;
            this.dataRetail = dataRetail;
            this.hidden = hidden;
        }

        public Long getSelectorId() {
            return selectorId;
        }

        public String getExternalId() {
            return externalId;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getSelectorName() {
            return selectorName;
        }

        public void setSelectorName(String selectorName) {
            this.selectorName = selectorName;
        }

        public BigDecimal getDataWholesale() {
            return dataWholesale;
        }

        public void setDataWholesale(BigDecimal dataWholesale) {
            this.dataWholesale = dataWholesale;
        }

        public BigDecimal getDataRetail() {
            return dataRetail;
        }

        public void setDataRetail(BigDecimal dataRetail) {
            this.dataRetail = dataRetail;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }
    } 
}
