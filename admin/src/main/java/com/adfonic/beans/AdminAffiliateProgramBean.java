package com.adfonic.beans;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

//import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.adfonic.domain.AffiliateProgram;
import com.adfonic.domain.AffiliateProgram_;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.UploadedContent;
import com.adfonic.util.MediaUtils;
import com.adfonic.util.MediaUtils.ImageInfo;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;


@SessionScoped
@ManagedBean
public class AdminAffiliateProgramBean extends BaseBean {
    public static final String SUMMARY_VIEW = "affiliateProgramSummary";
    public static final String DETAIL_VIEW = "affiliateProgram";

    private static final FetchStrategy AFFILIATE_PROGRAM_FS = new FetchStrategyBuilder()
        .addLeft(AffiliateProgram_.logo)
        .build();
    
    // the list of all programs
    private Collection<AffiliateProgram> affiliatePrograms;
    private boolean editMode = false;

    // the program being edited
    private AffiliateProgram affiliateProgram;
    private UploadedFile upload;

    private Boolean deleteLogo;

    // allowed types for logo
    private static List<String> contentTypes = Arrays.asList(new String[]{"PNG","JPEG","GIF"});

    private UploadedContent logo;

    @PostConstruct
    public void init() {
        if(isRestrictedUser()){
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }
    }
    
    public String doCancel() {
        affiliateProgram = null;
        setEditMode(false);
        return SUMMARY_VIEW;
    }

    public String doEdit() {
        if (!editMode) {
            this.affiliateProgram = new AffiliateProgram();
        }
        return DETAIL_VIEW;
    }
 
    public void handleFileUpload(FileUploadEvent event) {  
    	
    	FacesContext fc = FacesContext.getCurrentInstance();
    	upload = event.getFile();
    	if (upload != null) {
            byte[] uploadData = null;
            String uploadCT = null;
            try {
                uploadData = upload.getContents();
                uploadCT = upload.getContentType();
            } catch (Exception e) {
                fc.addMessage(null, new FacesMessage("upload error"));
                return;
            }
            logger.fine("upload size: " + uploadData.length);

            // check file size, no spec, just a sanity check
            int maxBytes = 300000;

            if (uploadData.length > maxBytes) {
                // File size too large
                fc.addMessage(null, new FacesMessage("upload error"));
                return;
            }

            ImageInfo imageInfo = null;
            try {
                imageInfo = MediaUtils.getImageInfo(uploadData);
            }
            catch (java.io.IOException e) {
                fc.addMessage(null,
                        new FacesMessage("upload error"));
                return;
            }
            if (imageInfo.getWidth() > 356 ||
                    imageInfo.getHeight() > 75) {
                fc.addMessage(null,
                        new FacesMessage("height/width exceeded"));
                return;
            }
            // File fits constraints

            // Hack around IE6 by translating its strange MIME types
            if ("image/pjpeg".equals(uploadCT)) {
                uploadCT = "image/jpeg";
            } else if ("image/x-png".equals(uploadCT)) {
                uploadCT = "image/png";
            }

            logger.fine("looking up content type: " + uploadCT);

            ContentType ct = getCommonManager().getContentTypeForMimeType(uploadCT, imageInfo.isAnimated());

            if (ct == null) {
                fc.addMessage(null, new FacesMessage("upload error"));
                return;
            }

            if (!contentTypes.contains(ct.getName())) {
                // Incompatible content type
                fc.addMessage(null, new FacesMessage("upload error"));
                return;
            }

            logo = new UploadedContent(ct);
            logo.setData(uploadData);
            
            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");  
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }     
    }
    
	public String doSave() {
        // Save changes

        // save the affiliate program
        // We really should check for null, but unfortunately, the ID is declared as a primitive type.
        if(affiliateProgram.getId() <= 0) {
            affiliateProgram = getAccountManager().newAffiliateProgram(affiliateProgram.getName(), affiliateProgram.getAffiliateId(), affiliateProgram.getDepositBonus());
        }

        if (deleteLogo != null && deleteLogo.equals(Boolean.TRUE)) {
            UploadedContent currentLogo = affiliateProgram.getLogo();
            if (currentLogo.getId() > 0) {
                getCommonManager().delete(currentLogo);
                affiliateProgram.setLogo(null);
            }
        }
        else if (logo != null) {
            if (logo.getId() <= 0) {
                getCommonManager().create(logo);
                affiliateProgram.setLogo(logo);
            }
        }
        affiliateProgram = getAccountManager().update(affiliateProgram);

        deleteLogo = null;
        affiliateProgram = null;
        logo = null;
        affiliatePrograms = null;
        setRequestFlag("didUpdate");

        return SUMMARY_VIEW;
    }

    public void doDelete() {
        if (affiliateProgram != null) {
            getAccountManager().delete(affiliateProgram);
            affiliateProgram = null;
            affiliatePrograms = null;
        }
    }

    public void setAffiliateProgram(AffiliateProgram affiliateProgram) {
        this.affiliateProgram = affiliateProgram;
    }

    public AffiliateProgram getAffiliateProgram() {
        return this.affiliateProgram;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    public UploadedFile getUpload(){
        return upload;
    }

    public void setUpload(UploadedFile upload){
        this.upload = upload;
    }

    public Collection<AffiliateProgram> getAffiliatePrograms() {
    	if (this.affiliatePrograms == null) {
    		this.affiliatePrograms = getAccountManager().getAllAffiliatePrograms(new Sorting(SortOrder.asc("name")), AFFILIATE_PROGRAM_FS);
    	}
        return this.affiliatePrograms;
    }

    public void setDeleteLogo(Boolean deleteLogo) {
        this.deleteLogo = deleteLogo;
    }

    public Boolean getDeleteLogo() {
        return deleteLogo;
    }
}
