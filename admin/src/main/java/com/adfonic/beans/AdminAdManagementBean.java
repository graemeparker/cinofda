package com.adfonic.beans;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortOrder;

import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.presentation.FacesUtils;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@SessionScoped
@ManagedBean(name="adminAdManagement")
public class AdminAdManagementBean extends BaseBean {
	
	private static final transient Logger LOG = Logger.getLogger(AdminAdManagementBean.class.getName());

    protected Publication publication;
    protected List<Publication> activePublications;
    protected List<SelectItem> availablePublications;
    protected String selectedQueue;
    protected String currentList;
    private String rowDisplay;

    private Creative[] selectedCreatives;

    private Publisher publisher;
    private PendingDataModel pendingDataModel;
    private ApprovedDataModel approvedDataModel;
    private DeniedDataModel deniedDataModel;

    private static final FetchStrategy PUBLICATION_FS = new FetchStrategyBuilder()
        .addInner(Publication_.publisher)
        .build();

    private static final FetchStrategy MULTI_PURPOSE_FS = new FetchStrategyBuilder()
        .addInner(Campaign_.advertiser)
        .addLeft(Campaign_.currentBid)
        .addLeft(Creative_.campaign)
        .addLeft(Creative_.segment)
        .addLeft(Creative_.assetBundleMap)
        .addLeft(AssetBundle_.assetMap)
        .build();

    public AdminAdManagementBean() {
        doInit();
    }

    public void doInit() {
        FacesContext fc = FacesContext.getCurrentInstance();
        
        AdminAccountBean adminAccountBean =
	            (AdminAccountBean)fc.getApplication().evaluateExpressionGet(fc, "#{adminAccountBean}", AdminAccountBean.class);
        publisher = adminAccountBean.getPublisher();
        if (publisher == null) {
        	// delegating redirection to the view triggers (adminAccountBean.adminAccountUserCheck)
        	LOG.log(Level.FINE, "Publisher is null");
        }else if(isRestrictedUser()){
        	// delegating redirection to the view triggers (adminAccountBean.adminAccountUserCheck)
        	LOG.log(Level.INFO, "Restricted user access");
        	try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }else{
		        activePublications =
		                getPublicationManager().getActivePublicationsToDateForPublisher(publisher);
		        availablePublications = FacesUtils.makeSelectItems(activePublications, true);
		        publication = null;
		        selectedQueue = null;
		        approvedDataModel = null;
		        pendingDataModel = null;
		        deniedDataModel = null;
		        rowDisplay = null;
	    }
    }

    public List<SelectItem> getAvailablePublications() {
        return availablePublications;
    }

    /*
     * update the publication from ui selection, reset the queue menu
     */
    public void publicationChanged(ValueChangeEvent event) {

        Publication newPublication = (Publication)event.getNewValue();
        if (newPublication != null) {
            boolean changed = this.publication != newPublication;
            if (changed) {
                setPublication(newPublication);
                // hydrate
                this.publication = getPublicationManager().getPublicationById(publication.getId(), PUBLICATION_FS);
                approvedDataModel = null;
                deniedDataModel = null;
                pendingDataModel = null;
                rowDisplay = null;
            }
        }
        // model value will not update for the selected queue
        PhaseId phaseId = event.getPhaseId();

        if (phaseId.equals(PhaseId.ANY_PHASE)) {
            event.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
            event.queue();
        }
        // correct phase update local variables
        else if (phaseId.equals(PhaseId.UPDATE_MODEL_VALUES)) {
            setSelectedQueue(null);
        }
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public List<SelectItem> getAvailableQueues() {
        List<SelectItem> availableQueues = new LinkedList<SelectItem>();
        if (!publication.isAutoApproval()) {
            availableQueues.add(new SelectItem("Pending"));
        }
        availableQueues.add(new SelectItem("Approved"));
        availableQueues.add(new SelectItem("Rejected"));
        return availableQueues;
    }

    public Creative[] getSelectedCreatives() {
        return selectedCreatives;
    }

    public void setSelectedCreatives(Creative[] selectedCreatives) {
        this.selectedCreatives = selectedCreatives;
    }

    public String getRowDisplay() {
		return rowDisplay;
	}

	public void setRowDisplay(String rowDisplay) {
		this.rowDisplay = rowDisplay;
	}

	public void doApprove() {
        publication = getPublicationManager().getPublicationById(publication.getId(), PUBLICATION_FS);

        try {
            for (Creative sc : selectedCreatives) {
                publication = getCreativeManager().approveCreativeForPublication(publication, sc);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE,
                       "Failed to approve creatives",
                       e);
        }
        selectedCreatives = null;
        approvedDataModel = null;
        deniedDataModel = null;
        pendingDataModel = null;
        rowDisplay = null;
    }
    public void doReject() {
        publication = getPublicationManager().getPublicationById(publication.getId(), PUBLICATION_FS);

        try {
            for (Creative sc : selectedCreatives) {
                publication = getCreativeManager().denyCreativeForPublication(publication, sc);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE,
                       "Failed to reject creatives",
                       e);
        }
        selectedCreatives = null;
        approvedDataModel = null;
        deniedDataModel = null;
        pendingDataModel = null;
        rowDisplay = null;
    }

    public String getSelectedQueue() {
        return selectedQueue;
    }

    public void setSelectedQueue(String selectedQueue) {
        if (selectedQueue == null) {
            // default to approved for auto, pending for non-auto
            if (publication != null) {
                if (publication.isAutoApproval()) {
                    selectedQueue = "Approved";
                }
                else {
                    selectedQueue = "Pending";
                }
            }
        }
        this.selectedQueue = selectedQueue;
    }
    
    private String doGetRowDisplay(int numOfRows){
    	StringBuilder displayString = new StringBuilder();
    	displayString.append("10");
    	
    	if(numOfRows >= 20){
    		displayString.append(",20");
    	}
    	if(numOfRows >= 50){
    		displayString.append(",50");
    	}
    	if(numOfRows >= 100){
    		displayString.append(",100");
    	}
    	
    	displayString.append("," + String.valueOf(numOfRows));
    	
    	return displayString.toString();
    }

    public LazyDataModel<Creative> getAdDataModel() {
        if (selectedQueue == null){
            return null;
        }
        if (selectedQueue.equals("Pending")){
            if (pendingDataModel == null){
            	pendingDataModel = new PendingDataModel();
            	setRowDisplay(doGetRowDisplay(pendingDataModel.getRowCount()));   
            }
            return pendingDataModel;
        }
        if (selectedQueue.equals("Approved")){
            if (approvedDataModel == null){
            	approvedDataModel = new ApprovedDataModel();
            	setRowDisplay(doGetRowDisplay(approvedDataModel.getRowCount()));
            }
            return approvedDataModel;    
        }
        if (selectedQueue.equals("Rejected")){
            if (deniedDataModel == null){
            	deniedDataModel = new DeniedDataModel();
            	setRowDisplay(doGetRowDisplay(deniedDataModel.getRowCount()));   
            }
            return deniedDataModel;
        }else{
            return null;
        }

    }

    public class ApprovedDataModel extends LazyDataModel<Creative> implements SelectableDataModel<Creative>{

		private static final long serialVersionUID = 1L;

		public ApprovedDataModel() {
            this.setRowCount(getCreativeManager().countApprovedCreativesForPublication(publication).intValue());
        }

        @Override
        public Creative getRowData(String externalId){
            return getCreativeManager().getCreativeByExternalId(externalId, MULTI_PURPOSE_FS);
        }

        @Override
        public Object getRowKey(Creative creative) {
            return creative.getExternalID();
        }

        @Override
        public void setRowIndex(int rowIndex) {
            if (rowIndex == -1 || getPageSize() == 0) {
                super.setRowIndex(-1);
            }else{
                super.setRowIndex(rowIndex % getPageSize());
            }
        }

        @Override
        public List<Creative> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {

            this.setRowCount(getCreativeManager().countApprovedCreativesForPublication(publication).intValue());

            //paginate
            if(getRowCount() > pageSize){
                return getCreativeManager().getApprovedCreativesForPublication(
                        publication,
                        new Pagination(first, pageSize),
                        MULTI_PURPOSE_FS);
            }else{
                return getCreativeManager().getApprovedCreativesForPublication(
                        publication,
                        MULTI_PURPOSE_FS);
            }
        }
    }

    public class PendingDataModel extends LazyDataModel<Creative> implements SelectableDataModel<Creative>{
    	
    	private static final long serialVersionUID = 1L;
    	
        public PendingDataModel() {
            this.setRowCount(getCreativeManager().countPendingCreativesForPublication(publication).intValue());
        }

        @Override
        public Creative getRowData(String externalId){
            return getCreativeManager().getCreativeByExternalId(externalId, MULTI_PURPOSE_FS);
        }

        @Override
        public Object getRowKey(Creative creative) {
            return creative.getExternalID();
        }

        @Override
        public void setRowIndex(int rowIndex) {
            if (rowIndex == -1 || getPageSize() == 0) {
                super.setRowIndex(-1);
            }else{
                super.setRowIndex(rowIndex % getPageSize());
            }
        }

        @Override
        public List<Creative> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {

            //rowCount
            this.setRowCount(getCreativeManager().countPendingCreativesForPublication(publication).intValue());

            //paginate
            if(getRowCount() > pageSize){
                return getCreativeManager().getPendingCreativesForPublication(
                        publication,
                        new Pagination(first, pageSize),
                        MULTI_PURPOSE_FS);
            }else{
                return getCreativeManager().getPendingCreativesForPublication(
                        publication,
                        MULTI_PURPOSE_FS);
            }
        }
    }

    public class DeniedDataModel extends LazyDataModel<Creative> implements SelectableDataModel<Creative>{

    	private static final long serialVersionUID = 1L;

        public DeniedDataModel() {
            this.setRowCount(getCreativeManager().countDeniedCreativesForPublication(publication).intValue());
        }

        @Override
        public Creative getRowData(String externalId){
            return getCreativeManager().getCreativeByExternalId(externalId, MULTI_PURPOSE_FS);
        }

        @Override
        public Object getRowKey(Creative creative) {
            return creative.getExternalID();
        }

        @Override
        public void setRowIndex(int rowIndex) {
            if (rowIndex == -1 || getPageSize() == 0) {
                super.setRowIndex(-1);
            }else{
                super.setRowIndex(rowIndex % getPageSize());
            }
        }

        @Override
        public List<Creative> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {

            //rowCount
            this.setRowCount(getCreativeManager().countDeniedCreativesForPublication(publication).intValue());

            //paginate
            if(getRowCount() > pageSize){
                return getCreativeManager().getDeniedCreativesForPublication(
                        publication,
                        new Pagination(first, pageSize),
                        MULTI_PURPOSE_FS);
            }else{
                return getCreativeManager().getDeniedCreativesForPublication(
                        publication,
                        MULTI_PURPOSE_FS);
            }
        }
    }
}
