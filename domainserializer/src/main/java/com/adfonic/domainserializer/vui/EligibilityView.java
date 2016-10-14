package com.adfonic.domainserializer.vui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCache.ShardMode;
import com.adfonic.domainserializer.DsShard;
import com.adfonic.domainserializer.loader.AdCacheBuildParams;
import com.adfonic.domainserializer.loader.AdserverDomainCacheLoader;
import com.adfonic.domainserializer.web.EligibilityController.DebugEligibilityListener;
import com.adfonic.domainserializer.web.EligibilityController.EligibilityResult;
import com.adfonic.domainserializer.web.InvalidInputException;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.Pair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@UIScope
@SpringView(name = EligibilityView.VIEW_NAME)
public class EligibilityView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "eligibility";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    private AdserverDomainCacheLoader cacheLoader;

    TextField tfCreative = new TextField("Creative");
    TextField tfCampaign = new TextField("Campaign");
    TextField tfPublication = new TextField("Publication");
    TextField tfAdSpace = new TextField("AdSpace");
    Button btSubmit = new Button("Check");
    FormLayout inputForm = new FormLayout(tfCreative, tfCampaign, tfPublication, tfAdSpace, btSubmit);
    Table tblResults = new Table();
    HorizontalLayout layout = new HorizontalLayout(inputForm, tblResults);

    public EligibilityView() {
        tblResults.addContainerProperty("Campaign", Label.class, null);
        tblResults.addContainerProperty("Creative", Label.class, null);
        tblResults.addContainerProperty("Publication", Label.class, null);
        tblResults.addContainerProperty("AdSpace", Label.class, null);
        tblResults.addContainerProperty("Message", String.class, null);
        tblResults.setVisible(false);
        tblResults.setPageLength(0);

        addComponent(DefaultView.buildMenuBar());
        layout.setSpacing(true);
        layout.setMargin(true);
        addComponent(layout);

        btSubmit.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                checkEligibility();
            }
        });
    }

    public void checkEligibility() {
        String creativeSpec = tfCreative.getValue();
        String campaignSpec = tfCampaign.getValue();
        String publicationSpec = tfPublication.getValue();
        String adSpaceSpec = tfAdSpace.getValue();

        Pair<Long, Long> advIds = findAdvSide(creativeSpec, campaignSpec);
        Long creativeId = advIds.first;
        Long campaignId = advIds.second;

        Long[] pubIds = findPubSide(adSpaceSpec, publicationSpec);
        Long adSpaceId = pubIds[0];
        Long publicationId = pubIds[1];
        Long publisherId = pubIds[2];

        DsShard shard = new DsShard("eligibility-check", ShardMode.include, Collections.singleton(publisherId), true);
        AdCacheBuildParams params = new AdCacheBuildParams(shard);
        params.setDebugCreativeId(creativeId);
        params.setDebugCampaignId(campaignId);
        params.setDebugAdSpaceId(adSpaceId);
        params.setDebugPublicationId(publicationId);

        DebugEligibilityListener listener = new DebugEligibilityListener();
        params.setEligibilityListener(listener);

        AdserverDomainCache adserverCache;
        try {
            adserverCache = cacheLoader.loadAdserverDomainCache(params);
        } catch (Exception x) {
            logger.error("Eligibility check failed", x);
            Notification.show("Eligibility check failed: " + x, Notification.Type.ERROR_MESSAGE);
            return;
        }

        displayResults(listener, adserverCache);

    }

    private void displayResults(DebugEligibilityListener listener, AdserverDomainCache adserverCache) {
        Long creativeId;
        Long adSpaceId;
        List<EligibilityResult> results = listener.getResults();
        tblResults.setPageLength(results.size());
        tblResults.setVisible(true);
        tblResults.getContainerDataSource().removeAllItems();
        if (results.isEmpty()) {
            //Notification.show("Wierd but no eligibility results", Notification.Type.WARNING_MESSAGE);
            if (adserverCache.getAllCreatives().length == 0) {
                Notification.show("No Creative selected. Maybe wrong state or missing budget or stoppage", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (adserverCache.getAllAdSpaces().length == 0) {
                Notification.show("No Adspace selected. Maybe wrong state", Notification.Type.WARNING_MESSAGE);
                return;
            }
        } else {

            for (int i = 0; i < results.size(); ++i) {
                EligibilityResult result = results.get(i);
                CreativeDto creative = result.getCreative();
                Label lblCreative = null;
                Label lblCampaign = null;
                if (creative != null) {
                    lblCreative = buildLabel(creative.getId(), creative.getExternalID() + " " + creative.getName());
                    CampaignDto campaign = creative.getCampaign();
                    lblCampaign = buildLabel(campaign.getId(), campaign.getExternalID() + " " + campaign.getName());
                }
                AdSpaceDto adSpace = result.getAdSpace();

                Label lblPublication = null;
                Label lblAdspace = null;
                if (adSpace != null) {
                    lblAdspace = buildLabel(adSpace.getId(), adSpace.getExternalID() + " " + adSpace.getName());
                    PublicationDto publication = adSpace.getPublication();
                    lblPublication = buildLabel(publication.getId(), publication.getExternalID() + " " + publication.getName());
                }
                adSpaceId = adSpace != null ? adSpace.getId() : null;
                String mesage = result.getRejectionReason() != null ? result.getRejectionReason() : "eligible";

                Object[] cells = { lblCampaign, lblCreative, lblPublication, lblAdspace, mesage };
                tblResults.addItem(cells, i);
            }
            tblResults.setPageLength(results.size());
        }
    }

    private Label buildLabel(Object text, String tooltip) {
        Label label = new Label(String.valueOf(text));
        label.setDescription(tooltip);
        return label;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        // nothing...
    }

    private Long tryLong(String string) {
        try {
            return Long.valueOf(string);
        } catch (NumberFormatException nfx) {
            return null;
        }
    }

    private Long[] findPubSide(String adspaceSpec, String publicationSpec) throws InvalidInputException {
        Long adspaceId = null;
        Long publicationId = null;
        String sqlCondition;
        if (StringUtils.isNotBlank(adspaceSpec)) {
            adspaceId = findIfExist("AD_SPACE", adspaceSpec);
            sqlCondition = " asp.ID=" + adspaceId;
        } else if (StringUtils.isNotBlank(publicationSpec)) {
            publicationId = findIfExist("PUBLICATION", publicationSpec);
            sqlCondition = " pbl.ID=" + publicationId;
        } else {
            throw new InvalidInputException("AdSpace or Publication must be specified");
        }
        Long[] retval = new Long[] { adspaceId, publicationId, null };
        String SQL = "SELECT asp.ID, asp.STATUS, pbl.ID, pbl.STATUS, psh.ID FROM AD_SPACE asp JOIN PUBLICATION pbl ON asp.PUBLICATION_ID=pbl.ID JOIN PUBLISHER psh ON pbl.PUBLISHER_ID=psh.ID WHERE "
                + sqlCondition;
        JdbcTemplate template = new JdbcTemplate(toolsDataSource);
        String errorMessage = template.query(SQL, new ResultSetExtractor<String>() {

            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                boolean anyResult = false;
                boolean adspaceStatusOk = false;
                Long adspaceId = null;
                String adspaceStatus = null;
                while (rs.next()) {
                    anyResult = true;
                    adspaceId = rs.getLong(1);
                    adspaceStatus = rs.getString(2);
                    if (!adspaceStatusOk && Arrays.asList("VERIFIED", "UNVERIFIED").contains(adspaceStatus)) {
                        adspaceStatusOk = true;
                    }
                    Long publicationId = rs.getLong(3);
                    String publicationStatus = rs.getString(4);
                    if (!Arrays.asList("ACTIVE").contains(publicationStatus)) {
                        return "Invalid Publication " + publicationId + " state: " + publicationStatus;
                    }
                    Long publisherId = rs.getLong(5);
                    retval[2] = publisherId;
                }
                if (!adspaceStatusOk) {
                    return "Invalid AdSpace " + adspaceId + " state " + adspaceStatus;
                }
                return null;
            }
        });
        if (errorMessage != null) {
            throw new InvalidInputException(errorMessage);
        }
        return retval;
    }

    private Pair<Long, Long> findAdvSide(String creativeSpec, String campaingSpec) throws InvalidInputException {
        Long creativeId = null;
        Long campaignId = null;
        String sqlCondition;
        if (StringUtils.isNotBlank(creativeSpec)) {
            creativeId = findIfExist("CREATIVE", creativeSpec);
            sqlCondition = " cre.ID=" + creativeId;
        } else if (StringUtils.isNotBlank(campaingSpec)) {
            campaignId = findIfExist("CAMPAIGN", campaingSpec);
            sqlCondition = " cam.ID=" + campaignId;
        } else {
            throw new InvalidInputException("Creative or Campaing must be specified");
        }
        String SQL = "SELECT cre.ID, cre.STATUS, cam.ID, cam.STATUS FROM CREATIVE cre JOIN CAMPAIGN cam ON cre.CAMPAIGN_ID=cam.ID WHERE " + sqlCondition;
        JdbcTemplate template = new JdbcTemplate(toolsDataSource);
        String errorMessage = template.query(SQL, new ResultSetExtractor<String>() {

            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                boolean anyResult = false;
                Long creativeId = null;
                String creativeStatus = null;
                boolean creativeStatusOk = false;
                while (rs.next()) {
                    anyResult = true;
                    creativeId = rs.getLong(1);
                    creativeStatus = rs.getString(2);
                    if (!creativeStatusOk && Arrays.asList("ACTIVE").contains(creativeStatus)) {
                        creativeStatusOk = true;
                    }
                    Long campaignId = rs.getLong(3);
                    String campaignStatus = rs.getString(4);
                    if (!Arrays.asList("ACTIVE", "PAUSED").contains(campaignStatus)) {
                        return "Invalid Campaign " + campaignId + " state: " + campaignStatus;
                    }
                }
                if (!creativeStatusOk) {
                    return "Invalid Creative " + creativeId + " state " + creativeStatus;
                }
                return null;
            }
        });

        if (errorMessage != null) {
            throw new InvalidInputException(errorMessage);
        }
        return Pair.of(creativeId, campaignId);
    }

    private Long findIfExist(String table, String specifier, String... allowedStatuses) {
        if (StringUtils.isNotBlank(specifier)) {
            Long id = tryLong(specifier);
            JdbcTemplate template = new JdbcTemplate(toolsDataSource);
            IdAndStatusMapper mapper = new IdAndStatusMapper();
            Object[] idAndStatus = null;
            try {
                if (id != null) {
                    // check if exists
                    idAndStatus = template.queryForObject("SELECT ID, STATUS FROM " + table + " WHERE ID=?", mapper, id);
                } else {
                    // find by external id
                    idAndStatus = template.queryForObject("SELECT ID, STATUS FROM " + table + " WHERE EXTERNAL_ID=?", mapper, specifier);
                }
            } catch (EmptyResultDataAccessException erdax) {
                // ok, not found
            }
            if (idAndStatus == null) {
                throw new InvalidInputException(table + " not found: '" + specifier + "'");
            } else {
                id = (Long) idAndStatus[0];
                String status = (String) idAndStatus[1];
                if (allowedStatuses != null && allowedStatuses.length > 0) {
                    List<String> allowedList = Arrays.asList(allowedStatuses);
                    if (!allowedList.contains(status)) {
                        throw new InvalidInputException(table + " '" + specifier + "' state: " + status + " not in: " + allowedList);
                    }
                }
                return id;
            }
        } else {
            return null;
        }
    }

    private class IdAndStatusMapper implements RowMapper<Object[]> {

        @Override
        public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("ID");
            String status = rs.getString("STATUS");
            return new Object[] { id, status };
        }

    }

}
