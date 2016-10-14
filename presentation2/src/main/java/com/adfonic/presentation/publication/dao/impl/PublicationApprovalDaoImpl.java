package com.adfonic.presentation.publication.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.publication.dao.PublicationApprovalDao;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationApprovalModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchModel;
import com.adfonic.presentation.publication.model.PublicationApprovalSearchResultModel;
import com.adfonic.presentation.publication.model.PublicationHistoryModel;
import com.adfonic.presentation.publication.sql.procedure.PublicationAccountTypesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationAdOpStatusStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationAlgorithmStatusesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationApprovalDetailStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationApprovalSearchStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationAssignedToUsersStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationDeadzoneStatusesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationExcludedCategoriesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationHistoriesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationTypesStoredProcedure;
import com.adfonic.presentation.publication.sql.procedure.PublicationWatchersStoredProcedure;
import com.adfonic.presentation.util.GenericDaoImpl;
import com.adfonic.presentation.util.Utils;
import com.byyd.middleware.iface.dao.SortOrder;

/**
 * DAO class for Publication Approvals Admin page.
 */
@Repository
public class PublicationApprovalDaoImpl extends GenericDaoImpl implements PublicationApprovalDao {

    private static final transient Logger LOG = Logger.getLogger(PublicationApprovalDaoImpl.class.getName());

    @Autowired(required = false)
    @Qualifier("readOnlyDataSourceForPublicationApproval")
    private DataSource dataSource;

    // Publication Approval Dash board

    @Override
    @SuppressWarnings("unchecked")
    public PublicationApprovalSearchResultModel searchForPublicationApprovals(PublicationApprovalSearchModel dto) {

        Object[] params = {
            dto.getInternalId(), dto.getName(), dto.getFriendlyName(), dto.getSupplierName(), dto.getSupplierUserName(), dto.getExternalId(), dto.getType(), dto.getStatus(),
            dto.getAssignedTo(), dto.getAccountType(), dto.getRtbId(), dto.getSellerNetworkId(), dto.getBundle(), dto.getAlgorithmStatus(), dto.getDeadZoneStatus(),
            dto.getPageSize(), dto.getFirst(), dto.getSortFieldIndex(), (dto.getAscending()) ? SortOrder.Direction.ASC.name() : SortOrder.Direction.DESC.name()
        };

        PublicationApprovalSearchStoredProcedure procedure = new PublicationApprovalSearchStoredProcedure(dataSource, "proc_return_publication_search");
        String randomId = "#" + new Random().nextInt(100);
        Utils.logWithTitle(LOG, Level.INFO, "Publication search proc call" + " - " + randomId, procCallWithOutParam(procedure.getSql(), params, ", @out_record_count);"));
        
        BigDecimal recordCount = BigDecimal.ONE.negate();
        List<PublicationApprovalModel> result = new ArrayList<PublicationApprovalModel>();
        Map<String, Object> data;
        try {
            data = procedure.execute(params);
            result = ((List<PublicationApprovalModel>) data.get("result"));
            Utils.logWithTitle(LOG, Level.FINE, "Record count", (result == null) ? null : Integer.valueOf(result.size()));

            recordCount = (BigDecimal) data.get("out_record_count");
            Utils.logWithTitle(LOG, Level.INFO, "Output param 'out_record_count'" + " - " + randomId, recordCount);
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }
        
        return new PublicationApprovalSearchResultModel(result, recordCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> searchForPublicationTypes() {

        PublicationTypesStoredProcedure procedure = new PublicationTypesStoredProcedure(dataSource, "proc_return_publication_types");
        Utils.logWithTitle(LOG, Level.FINE, "Publication types proc call", procCallNoParam(procedure.getSql()));
        
        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute();
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> searchForPublicationAssignedToUsers(Long publicationId) {

        PublicationAssignedToUsersStoredProcedure procedure = new PublicationAssignedToUsersStoredProcedure(dataSource, "proc_return_assigned_to_users");
        Utils.logWithTitle(LOG, Level.FINE, "Publication assigned to users proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> searchForPublicationAccountTypes() {

        PublicationAccountTypesStoredProcedure procedure = new PublicationAccountTypesStoredProcedure(dataSource, "proc_return_account_types");
        Utils.logWithTitle(LOG, Level.FINE, "Publication account types proc call", procCallNoParam(procedure.getSql()));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute();
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> searchForPublicationAlgorithmStatuses() {

        PublicationAlgorithmStatusesStoredProcedure procedure = new PublicationAlgorithmStatusesStoredProcedure(dataSource, "proc_return_publication_algo_statuses");
        Utils.logWithTitle(LOG, Level.FINE, "Publication algorithm statuses proc call", procCallNoParam(procedure.getSql()));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute();
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> searchForPublicationDeadzoneStatuses() {

        PublicationDeadzoneStatusesStoredProcedure procedure = new PublicationDeadzoneStatusesStoredProcedure(dataSource, "proc_return_publication_deadzone_statuses");
        Utils.logWithTitle(LOG, Level.FINE, "Publication deadzone statuses proc call", procCallNoParam(procedure.getSql()));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute();
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    // Publication Approval Detail

    @Override
    @SuppressWarnings("unchecked")
    public List<PublicationHistoryModel> searchForPublicationHistories(Long publicationId) {

        PublicationHistoriesStoredProcedure procedure = new PublicationHistoriesStoredProcedure(dataSource, "proc_return_publication_history");
        Utils.logWithTitle(LOG, Level.FINE, "Publication histories proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        List<PublicationHistoryModel> result = Collections.<PublicationHistoryModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (List<PublicationHistoryModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    public PublicationApprovalDetailModel getPublicationApprovalDetail(Long publicationId) {

        PublicationApprovalDetailStoredProcedure procedure = new PublicationApprovalDetailStoredProcedure(dataSource, "proc_return_publication_detail");
        Utils.logWithTitle(LOG, Level.FINE, "Publication approval detail proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        PublicationApprovalDetailModel result = new PublicationApprovalDetailModel();
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (PublicationApprovalDetailModel) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Publication Detail", result);
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> getPublicationWatchers(Long publicationId) {

        PublicationWatchersStoredProcedure procedure = new PublicationWatchersStoredProcedure(dataSource, "proc_return_publication_watchers");
        Utils.logWithTitle(LOG, Level.FINE, "Publication watchers proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NameIdModel> getPublicationExcludedCategories(Long publicationId) {

        PublicationExcludedCategoriesStoredProcedure procedure = new PublicationExcludedCategoriesStoredProcedure(dataSource, "proc_return_publication_excluded_categories");
        Utils.logWithTitle(LOG, Level.FINE, "Publication excluded categories proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        List<NameIdModel> result = Collections.<NameIdModel>emptyList();
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (List<NameIdModel>) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }

    @Override
    public String getPublicationAdOpsStatus(Long publicationId) {
        PublicationAdOpStatusStoredProcedure procedure = new PublicationAdOpStatusStoredProcedure(dataSource, "proc_return_publication_ad_ops_status");
        Utils.logWithTitle(LOG, Level.FINE, "Publication AdOpsStatus proc call", procCallWithOneParam(procedure.getSql(), publicationId));

        String result = StringUtils.EMPTY;
        Map<String, Object> data;
        try {
            data = procedure.execute(publicationId);
            result = (String) data.get("result");
            Utils.logWithTitle(LOG, Level.FINE, "AdOpsStatus", result);
        } catch (DataAccessException dae) {
            Utils.logWithTitle(LOG, Level.SEVERE, "Error during proc call", dae.getMessage());
        }

        return result;
    }
}
