package com.adfonic.presentation.optimisation.datamodels;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.optimisation.OptimisationUserInterfaceRemovedPublicationDto;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceRemovedPublicationDetailByCreativeCountStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceRemovedPublicationDetailPaginatedStoredProcedure;

/**
 * Data model returning live publications according to the query parameters passed from the presentation layer
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceRemovedPublicationLazyDataModel extends OptimisationUserInterfaceAbstractLazyDataModel<OptimisationUserInterfaceRemovedPublicationDto> {

    private static Logger LOGGER = LoggerFactory.getLogger(OptimisationUserInterfaceRemovedPublicationLazyDataModel.class);

	protected List<OptimisationUserInterfaceRemovedPublicationDto> data;

	public OptimisationUserInterfaceRemovedPublicationLazyDataModel(
			DataSource dataSource,
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative) {
		super(dataSource, campaign, advertiser, dateRange, breakdownByCreative);
		
		Long count = null;
		if(breakdownByCreative) {
			OptimisationUserInterfaceRemovedPublicationDetailByCreativeCountStoredProcedure proc = new OptimisationUserInterfaceRemovedPublicationDetailByCreativeCountStoredProcedure(this.dataSource);
			count = proc.run(this.advertiser.getId(), this.campaign.getId(), this.dateRange);
		} else {
			OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure proc = new OptimisationUserInterfaceRemovedPublicationDetailCountStoredProcedure(this.dataSource);
			count = proc.run(this.advertiser.getId(), this.campaign.getId(), this.dateRange);
		}
		LOGGER.debug("Count: " + count);
		this.setTotalRowCount(count.intValue());
	}

	public List<OptimisationUserInterfaceRemovedPublicationDto> getList(int startPage,
			int recordsPerPage, 
			String sortColumn, 
			String sortDirection,
			Map<String, String> filters) {
		List<OptimisationUserInterfaceRemovedPublicationDto> list = null;
		if(breakdownByCreative) {
			OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure proc = new OptimisationUserInterfaceRemovedPublicationDetailByCreativePaginatedStoredProcedure(this.dataSource);
			list = proc.run(
					this.advertiser.getId(), 
					this.campaign.getId(), 
					this.dateRange, 
					sortColumn,
					sortDirection,
					recordsPerPage,
					startPage);
		} else {
			OptimisationUserInterfaceRemovedPublicationDetailPaginatedStoredProcedure proc = new OptimisationUserInterfaceRemovedPublicationDetailPaginatedStoredProcedure(this.dataSource);
			list = proc.run(
					this.advertiser.getId(), 
					this.campaign.getId(), 
					this.dateRange, 
					sortColumn,
					sortDirection,
					recordsPerPage,
					startPage);
		}
		LOGGER.debug("List size: " + list.size());
		return list;
	}
	
	public List<OptimisationUserInterfaceRemovedPublicationDto> loadPage(
			int firstRowIndex,
			int pageSize, 
			String sortField, 
			SortDirection sortDirection,
			Map<String, String> filters) {
	    
	    // note that the control has limitations for sortBy and sortOrder so we define the default column here
        if (sortField == null) {
            sortField = "impressions";
        }
	    
		LOGGER.info("loadPage() - firstRowIndex: " + firstRowIndex + " - pageSize: " + pageSize + " - sortField: " + sortField + " - sortDirection: " + sortDirection);
		// Pagination as passed follows:
		// first: 0-based index of the first record to be returned
		// pageSize: the number of records to return
		// The stored procedure requires the pagination to be passed as 1-based page index and page size,
		// so the page index has to be calculated as shown below.
		data = getList(
				(firstRowIndex / pageSize) + 1,
				pageSize, 
				sortField, 
				getSortDirectionString(sortDirection),
				filters);
		
		return data;
	}

	@Override
    public String getRowKey(OptimisationUserInterfaceRemovedPublicationDto dto) {
        return dto.getId();
    }

	@Override
    public OptimisationUserInterfaceRemovedPublicationDto getRowData(String rowKey) {
    	for(OptimisationUserInterfaceRemovedPublicationDto dto : data) {
    		if(dto.getId().equals(rowKey)) {
    			return dto;
    		}
    	}
    	return null;
    }

	
}
