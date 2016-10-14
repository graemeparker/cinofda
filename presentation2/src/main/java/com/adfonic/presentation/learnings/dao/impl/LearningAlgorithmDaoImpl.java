package com.adfonic.presentation.learnings.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.adfonic.presentation.learnings.dao.LearningAlgorithmDao;
import com.adfonic.presentation.learnings.sql.procedures.IncludeCampaignToLearningAlgorithmStoredProcedure;
import com.adfonic.presentation.learnings.sql.procedures.IncludeAdSpaceToLearningAlgorithmStoredProcedure;
import com.adfonic.presentation.learnings.sql.procedures.RemoveCampaignFromLearningAlgorithmStoredProcedure;
import com.adfonic.presentation.learnings.sql.procedures.RemoveCampaignLearningsStoredProcedure;
import com.adfonic.presentation.learnings.sql.procedures.RemoveAdSpaceFromLearningAlgorithmStoredProcedure;
import com.adfonic.presentation.learnings.sql.procedures.RemoveAdSpaceLearningsStoredProcedure;
import com.adfonic.presentation.util.GenericDaoImpl;

@Repository
public class LearningAlgorithmDaoImpl extends GenericDaoImpl implements LearningAlgorithmDao {
    
    private static final String QUERY_CREATIVE_EXCLUDE_FROM_CALCS = "SELECT count(*) as RESULT FROM creative_exclude_from_calcs WHERE creative_id IN (?)";
    private static final String QUERY_ADSPACE_EXCLUDE_FROM_CALCS  = "SELECT count(*) as RESULT FROM ad_space_exclude_from_calcs WHERE ad_space_id IN (?)";

    @Autowired(required = false)
    @Qualifier("optdbDataSource")
    private DataSource optdbDataSource;

    // ------------------
    // Campaign learnings
    // ------------------
    @Override
    public void includeCampaignToLearningAlgorithm(Long campaignId, Long adfonicUserId){
        new IncludeCampaignToLearningAlgorithmStoredProcedure(optdbDataSource).run(campaignId, adfonicUserId);
    }
    
    @Override
    public void excludeCampaignFromLearningAlgorithm(Long campaignId, Long adfonicUserId){
        new RemoveCampaignFromLearningAlgorithmStoredProcedure(optdbDataSource).run(campaignId, adfonicUserId);
    }
    
    @Override
    public void removeCampaignLearnings(Long campaignId, Long adfonicUserId){
        new RemoveCampaignLearningsStoredProcedure(optdbDataSource).run(campaignId, adfonicUserId);
    }

    @Override
    public Boolean isCampaignAddedToLearningAlgorithm(List<Creative> creatives) {
        Boolean isAdded = true;
        
        if (!CollectionUtils.isEmpty(creatives)){
            
            // Get creative id comma-separated string
            Iterator<Creative> iterator = creatives.iterator();
            StringBuilder creativeIds = new StringBuilder(String.valueOf(iterator.next().getId()));
            while(iterator.hasNext()){
                creativeIds.append(",").append(String.valueOf(iterator.next().getId()));
            }
            
            try(Connection connection = optdbDataSource.getConnection(); 
                PreparedStatement stmt = connection.prepareStatement(QUERY_CREATIVE_EXCLUDE_FROM_CALCS)){
                stmt.setString(1, creativeIds.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1)>0){
                        isAdded = false;
                    }
                }
            }catch(SQLException sqle){
                throw new RuntimeException("", sqle);
            }
        }
        
        return isAdded;
    }
    
    // ---------------------
    // Publication learnings
    // ---------------------
    @Override
    public void includeAdSpaceToLearningAlgorithm(Long publicationId, Long adfonicUserId){
        new IncludeAdSpaceToLearningAlgorithmStoredProcedure(optdbDataSource).run(publicationId, adfonicUserId);
    }
    
    @Override
    public void excludeAdSpaceFromLearningAlgorithm(Long publicationId, Long adfonicUserId){
        new RemoveAdSpaceFromLearningAlgorithmStoredProcedure(optdbDataSource).run(publicationId, adfonicUserId);
    }
    
    @Override
    public void removeAdSpaceLearnings(Long publicationId, Long adfonicUserId){
        new RemoveAdSpaceLearningsStoredProcedure(optdbDataSource).run(publicationId, adfonicUserId);
    }

    @Override
    public Boolean isPublicationAddedToLearningAlgorithm(List<AdSpace> adSpaces) {
        Boolean isAdded = true;
        
        if (!CollectionUtils.isEmpty(adSpaces)){
            
            // Get creative id comma-separated string
            Iterator<AdSpace> iterator = adSpaces.iterator();
            StringBuilder adspacesIds = new StringBuilder(String.valueOf(iterator.next().getId()));
            while(iterator.hasNext()){
                adspacesIds.append(",").append(String.valueOf(iterator.next().getId()));
            }
            
            try(Connection connection = optdbDataSource.getConnection(); 
                PreparedStatement stmt = connection.prepareStatement(QUERY_ADSPACE_EXCLUDE_FROM_CALCS)){
                stmt.setString(1, adspacesIds.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1)>0){
                        isAdded = false;
                    }
                }
            }catch(SQLException sqle){
                throw new RuntimeException("", sqle);
            }
        }
        
        return isAdded;
    }
}
