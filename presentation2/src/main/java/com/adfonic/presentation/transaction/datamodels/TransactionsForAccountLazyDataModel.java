package com.adfonic.presentation.transaction.datamodels;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.TransactionType;
import com.adfonic.dto.account.AccountDto;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.util.Range;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

public class TransactionsForAccountLazyDataModel extends AbstractLazyDataModel<AccountDetailDto> {

	private TransactionService transactionService;
	
	private AccountDto accountDto;
	private Range<Date> range;
	
	private List<AccountDetailDto> data;
	
	public TransactionsForAccountLazyDataModel(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionService transactionService) {
		super();
		this.transactionService = transactionService;
		this.accountDto = accountDto;
		this.range = range;
		
	}

	@Override
	public String getRowKey(AccountDetailDto t) {
		return t.getId().toString();
	}

	@Override
	public AccountDetailDto getRowData(String rowKey) {
		if(!CollectionUtils.isEmpty(data)) {
			for(AccountDetailDto dto : data) {
				if(dto.getId().toString().equals(rowKey)) {
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public List<AccountDetailDto> loadPage(
			int firstRowIndex,
			int pageSize,
			String sortField,
			com.adfonic.presentation.datamodels.AbstractLazyDataModel.SortDirection sortDirection,
			Map<String, String> filters) {
		Pagination page = new Pagination(firstRowIndex, pageSize);
		Sorting sort = null;
		if(!StringUtils.isEmpty(sortField)) {
			if(sortField.equals("transactionType")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "transactionType") :  SortOrder.desc(AccountDetail.class, "transactionType"));
			} else if(sortField.equals("amount")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "amount") :  SortOrder.desc(AccountDetail.class, "amount"));
			} else if(sortField.equals("tax")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "tax") :  SortOrder.desc(AccountDetail.class, "tax"));
			} else if(sortField.equals("total")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "total") :  SortOrder.desc(AccountDetail.class, "total"));
			} else if(sortField.equals("description")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "description") :  SortOrder.desc(AccountDetail.class, "description"));
			} else if(sortField.equals("reference")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "reference") :  SortOrder.desc(AccountDetail.class, "reference"));
			} else if(sortField.equals("transactionTime")) {
				sort = new Sorting(sortDirection.equals(SortDirection.ASC) ? SortOrder.asc(AccountDetail.class, "transactionTime") :  SortOrder.desc(AccountDetail.class, "transactionTime"));
			} 
		}
        TransactionType transactionType = null;
        if(filters != null) {
	        String szTransactionType = filters.get("transactionType");
	        if(!StringUtils.isEmpty(szTransactionType)) {
	        	transactionType = TransactionType.valueOf(szTransactionType);
	        }
        }
        // If the filters changed, update the total count
        if (getCurrentFilters() == null || !getCurrentFilters().equals(filters)) {
        	Long count = transactionService.countAllTransactions(accountDto, range, transactionType);
    		this.setTotalRowCount(count.intValue());    		
    		setCurrentFilters(filters);
        }
        

		data = transactionService.getAllTransactions(accountDto, range, transactionType, page, sort);
		return data;
	}
	
	
}
