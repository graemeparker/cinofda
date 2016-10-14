package com.adfonic.presentation.transaction.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.TransactionType;
import com.adfonic.dto.account.AccountDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CampaignTransactionDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.dto.transactions.PaymentOptionsDto;
import com.adfonic.dto.transactions.PublisherAccountingDto;
import com.adfonic.dto.transactions.TransactionNotificationDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.transaction.datamodels.TransactionsForAccountLazyDataModel;
import com.adfonic.util.Range;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface TransactionService {
	
	//-------------------------------------------------------------------------------------
	
	CompanyAccountingDto getCompanyById(Long id);
	
	CompanyAccountingDto getCompanyAccountingDtoForUser(UserDTO user);

	CompanyAccountingDto clearPaymentOptions(CompanyAccountingDto companyDto);
	CompanyAccountingDto savePaymentOptions(CompanyAccountingDto companyDto);
	CompanyAccountingDto savePaymentOptions(CompanyAccountingDto companyDto, PaymentOptionsDto paymentOptionsDto);
	
	//-------------------------------------------------------------------------------------

	PublisherAccountingDto getPublisherAccountingDtoForUser(UserDTO userDto);

	//-------------------------------------------------------------------------------------

	AdvertiserAccountingDto getAdvertiserAccountingDtoForUser(UserDTO userDto);
	AdvertiserAccountingDto updateAdvertiser(AdvertiserAccountingDto advertiserDto);
	
	//-------------------------------------------------------------------------------------

	Long countCampaignsForAdvertiser(AdvertiserDto advertiserDto);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Sorting sort);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Pagination page);
	
	Long countCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Sorting sort);
	List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Pagination page);	

    List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Collection<Campaign.Status> statuses);
	
	//-------------------------------------------------------------------------------------
	
	Long countAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Sorting sort);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Pagination page);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Pagination page,
			Sorting sort);
	
	Long countAllTransactions(
			AccountDto accountDto, 
			Range<Date> range);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Sorting sort);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Pagination page);
	List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Pagination page,
			Sorting sort);
	
	TransactionsForAccountLazyDataModel createTransactionsForAccountLazyDataModel(AccountDto accountDto, Range<Date> range);
	
	AccountDetailDto newAccountDetail(
			AccountDto accountDto, 
			Date transactionTime, 
			BigDecimal amount, 
			BigDecimal tax, 
			TransactionType transactionType, 
			String description, 
			String reference);
	
	//-------------------------------------------------------------------------------------

	BigDecimal getBalanceAsOfDate(AccountDto accountDto, boolean postPay, Date date);
	
	//-------------------------------------------------------------------------------------

	final static String ADVERTISER_DETAIL = "advertiserDetail";
    final static String PUBLISHER_DETAIL = "publisherDetail";
    
    Map<String,AccountDetailDto> transferFundsAcross(AccountDto advertiserAccountDto, AccountDto publisherAccountDto, BigDecimal amount);

    //-------------------------------------------------------------------------------------
    
    TransactionNotificationDto newTransactionNotification(AdvertiserDto advertiser, UserDTO user, BigDecimal amount, String reference);
}
