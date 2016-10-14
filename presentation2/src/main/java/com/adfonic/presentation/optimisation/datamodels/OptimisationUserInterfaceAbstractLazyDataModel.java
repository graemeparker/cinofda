package com.adfonic.presentation.optimisation.datamodels;

import javax.sql.DataSource;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel;

/**
 * Base class for the data models used in the optimisation interface.
 * 
 * @author pierre
 *
 * @param <T>
 */
public abstract class OptimisationUserInterfaceAbstractLazyDataModel <T> extends AbstractLazyDataModel<T> {
	
	protected NameIdBusinessDto campaign;
	protected AdvertiserDto advertiser;
	protected String dateRange;
	protected boolean breakdownByCreative;
	protected DataSource dataSource;
	
	protected OptimisationUserInterfaceAbstractLazyDataModel(
			DataSource dataSource,
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative) {
		super();
		this.dataSource = dataSource;
		this.campaign = campaign;
		this.advertiser = advertiser;
		this.dateRange = dateRange;
		this.breakdownByCreative = breakdownByCreative;
	}

	/**
	 * The stored procedures require the sort direction passed as a String. 
	 * This method translates SortDirection instances into the proper Strings.
	 * @param sortDirection
	 * @return
	 */
    protected static String getSortDirectionString(SortDirection sortDirection) {
    	if(sortDirection == null || sortDirection == SortDirection.ASC) {
    		return "ASC";
    	} else if(sortDirection == SortDirection.DESC) {
    		return "DESC";
    	} else {
    		return null;
    	}
    }

}
