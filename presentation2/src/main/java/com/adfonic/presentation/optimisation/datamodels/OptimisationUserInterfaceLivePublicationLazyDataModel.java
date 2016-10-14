package com.adfonic.presentation.optimisation.datamodels;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.optimisation.OptimisationUserInterfaceLivePublicationDto;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceLivePublicationDetailByCreativeCountStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceLivePublicationDetailByCreativePaginatedStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceLivePublicationDetailCountStoredProcedure;
import com.adfonic.presentation.optimisation.sql.procedures.OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure;

/**
 * Data model returning live publications according to the query parameters passed from the presentation layer
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceLivePublicationLazyDataModel extends OptimisationUserInterfaceAbstractLazyDataModel<OptimisationUserInterfaceLivePublicationDto> {

    private static Logger LOGGER = LoggerFactory.getLogger(OptimisationUserInterfaceLivePublicationLazyDataModel.class);

	protected List<OptimisationUserInterfaceLivePublicationDto> data;

	public OptimisationUserInterfaceLivePublicationLazyDataModel(
			DataSource dataSource,
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative) {
		super(dataSource, campaign, advertiser, dateRange, breakdownByCreative);
		
		Long count = null;
		if(breakdownByCreative) {
			OptimisationUserInterfaceLivePublicationDetailByCreativeCountStoredProcedure proc = new OptimisationUserInterfaceLivePublicationDetailByCreativeCountStoredProcedure(this.dataSource);
			count = proc.run(this.advertiser.getId(), this.campaign.getId(), this.dateRange);
		} else {
			OptimisationUserInterfaceLivePublicationDetailCountStoredProcedure proc = new OptimisationUserInterfaceLivePublicationDetailCountStoredProcedure(this.dataSource);
			count = proc.run(this.advertiser.getId(), this.campaign.getId(), this.dateRange);
		}
		LOGGER.debug("Count: " + count);
		this.setTotalRowCount(count.intValue());
	}

	public List<OptimisationUserInterfaceLivePublicationDto> getList(int startPage,
			int recordsPerPage, 
			String sortColumn, 
			String sortDirection,
			Map<String, String> filters) {
		List<OptimisationUserInterfaceLivePublicationDto> list = null;
		if(breakdownByCreative) {
			OptimisationUserInterfaceLivePublicationDetailByCreativePaginatedStoredProcedure proc = new OptimisationUserInterfaceLivePublicationDetailByCreativePaginatedStoredProcedure(this.dataSource);
			list = proc.run(
					this.advertiser.getId(), 
					this.campaign.getId(), 
					this.dateRange, 
					sortColumn,
					sortDirection,
					recordsPerPage,
					startPage);
		} else {
			OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure proc = new OptimisationUserInterfaceLivePublicationDetailPaginatedStoredProcedure(this.dataSource);
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
	
	@Override
	public List<OptimisationUserInterfaceLivePublicationDto> loadPage(
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
    public String getRowKey(OptimisationUserInterfaceLivePublicationDto dto) {
        return dto.getId();
    }

	@Override
    public OptimisationUserInterfaceLivePublicationDto getRowData(String rowKey) {
    	for(OptimisationUserInterfaceLivePublicationDto dto : data) {
    		if(dto.getId().equals(rowKey)) {
    			return dto;
    		}
    	}
    	return null;
    }

	
}
