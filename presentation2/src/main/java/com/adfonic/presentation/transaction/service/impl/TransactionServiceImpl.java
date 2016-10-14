package com.adfonic.presentation.transaction.service.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.AccountDetail_;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.PaymentOptions;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.TransactionNotification;
import com.adfonic.domain.TransactionType;
import com.adfonic.domain.User;
import com.adfonic.dto.account.AccountDto;
import com.adfonic.dto.address.PostalAddressDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CampaignTransactionDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.dto.transactions.PaymentOptionsDto;
import com.adfonic.dto.transactions.PublisherAccountingDto;
import com.adfonic.dto.transactions.TransactionNotificationDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.presentation.transaction.datamodels.TransactionsForAccountLazyDataModel;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.adfonic.util.Range;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.invoicing.service.InvoicingManager;

@Service("transactionService")
public class TransactionServiceImpl extends GenericServiceImpl implements TransactionService {
	
    @Autowired
    private UserManager userManager;
	@Autowired
	private CompanyManager companyManager;
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private InvoicingManager invoicingManager;
	@Autowired
	private AdvertiserManager advertiserManager;
	@Autowired
	private PublisherManager publisherManager;
	@Autowired
	private CampaignManager campaignManager;
	
	@Autowired
	private LocationService locationService;
	
	private static final FetchStrategy ACCOUNT_DETAIL_FS = 
	        new FetchStrategyBuilder()
	            .addInner(AccountDetail_.account)
	            .build();
    //---------------------------------------------------------------------------------------------------------------------

	@Transactional(readOnly=true)
	public AdvertiserAccountingDto getAdvertiserAccountingDtoForUser(UserDTO userDto) {
		if(userDto == null || userDto.getAdvertiserDto() == null) {
			return null;
		}
		Long id = userDto.getAdvertiserDto().getId();
		Advertiser advertiser = advertiserManager.getAdvertiserById(id);
		return this.getObjectDto(AdvertiserAccountingDto.class, advertiser);
	}
	
	@Transactional(readOnly=false)
	public AdvertiserAccountingDto updateAdvertiser(AdvertiserAccountingDto advertiserDto) {
		if(advertiserDto == null) {
			return null;
		}
		Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
		if(advertiser == null) {
			return null;
		}
		advertiser.setDailyBudget(advertiserDto.getDailyBudget());
		advertiser.setNotifyLimit(advertiserDto.getNotifyLimit());
		advertiser.setNotifyAdditionalEmails(advertiserDto.getNotifyAdditionalEmails());
		advertiser = advertiserManager.update(advertiser);
		return this.getObjectDto(AdvertiserAccountingDto.class, advertiser);
	}

    //---------------------------------------------------------------------------------------------------------------------
	
	@Transactional(readOnly=true)
	public CompanyAccountingDto getCompanyById(Long id) {
		Company company = companyManager.getCompanyById(id);
		if(company == null) {
			return null;
		}
		return this.getObjectDto(CompanyAccountingDto.class, company);
	}
	
    @Transactional(readOnly=true)
    public CompanyAccountingDto getCompanyAccountingDtoForUser(UserDTO userDto) {
		if(userDto.getCompany() == null) {
			return null;
		}
		Long id = userDto.getCompany().getId();
		Company company = companyManager.getCompanyById(id);
		return this.getObjectDto(CompanyAccountingDto.class, company);
    }
	
    //---------------------------------------------------------------------------------------------------------------------

    @Transactional(readOnly=true)
	public PublisherAccountingDto getPublisherAccountingDtoForUser(UserDTO userDto) {
        Long id;
		if(userDto.getPublisherDto() == null) {
		    Company company = companyManager.getCompanyById(userDto.getCompany().getId());
		    if(company.getPublisher()==null){
		        return null;
		    }
		    id = company.getPublisher().getId();
		}
		else{
		    id = userDto.getPublisherDto().getId();
		}
		Publisher publisher = publisherManager.getPublisherById(id);
		return this.getObjectDto(PublisherAccountingDto.class, publisher);
	}

	//---------------------------------------------------------------------------------------------------------------------

	@Transactional(readOnly=true)
	public Long countCampaignsForAdvertiser(AdvertiserDto advertiserDto) {
		return this.countCampaignsForAdvertiser(advertiserDto, null);
	}
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto) {
		return this.getCampaignsForAdvertiser(advertiserDto, (Boolean)null);
	}
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Sorting sort) {
		return getCampaignsForAdvertiser(advertiserDto, (Boolean)null, null, null, sort);
	}
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Pagination page) {
		return getCampaignsForAdvertiser(advertiserDto, (Boolean)null, null, page, page.getSorting());
	}

	@Transactional(readOnly=true)
	public Long countCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds) {
		Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
		return campaignManager.countAllCampaigns(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds));
	}

    @Transactional(readOnly=true)
    public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Collection<Campaign.Status> statuses) {
        return getCampaignsForAdvertiser(advertiserDto, houseAds, statuses, null, null);
    }
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds) {
		return getCampaignsForAdvertiser(advertiserDto, houseAds, null, null, null);
	}
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Sorting sort) {
		return getCampaignsForAdvertiser(advertiserDto, houseAds, null, null, sort);
	}
	
	@Transactional(readOnly=true)
	public List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Pagination page) {
		return getCampaignsForAdvertiser(advertiserDto, houseAds, null, page, page.getSorting());
	}

	@Transactional(readOnly=true)
	protected List<CampaignTransactionDto> getCampaignsForAdvertiser(AdvertiserDto advertiserDto, Boolean houseAds, Collection<Campaign.Status> statuses, Pagination page, Sorting sort) {
		Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
		List<Campaign> list = campaignManager.getAllCampaigns(new CampaignFilter().setAdvertiser(advertiser).setHouseAds(houseAds).setStatuses(statuses), page, sort);
		return this.makeListFromCollection(this.getList(CampaignTransactionDto.class, list));
	}

    //---------------------------------------------------------------------------------------------------------------------

	@Transactional(readOnly=true)
	public Long countAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType) {
		Account account = accountManager.getAccountById(accountDto.getId());
		return accountManager.countAllTransactions(
                account,
                range,
                transactionType);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType) {
		return this.getAllTransactions(accountDto, range, transactionType, null, null);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Sorting sort) {
		return this.getAllTransactions(accountDto, range, transactionType, null, sort);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Pagination page) {
		return this.getAllTransactions(accountDto, range, transactionType, page, page.getSorting());
	}
	
	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			TransactionType transactionType,
			Pagination page,
			Sorting sort) {
		
		Account account = accountManager.getAccountById(accountDto.getId());
		List<AccountDetail> list = accountManager.getAllTransactions(
                account,
                range,
                transactionType,
                page,
                sort,
                ACCOUNT_DETAIL_FS);
		return makeListFromCollection(this.getList(AccountDetailDto.class, list));
		
	}

	//---------------------------------------------------------------------------------------------------------------------
	
	@Transactional(readOnly=true)
	public Long countAllTransactions(
			AccountDto accountDto, 
			Range<Date> range) {
		Account account = accountManager.getAccountById(accountDto.getId());
		return accountManager.countAllTransactions(
                account,
                range);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range) {
		return this.getAllTransactions(accountDto, range, (Pagination)null, (Sorting)null);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Sorting sort) {
		return this.getAllTransactions(accountDto, range, (Pagination)null, sort);
	}

	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Pagination page) {
		return this.getAllTransactions(accountDto, range, page, page.getSorting());
	}
	
	@Transactional(readOnly=true)
	public List<AccountDetailDto> getAllTransactions(
			AccountDto accountDto, 
			Range<Date> range, 
			Pagination page,
			Sorting sort) {
		
		Account account = accountManager.getAccountById(accountDto.getId());
		List<AccountDetail> list = accountManager.getAllTransactions(
                account,
                range,
                page,
                sort,
                ACCOUNT_DETAIL_FS);
		return makeListFromCollection(this.getList(AccountDetailDto.class, list));
		
	}
	
	public TransactionsForAccountLazyDataModel createTransactionsForAccountLazyDataModel(AccountDto accountDto, 
			Range<Date> range) {
		return new TransactionsForAccountLazyDataModel(accountDto, range, this);
	}

	//---------------------------------------------------------------------------------------------------------------------

	@Transactional(readOnly=true)
	public BigDecimal getBalanceAsOfDate(AccountDto accountDto, boolean postPay, Date date) {
		Account account = accountManager.getAccountById(accountDto.getId());
		if(account == null) {
			return null;
		}
		return accountManager.getBalanceAsOfDate(account, postPay, date);
	}
	
	//---------------------------------------------------------------------------------------------------------------------

	@Transactional(readOnly=false)
	public AccountDetailDto newAccountDetail(
			AccountDto accountDto, 
			Date transactionTime, 
			BigDecimal amount, 
			BigDecimal tax, 
			TransactionType transactionType, 
			String description, 
			String reference) {
		Account account = accountManager.getAccountById(accountDto.getId());
		AccountDetail accountDetail = accountManager.newAccountDetail(
				account, 
				transactionTime, 
				amount, 
				tax, 
				transactionType, 
				description, 
				reference, null);
		return this.getObjectDto(AccountDetailDto.class, accountDetail);
	}
	
	//---------------------------------------------------------------------------------------------------------------------
	
	@Transactional(readOnly=false)
	public CompanyAccountingDto clearPaymentOptions(CompanyAccountingDto companyDto) {
		Company company = companyManager.getCompanyById(companyDto.getId());
		if(company.getPaymentOptions() != null) {
		    accountManager.delete(company.getPaymentOptions());
			company.setPaymentOptions(null);
			company = companyManager.update(company);
	        return this.getObjectDto(CompanyAccountingDto.class, companyManager.getCompanyById(company.getId()));
		} else {
			return companyDto;
		}
	}

	@Transactional(readOnly=false)
	public CompanyAccountingDto savePaymentOptions(CompanyAccountingDto companyDto) {
		return savePaymentOptions(companyDto, companyDto.getPaymentOptions());
	}

	@Transactional(readOnly=false)
	public CompanyAccountingDto savePaymentOptions(
            CompanyAccountingDto companyDto,
            PaymentOptionsDto paymentOptionsDto) {

		if(paymentOptionsDto == null) {
			return this.clearPaymentOptions(companyDto);
		}
		
		Company company = companyManager.getCompanyById(companyDto.getId());
        PostalAddressDto addressDto = paymentOptionsDto.getPostalAddress();
        if (addressDto != null) {
        	if(addressDto.persisted()) {
        		addressDto = locationService.updatePostalAddress(addressDto);
        	} else {
           		addressDto = locationService.createPostalAddress(addressDto);
        	}
        }
        
		PaymentOptions paymentOptions = null;
		if(paymentOptionsDto.persisted()) {
			paymentOptions = accountManager.getPaymentOptionsById(paymentOptionsDto.getId());
		} else {
			paymentOptions = new PaymentOptions();
		}
        
		paymentOptions.setPaymentAccount(paymentOptionsDto.getPaymentAccount());
		paymentOptions.setPaymentType(paymentOptionsDto.getPaymentType());
		
		if (addressDto != null) {
		    paymentOptions.setPostalAddress(accountManager.getPostalAddressById(addressDto.getId()));
		}
		else {
		    paymentOptions.setPostalAddress(null);
		}
		    
        if(accountManager.isPersisted(paymentOptions)) {
            paymentOptions = accountManager.update(paymentOptions);
        } else {
            paymentOptions = accountManager.create(paymentOptions);
        }
        if (company.getPaymentOptions() == null) {
            company.setPaymentOptions(paymentOptions);
            company = companyManager.update(company);
        }
        return this.getObjectDto(CompanyAccountingDto.class, companyManager.getCompanyById(company.getId()));
    }
	
	//---------------------------------------------------------------------------------------------------------------------
	
	@Transactional(readOnly=false)
	public Map<String,AccountDetailDto> transferFundsAcross(AccountDto advertiserAccountDto, AccountDto publisherAccountDto, BigDecimal amount) {
        Map<String,AccountDetailDto> results = new HashMap<String,AccountDetailDto>();

        Account advertiserAccount = accountManager.getAccountById(advertiserAccountDto.getId());
        Account publisherAccount = accountManager.getAccountById(publisherAccountDto.getId());
        if (advertiserAccount != null && publisherAccount != null && amount != null && amount.compareTo(BigDecimal.ZERO) != 0) {
            Date date = new Date();
            String reference = UUID.randomUUID().toString();

            // credit advertiser account
            AccountDetail advertiserDetail = accountManager.newAccountDetail(
                    advertiserAccount,						//Account account,
                    date,									//Date transactionTime,
                    amount,									// BigDecimal amount,
                    BigDecimal.ZERO,						//BigDecimal tax,
                    TransactionType.FUNDS_ACROSS,			//TransactionType transactionType,
                    "Transfer from publisher account",		//String description,
                    reference,								//String reference,
                    null);									//String opportunity)

            // debit pub account
            AccountDetail publisherDetail = accountManager.newAccountDetail(
                    publisherAccount,						//Account account,
                    date,									//Date transactionTime,
                    amount.negate(),						// BigDecimal amount,
                    BigDecimal.ZERO,						//BigDecimal tax,
                    TransactionType.FUNDS_ACROSS,			//TransactionType transactionType,
                    "Transfer to advertiser account",		//String description,
                    reference,								//String reference,
                    null);									//String opportunity)

            results.put(ADVERTISER_DETAIL, this.getObjectDto(AccountDetailDto.class, advertiserDetail));
            results.put(PUBLISHER_DETAIL, this.getObjectDto(AccountDetailDto.class, publisherDetail));
        }
        return results;
    }
	
    //---------------------------------------------------------------------------------------------------------------------

    @Transactional(readOnly=false)
    public TransactionNotificationDto newTransactionNotification(
            AdvertiserDto advertiserDto, 
            UserDTO userDto, 
            BigDecimal amount,
            String reference) {
        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
        User user = userManager.getUserById(userDto.getId());
        TransactionNotification transactionNotification = invoicingManager.newTransactionNotification(
                advertiser, 
                user,
                amount,
                reference);
        return this.getObjectDto(TransactionNotificationDto.class, transactionNotification);
    }
    
	//---------------------------------------------------------------------------------------------------------------------

}
