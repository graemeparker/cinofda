package com.adfonic.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.SelectEvent;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RemovalInfo;
import com.adfonic.domain.User;
import com.adfonic.presentation.FacesUtils;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@SessionScoped
@ManagedBean
public class AdminPidOptimisationBean extends BaseBean {
	private static final transient Logger LOG = Logger
			.getLogger(AdminPidOptimisationBean.class.getName());

	private static final FetchStrategy MULTI_PURPOSE_FS = new FetchStrategyBuilder()
			.addLeft(Campaign_.creatives)
			.addLeft(Creative_.removedPublications)
			.addInner(Publication_.publisher).build();
	private static final FetchStrategy ADVERTISER_FS = new FetchStrategyBuilder()
			.addLeft(Advertiser_.campaigns).build();

	/** Current advertiser */
	private Advertiser advertiser;

	// access to the currently loaded user being administered
	@ManagedProperty(value = "#{adminAccountBean}")
	private AdminAccountBean adminAccountBean;
	/** Current client to produce report for. */
	private User user;
	protected Campaign campaign;
	protected List<SelectItem> availableCampaigns;
	protected List<SelectItem> companyAdvertisers;

	private String publicationsIds;
	private String search;
	private Map<Campaign, String> labelMap = new HashMap<Campaign, String>();
	private List<Campaign> selectedCampaigns = new ArrayList<Campaign>(0);
	private List<PidOptiTableRow> optiTableList = new ArrayList<PidOptiTableRow>(
			0);
	/** Specific date range for report. */
	protected DateRangeBean dateRangeBean = new DateRangeBean();
	public List<PidOptiTableRow> excludedPubs = new ArrayList<PidOptiTableRow>(
			0);
	private boolean isload = true;
	/**
	 * Super hack to track which columns are sorted which direction true = DSC
	 * false = ASC
	 */
	private Map<String, Boolean> tableSortedDSC = new HashMap<String, Boolean>(0);

	/*
	 * This is mapped via: <action
	 * onPostback="false">#{adminPidOptimisationBean.doInit}</action> so that it
	 * will snag whatever user has been loaded into the adminAccountBean
	 */
	public void doInit() {
		user = null;
		availableCampaigns = new ArrayList<SelectItem>(0);
		selectedCampaigns = new ArrayList<Campaign>(0);
		companyAdvertisers = null;
		search = null;
		publicationsIds = null;
		excludedPubs = new ArrayList<PidOptiTableRow>(0);
		optiTableList = new ArrayList<PidOptiTableRow>(0);
		tableSortedDSC = new HashMap<String, Boolean>(0);
		
		if (adminAccountBean == null || ((user = adminAccountBean.getUser()) == null)) {
			// delegating redirection to the view triggers (adminAccountBean.adminAccountUserCheck)
			LOG.log(Level.FINE, "admin account bean and user must be loaded");
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
			if (!user.getCompany().isAccountType(AccountType.AGENCY)) {
				advertiser = adminAccountBean.getAdvertiser();
			}
		
			if (advertiser != null) {
				// hydrate advertiser with campaigns
				advertiser = getAdvertiserManager().getAdvertiserById(
						advertiser.getId(), ADVERTISER_FS);
				// Reload the available campaigns on page in case they have changed.
				reloadAvailableCampaigns();
			}
		
			if (campaign != null) {
				campaign = getCampaignManager().getCampaignById(campaign.getId(),
						MULTI_PURPOSE_FS);
			}
        }
	}

	public void doLoadRemovedPids() {
		// a campaign should been selected at least.
		// we check for the campaign chosen what is in
		// CAMPAIGN_REMOVED_PUBLICATION_MAP
		isload = false;
		if (!validateSearch(false)) {
			return;
		}
		optiTableList.clear();
		FetchStrategyImpl fsc = new FetchStrategyImpl();
		fsc.addEagerlyLoadedFieldForClass(Campaign.class,
				"removedPublications", JoinType.LEFT);
		fsc.addEagerlyLoadedFieldForClass(Publisher.class, "company",
				JoinType.LEFT);
		fsc.addEagerlyLoadedFieldForClass(Publication.class, "publisher",
				JoinType.LEFT);
		fsc.addEagerlyLoadedFieldForClass(RemovalInfo.class, "removalType",
				JoinType.LEFT);
		Campaign selected = getCampaignManager().getCampaignById(
				getSelectedCampaigns().get(0).getId(), fsc);
		Map<Publication, RemovalInfo> removedPubs = selected
				.getRemovedPublications();

		if (removedPubs != null && removedPubs.size() > 0) {
			// iterate the publications and get the info in it
			Iterator<Publication> itPub = removedPubs.keySet().iterator();
			while (itPub.hasNext()) {
				Publication pub = itPub.next();
				RemovalInfo rinfo = removedPubs.get(pub);
				if (rinfo.getRemovalType().name()
						.equals(RemovalInfo.RemovalType.AD_OPS.name())) {
					// pub inside the map does not have the publisher eagerly
					// retrieved
					Publication eagerlyPub = getPublicationManager()
							.getPublicationById(pub.getId(), fsc);
					PidOptiTableRow pid = new PidOptiTableRow("Removed",
							rinfo.getRemovalTime(), eagerlyPub.getPublisher()
									.getName(), eagerlyPub,
							eagerlyPub.getExternalID(), false, true);
					optiTableList.add(pid);
				}
			}
		}
	}

	public void toggleRow(PidOptiTableRow row) {
		if (excludedPubs.contains(row)) {
			excludedPubs.remove(row);
		} else {
			excludedPubs.add(row);
		}
	}

	public void doLoad() {
		// validations here for a campaign selected!!
		isload = true;
		if (!validateSearch(true)) {
			return;
		}
		optiTableList.clear();
		FetchStrategyImpl fs = new FetchStrategyImpl();
		fs.addEagerlyLoadedFieldForClass(Publication.class, "publisher",
				JoinType.LEFT);
		fs.addEagerlyLoadedFieldForClass(Publisher.class, "company",
				JoinType.LEFT);
		// a campaign should been selected at least.
		// we check for the campaign chosen what is in
		// CAMPAIGN_REMOVED_PUBLICATION_MAP
		FetchStrategyImpl fsc = new FetchStrategyImpl();
		fsc.addEagerlyLoadedFieldForClass(Campaign.class,
				"removedPublications", JoinType.LEFT);

		Campaign selected = getCampaignManager().getCampaignById(
				getSelectedCampaigns().get(0).getId(), fsc);
		Map<Publication, RemovalInfo> removedPubs = selected
				.getRemovedPublications();

		if (!StringUtils.isEmpty(getPublicationsIds())) {
			// check if comes carriage return or comma separeted
			String[] idsToFind = new String[] { "" };
			if (getPublicationsIds().indexOf(",") != -1) {
				idsToFind = getPublicationsIds().split(",");
			} else if (getPublicationsIds().indexOf("\n") != -1) {
				idsToFind = getPublicationsIds().split("\n");
			} else {
				// case where there's only one line
				idsToFind = new String[] { getPublicationsIds().trim() };
			}
			
			if(idsToFind.length>500){
				FacesContext fc = FacesContext.getCurrentInstance();
				fc.addMessage("mainForm:publicationIds",
						messageForId("error.pidoptimisation.pids.morethanexpected"));
				return ;
			}

			// suposse are one per line:
			List<String> list = new ArrayList<String>(0);
			if (idsToFind.length > 0) {
				Map<String,String> temporalMap = new LinkedHashMap<String,String>(0);
				for (int k = 0; k < idsToFind.length; k++) {
					String tmp = idsToFind[k].replace("\r", "");
					//hack to avoid the same external_id repeated over and over;
					temporalMap.put(tmp, tmp);
				}
				if(!temporalMap.isEmpty()){
					list.addAll(temporalMap.keySet());
				}
			}

			List<Publication> publicationList = getPublicationManager()
					.getPublicationByExternalIds(list,
							getDateRangeBean().getStart(),
							getDateRangeBean().getEnd(), fs);

			for (Publication a : publicationList) {
				// check the columns:
				// status
				// temporal.put("status", a.getStatus().name());
				if (a.getStatus() != null && a.getPublisher() != null
						&& a.getExternalID() != null) {
					PidOptiTableRow pid = null;
					if (removedPubs.containsKey(a)) {
						// could have been removed/reactivated
						RemovalInfo tmp = removedPubs.get(a);
						String status = "";
						if (tmp.getRemovalType().name()
								.equals(RemovalInfo.RemovalType.AD_OPS.name())) {
							status = "Removed";
						} else if (tmp
								.getRemovalType()
								.name()
								.equals(RemovalInfo.RemovalType.UNREMOVED
										.name())) {
							status = "Re-activated";
						} else {
							status = "Live";
						}
						pid = new PidOptiTableRow(status, tmp.getRemovalTime(),
								a.getPublisher().getName(), a,
								a.getExternalID(), false, true);
					} else {
						pid = new PidOptiTableRow("Live", null, a
								.getPublisher().getName(), a,
								a.getExternalID(), false, true);
					}
					// check if the pub is in campaign removed pub map.
					optiTableList.add(pid);
				} else {
					PidOptiTableRow pid = new PidOptiTableRow(null, null, null,
							a, a.getExternalID(), false, false);
					optiTableList.add(pid);
				}

			}
			// order publications by externalsids introduced by the user.
			optiTableList = orderToTableByUserInput(optiTableList, list);
			
		}
	}

	private List<PidOptiTableRow> orderToTableByUserInput(
			List<PidOptiTableRow> optiTableList, List<String> pubsIds) {
		// somehow the query result does not get the pubs in the same order as
		// we introduced.
		List<PidOptiTableRow> orderedOne = new ArrayList<PidOptiTableRow>(0);
		for(int j=0;j<pubsIds.size();j++) {
			String publi=pubsIds.get(j); 
			boolean found = false;
			for (int k=0;k<optiTableList.size()&&!found;k++) {
				PidOptiTableRow pid = optiTableList.get(k);
				if (pid != null && pid.getPublicationId().equals(publi)) {
					orderedOne.add(pid);
					found = true;
				}
			}
			if(!found){
				//not found, we add it as not found row.
				PidOptiTableRow tpid = new PidOptiTableRow(null, null, null,
						null, publi, false, false);
				orderedOne.add(tpid);				
			}
		}
		return orderedOne;
	}

	// Autocomplete user query method
	public List<Campaign> doPubQuery(final String search) {
		LOG.info("doPubQuery (final String search) BEGIN");
		List<Campaign> publications = new ArrayList<Campaign>(0);
		publications = getCampaignManager()
				.getAllCampaignsActivePausedForAdvertiser(advertiser, search);
		return publications;
	}

	/**
	 * Reload all available campaigns for this user in case they have changed.
	 */
	protected void reloadAvailableCampaigns() {
		availableCampaigns = FacesUtils.makeSelectItems(
		        getCampaignManager()
						.getAllCampaignsThatHaveEverBeenActiveForAdvertiser(
								advertiser), true);
	}

	public List<SelectItem> getCompanyAdvertisers() {
		return FacesUtils.makeSelectItems(
				getAdvertiserManager().getAllAdvertisersForCompany(
						user.getCompany()), true);
	}

	public void handleSelectedCampaignsId(SelectEvent event) {
		LOG.info("handleSelectedCampaignsId (electEvent event) BEGIN");
		Campaign c = (Campaign) event.getObject();
		selectedCampaigns.clear();
		labelMap.clear();
		selectedCampaigns.add(c);
		labelMap.put(c, c.getName());
	}

	public void doRemoveCampaign(Campaign camp) {
		LOG.info("doRemoveCampaign (Campaign camp) BEGIN");
		if (selectedCampaigns.contains(camp)) {
			selectedCampaigns.remove(camp);
			labelMap.remove(camp);
		}
	}

	public void doRemoveCampaign(String camp) {
		LOG.info("doRemoveCampaign (String camp) BEGIN");
		if (selectedCampaigns.contains(camp)) {
			selectedCampaigns.remove(camp);
			labelMap.remove(camp);
		}

	}

	public void doRemoveCampaign() {
		LOG.info("doRemoveCampaign () BEGIN");
		labelMap.size();

	}

	public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
		this.adminAccountBean = adminAccountBean;
	}

	public String getPublicationsIds() {
		return publicationsIds;
	}

	public void setPublicationsIds(String publicationsIds) {
		this.publicationsIds = publicationsIds;
	}

	public Campaign getCampaign() {
		return this.campaign;
	}

	public List<SelectItem> getAvailableCampaigns() {
		return availableCampaigns;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public Map<Campaign, String> getLabelMap() {
		return labelMap;
	}

	public void setLabelMap(Map<Campaign, String> labelMap) {
		this.labelMap = labelMap;
	}

	public List<Campaign> getSelectedCampaigns() {
		return selectedCampaigns;
	}

	public void setSelectedCampaigns(List<Campaign> selectedCampaigns) {
		this.selectedCampaigns = selectedCampaigns;
	}

	public Advertiser getAdvertiser() {
		return advertiser;
	}

	public void setAdvertiser(Advertiser advertiser) {
		this.advertiser = advertiser;
	}

	public void removePubs() {
		// only do action if the array of excluded pubs is filled and any
		// campaign is chosen
		if (!CollectionUtils.isEmpty(excludedPubs)
				&& !CollectionUtils.isEmpty(selectedCampaigns)) {

			// Only one campaign at a time thought!
			for (Campaign c : selectedCampaigns) {
				for (PidOptiTableRow pid : excludedPubs) {
					// need to fech again the campaign, cause it checks for the
					// publication!
					FetchStrategyImpl fsc = new FetchStrategyImpl();
					fsc.addEagerlyLoadedFieldForClass(Campaign.class,
							"removedPublications", JoinType.LEFT);
					Campaign ct = getCampaignManager().getCampaignById(c.getId(),
							fsc);

					getCampaignManager().removePublicationFromCampaign(ct,
							pid.getPublication(),
							RemovalInfo.RemovalType.AD_OPS);
				}
			}
			// update list
			if (isload) {
				doLoad();
			} else {
				doLoadRemovedPids();
			}
			excludedPubs.clear();
		}
	}

	public void enablePubs() {
		// only do action if the array of excluded pubs is filled and any
		// campaign is chosen
		if (!CollectionUtils.isEmpty(excludedPubs)
				&& !CollectionUtils.isEmpty(selectedCampaigns)) {
			// Only one campaign at a time thought!
			for (Campaign c : selectedCampaigns) {
				for (PidOptiTableRow pid : excludedPubs) {
					FetchStrategyImpl fsc = new FetchStrategyImpl();
					fsc.addEagerlyLoadedFieldForClass(Campaign.class,
							"removedPublications", JoinType.LEFT);
					Campaign ct = getCampaignManager().getCampaignById(c.getId(),
							fsc);

					getCampaignManager().unremovePublicationFromCampaign(ct,
							pid.getPublication());
				}
			}
			if (isload) {
				doLoad();
			} else {
				doLoadRemovedPids();
			}
			excludedPubs.clear();
		}

	}

	private boolean validateSearch(boolean pids) {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (CollectionUtils.isEmpty(getSelectedCampaigns())) {
			fc.addMessage("mainForm:campaign",
					messageForId("error.pidoptimisation.campaign.needed"));
			return false;
		} else if (pids && isload && StringUtils.isEmpty(getPublicationsIds())) {
			fc.addMessage("mainForm:publicationIds",
					messageForId("error.pidoptimisation.pids.needed"));
			return false;
		} else {
			return true;
		}

	}

	public void doSortByStatus() {
		if (tableSortedDSC.get("status") != null
				&& tableSortedDSC.get("status") == true) {
			Collections.sort(optiTableList, new StatusComparator());
			Collections.reverse(optiTableList);
			tableSortedDSC.put("status", false);
		} else {
			Collections.sort(optiTableList, new StatusComparator());
			tableSortedDSC.put("status", true);
		}
	}

	public void doSortByStatusDate() {
		if (tableSortedDSC.get("statusDate") != null
				&& tableSortedDSC.get("statusDate") == true) {
			Collections.sort(optiTableList, new StatusDateComparator());
			Collections.reverse(optiTableList);
			tableSortedDSC.put("statusDate", false);
		} else {
			Collections.sort(optiTableList, new StatusDateComparator());
			tableSortedDSC.put("statusDate", true);
		}
	}

	public void doSortByPublisher() {
		if (tableSortedDSC.get("publisher") != null
				&& tableSortedDSC.get("publisher") == true) {
			Collections.sort(optiTableList, new PublisherComparator());
			Collections.reverse(optiTableList);
			tableSortedDSC.put("publisher", false);
		} else {
			Collections.sort(optiTableList, new PublisherComparator());
			tableSortedDSC.put("publisher", true);
		}
	}

	public void doSortByPublication() {
		if (tableSortedDSC.get("publication") != null
				&& tableSortedDSC.get("publication") == true) {
			Collections.sort(optiTableList, new PublicationComparator());
			Collections.reverse(optiTableList);
			tableSortedDSC.put("publication", false);
		} else {
			Collections.sort(optiTableList, new PublicationComparator());
			tableSortedDSC.put("publication", true);
		}
	}

	public void doSortByPublicationId() {
		if (tableSortedDSC.get("publicationId") != null
				&& tableSortedDSC.get("publicationId") == true) {
			Collections.sort(optiTableList, new PublicationIdComparator());
			Collections.reverse(optiTableList);
			tableSortedDSC.put("publicationId", false);
		} else {
			Collections.sort(optiTableList, new PublicationIdComparator());
			tableSortedDSC.put("publicationId", true);
		}
	}

	private static final class StatusComparator implements Comparator<Object> {

		public int compare(Object rtr1, Object rtr2) {
			String cre1status = "";
			String cre2status = "";
			if( ((PidOptiTableRow) rtr1).getStatus()!=null && ((PidOptiTableRow) rtr2).getStatus()!=null){
				  cre1status = ((PidOptiTableRow) rtr1).getStatus();
				  cre2status = ((PidOptiTableRow) rtr2).getStatus();
				return cre1status.compareTo(cre2status); 
			}else if(((PidOptiTableRow) rtr1).getStatus()!=null && ((PidOptiTableRow) rtr2).getStatus()==null ){
				return 1;
			}else if( ((PidOptiTableRow) rtr1).getStatus()==null &&((PidOptiTableRow) rtr2).getStatus()!=null ){
				return -1;
			}else{
				return 0;
			}
		}
	}

	private static final class StatusDateComparator implements Comparator<Object> {

		public int compare(Object rtr1, Object rtr2) {
			Date cre1status=null;
			Date cre2status=null;
			if( ((PidOptiTableRow) rtr1).getStatusDate()!=null && ((PidOptiTableRow) rtr2).getStatusDate()!=null){
				  cre1status = ((PidOptiTableRow) rtr1).getStatusDate();
				  cre2status = ((PidOptiTableRow) rtr2).getStatusDate();
				return cre1status.compareTo(cre2status);
			}else if ( ((PidOptiTableRow) rtr1).getStatusDate()!=null && ((PidOptiTableRow) rtr2).getStatusDate()==null){
				return 1;
			}else if (((PidOptiTableRow) rtr1).getStatusDate()==null && ((PidOptiTableRow) rtr2).getStatusDate()!=null) {
				return -1;
			}else{
				return 0;
			}
		}
	}

	private static final class PublisherComparator implements Comparator<Object> {

		public int compare(Object rtr1, Object rtr2) {
			String publisher1="";
			String publisher2="";
			
			if( ((PidOptiTableRow) rtr1).getPublisher()!=null && ((PidOptiTableRow) rtr2).getPublisher()!=null){
				  publisher1 = ((PidOptiTableRow) rtr1).getPublisher();
				  publisher2 = ((PidOptiTableRow) rtr2).getPublisher();
				return publisher1.compareTo(publisher2);
			}else if (((PidOptiTableRow) rtr1).getPublisher()!=null && ((PidOptiTableRow) rtr2).getPublisher()==null) {
				return 1;
			}else if (((PidOptiTableRow) rtr1).getPublisher()==null && ((PidOptiTableRow) rtr2).getPublisher()!=null) {
				return -1;
			}else{
				return 0;
			}
		}
	}

	private static final class PublicationComparator implements Comparator<Object> {

		public int compare(Object rtr1, Object rtr2) {
			
			String publication1 = "";
			String publication2 = "";
			if( ((PidOptiTableRow) rtr1).getPublication()!=null && ((PidOptiTableRow) rtr1).getPublication().getName()!=null
				&& ((PidOptiTableRow) rtr2).getPublication()!=null && ((PidOptiTableRow) rtr2).getPublication().getName()!=null	)
			{
				  publication1 = ((PidOptiTableRow) rtr1).getPublication()
						.getName();
				
				  publication2 = ((PidOptiTableRow) rtr2).getPublication()
						.getName();
				  return publication1.compareTo(publication2);
			}else if (((PidOptiTableRow) rtr1).getPublication()!=null && ((PidOptiTableRow) rtr1).getPublication().getName()!=null 
					  && (((PidOptiTableRow) rtr2).getPublication()==null || ((PidOptiTableRow) rtr2).getPublication().getName()==null || "".equals(((PidOptiTableRow) rtr2).getPublication().getName()) ) ) {
				return 1;
			}else if (((PidOptiTableRow) rtr2).getPublication()!=null && ((PidOptiTableRow) rtr2).getPublication().getName()!=null 
					  && (((PidOptiTableRow) rtr1).getPublication()==null || ((PidOptiTableRow) rtr1).getPublication().getName()==null || "".equals(((PidOptiTableRow) rtr1).getPublication().getName()) ) ){
				return -1;
			}else{
				return 0;
			}
		}
	}

	private static final class PublicationIdComparator implements Comparator<Object> {

		public int compare(Object rtr1, Object rtr2) {
			String publication1="";
			String publication2="";
			if(((PidOptiTableRow) rtr1).getPublicationId()!=null && ((PidOptiTableRow) rtr2).getPublicationId()!=null){
				  publication1 = ((PidOptiTableRow) rtr1).getPublicationId();
				  publication2 = ((PidOptiTableRow) rtr2).getPublicationId();
				return publication1.compareTo(publication2);
			}else if (((PidOptiTableRow) rtr1).getPublicationId()!=null && ((PidOptiTableRow) rtr2).getPublicationId()==null) {
				return 1;
			}else if (((PidOptiTableRow) rtr1).getPublicationId()==null && ((PidOptiTableRow) rtr2).getPublicationId()!=null) {
				return -1;
			}else{
				return 0;
			}

		}
	}

	public static final class PidOptiTableRow {
		/*
		 * Status Status date Publisher Publication Impressions Clicks CTR
		 * Publication ID checked
		 */

		private String status;
		private Date statusDate;
		private String publisher;
		private Publication publication;
		private String publicationId;
		private boolean checked;
		private boolean exists;

		/**
		 * Constructor for removed publications data rows
		 * 
		 * @param publication
		 * @param creative
		 * @param impressions
		 * @param clicks
		 * @param ctr
		 * @param conversions
		 * @param cpc
		 * @param spend
		 * @param removedType
		 * @param dateRemoved
		 * @param checked
		 */
		private PidOptiTableRow(String status, Date statusDate,
				String publisher, Publication publication,
				String publicationId, boolean checked, boolean exists) {
			this.status = status;
			this.statusDate = statusDate;
			this.publisher = publisher;
			this.publication = publication;
			this.publicationId = publicationId;
			this.checked = checked;
			this.exists = exists;
		}

		private PidOptiTableRow() {

		}

		public Publication getPublication() {
			return publication;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Date getStatusDate() {
			return statusDate;
		}

		public void setStatusDate(Date statusDate) {
			this.statusDate = statusDate;
		}

		public String getPublisher() {
			return publisher;
		}

		public void setPublisher(String publisher) {
			this.publisher = publisher;
		}

		public String getPublicationId() {
			return publicationId;
		}

		public void setPublicationId(String publicationId) {
			this.publicationId = publicationId;
		}

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}

		public void setPublication(Publication publication) {
			this.publication = publication;
		}

		public boolean isExists() {
			return exists;
		}

		public void setExists(boolean exists) {
			this.exists = exists;
		}

	}

	public List<PidOptiTableRow> getOptiTableList() {
		return optiTableList;
	}

	public void setOptiTableList(List<PidOptiTableRow> optiTableList) {
		this.optiTableList = optiTableList;
	}

	public DateRangeBean getDateRangeBean() {
		return dateRangeBean;
	}

	public void setDateRangeBean(DateRangeBean dateRangeBean) {
		this.dateRangeBean = dateRangeBean;
	}

	public List<PidOptiTableRow> getExcludedPubs() {
		return excludedPubs;
	}

	public void setExcludedPubs(List<PidOptiTableRow> excludedPubs) {
		this.excludedPubs = excludedPubs;
	}

	public boolean isIsload() {
		return isload;
	}

	public void setIsload(boolean isload) {
		this.isload = isload;
	}

}
