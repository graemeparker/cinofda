package com.byyd.middleware.account.service.jpa;

import static com.byyd.middleware.iface.dao.SortOrder.desc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.AccountType;
import com.adfonic.domain.AffiliateProgram;
import com.adfonic.domain.PaymentOptions;
import com.adfonic.domain.PaymentOptions.PaymentType;
import com.adfonic.domain.PostalAddress;
import com.adfonic.domain.TransactionType;
import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.domain.VerificationCode_;
import com.adfonic.util.Range;
import com.byyd.middleware.account.dao.AccountDao;
import com.byyd.middleware.account.dao.AccountDetailDao;
import com.byyd.middleware.account.dao.AffiliateProgramDao;
import com.byyd.middleware.account.dao.PaymentOptionsDao;
import com.byyd.middleware.account.dao.PostalAddressDao;
import com.byyd.middleware.account.dao.VerificationCodeDao;
import com.byyd.middleware.account.filter.AccountDetailFilter;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyJpaImpl;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;

@Service("accountManager")
public class AccountManagerJpaImpl extends BaseJpaManagerImpl implements AccountManager {
    
    @Autowired(required=false)
    private AffiliateProgramDao affiliateProgramDao;
    
    @Autowired(required=false)
    private AccountDao accountDao;
    
    @Autowired(required=false)
    private AccountDetailDao accountDetailDao;
    
    @Autowired(required=false)
    private VerificationCodeDao verificationCodeDao;
    
    @Autowired(required=false)
    private PaymentOptionsDao paymentOptionsDao;
    
    @Autowired(required=false)
    private PostalAddressDao postalAddressDao;

    //------------------------------------------------------------------------------------------
    // AffiliateProgram
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public AffiliateProgram newAffiliateProgram(String name, String affiliateId, BigDecimal depositBonus, FetchStrategy... fetchStrategy) {
        AffiliateProgram af = new AffiliateProgram();
        af.setName(name);
        af.setAffiliateId(affiliateId);
        af.setDepositBonus(depositBonus);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return affiliateProgramDao.create(af);
        } else {
            af = affiliateProgramDao.create(af);
            return getAffiliateProgramById(af.getId(), fetchStrategy);
        }
     }

    @Override
    @Transactional(readOnly=true)
    public AffiliateProgram getAffiliateProgramById(String id, FetchStrategy... fetchStrategy) {
        return getAffiliateProgramById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public AffiliateProgram getAffiliateProgramById(Long id, FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public AffiliateProgram update(AffiliateProgram affiliateProgram) {
        return affiliateProgramDao.update(affiliateProgram);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(AffiliateProgram affiliateProgram) {
        affiliateProgramDao.delete(affiliateProgram);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAffiliatePrograms(List<AffiliateProgram> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(AffiliateProgram entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public AffiliateProgram getAffiliateProgramByAffiliateId(String affiliateId, FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getByAffiliateId(affiliateId, fetchStrategy);
    }

    @Transactional(readOnly=true)
    public AffiliateProgram getAffiliateProgramByName(String name, FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllAffiliatePrograms() {
        return affiliateProgramDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<AffiliateProgram> getAllAffiliatePrograms(FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AffiliateProgram> getAllAffiliatePrograms(Sorting sort, FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AffiliateProgram> getAllAffiliatePrograms(Pagination page, FetchStrategy... fetchStrategy) {
        return affiliateProgramDao.getAll(page, fetchStrategy);
    }
    
  //------------------------------------------------------------------------------------------
    // Account
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public Account newAccount(AccountType accountType, FetchStrategy... fetchStrategy) {
        Account account = new Account(accountType);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(account);
        } else {
            account = create(account);
            return getAccountById(account.getId(), fetchStrategy);
        }
    }


    @Override
    @Transactional(readOnly=true)
    public Account getAccountById(String id, FetchStrategy... fetchStrategy) {
        return getAccountById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Account getAccountById(Long id, FetchStrategy... fetchStrategy) {
        return accountDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public Account create(Account account) {
        return accountDao.create(account);
    }

    @Override
    @Transactional(readOnly=false)
    public Account update(Account account) {
        return accountDao.update(account);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(Account account) {
        accountDao.delete(account);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAccounts(List<Account> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(Account account : list) {
            delete(account);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public Account addToBalance(Account account, BigDecimal amount, FetchStrategy... fetchStrategy) {
        if(account == null || amount == null) {
            return account;
        }
        accountDao.addToBalance(account, amount);
        return accountDao.getById(account.getId(), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public Account subtractFromBalance(Account account, BigDecimal amount, FetchStrategy... fetchStrategy) {
        return addToBalance(account, BigDecimal.ZERO.subtract(amount), fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=false)
    public Double getAccountAdvertisersBalanceForCompany(Long companyId) {
        if(companyId == null || companyId == 0L) {
            return 0.0;
        }
        return accountDao.getAccountAdvertisersBalanceForCompany(companyId);
    }

    //------------------------------------------------------------------------------------------
    // AccountDetail
    //------------------------------------------------------------------------------------------
    // NOTE: in all the newAccountDetail() methods, we do not allow the passing of a FetchStrategy, as to mandate that the Account
    // object set in the AccountDetail always be manually reset to the current state for the balance. Testing a method where a FS would
    // be passed instructing the Account be loaded actually brings in the previous state of the Account object, without the balance update.
    // I dont really get why that is, but this is not the first time we see Hibernate do that.
	@Override
	@Transactional(readOnly = false)
	public AccountDetail newAccountDetail(Account account, Date transactionTime, BigDecimal amount, BigDecimal tax,
			TransactionType transactionType, String description, String reference, String opportunity) {
		AccountDetail detail = new AccountDetail();
		detail.setAccount(account);
		detail.setTransactionTime(transactionTime);
		detail.setAmount(amount);
		detail.setTax(tax);
		if (tax == null) {
			detail.setTotal(amount);
		} else {
			detail.setTotal(amount.add(tax));
		}
		detail.setTransactionType(transactionType);
		if (StringUtils.isNotEmpty(description)) {
			detail.setDescription(description);
		}
		if (StringUtils.isNotEmpty(reference)) {
			detail.setReference(reference);
		}
		if (StringUtils.isNotEmpty(opportunity)) {
			detail.setOpportunity(opportunity);
		}
		detail = create(detail);
		detail.setAccount(getAccountById(account.getId()));
		return detail;
	}

    @Override
    @Transactional(readOnly=true)
    public AccountDetail getAccountDetailById(String id, FetchStrategy... fetchStrategy) {
        return getAccountDetailById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public AccountDetail getAccountDetailById(Long id, FetchStrategy... fetchStrategy) {
        return accountDetailDao.getById(id, fetchStrategy);
    }
    
	@Override
	@Transactional(readOnly = true)
	public AccountDetail getAccountDetailByAccountOrderByTimeDesc(Account account, FetchStrategy... fetchStrategy) {
		List<AccountDetail> accounts = accountDetailDao.getAll(
				new AccountDetailFilter().setAccount(account), new Sorting(desc("transactionTime")), fetchStrategy);
		return (!accounts.isEmpty()) ? accounts.get(0) : null;
	}

    @Transactional(readOnly=false)
    public AccountDetail create(AccountDetail accountDetail) {
        return create(accountDetail, true);
    }

    @Transactional(readOnly=false)
    public AccountDetail create(AccountDetail accountDetail, boolean updateAccount) {
        AccountDetail detail = accountDetailDao.create(accountDetail);
        if(updateAccount) {
            // Using the DAO directly instead of teh service method, because reloading the Account object
            // serves no purpose.
            accountDao.addToBalance(detail.getAccount(), detail.getAmount());
        }
        return detail;
    }

    @Override
    @Transactional(readOnly=false)
    public AccountDetail update(AccountDetail accountDetail) {
        return accountDetailDao.update(accountDetail);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(AccountDetail accountDetail) {
        accountDetailDao.delete(accountDetail);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAccountDetails(List<AccountDetail> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(AccountDetail accountDetail : list) {
            delete(accountDetail);
        }
    }


    protected AccountDetailFilter makeFilter(Account account, Range<Date> dateRange, TransactionType transactionType) {
        AccountDetailFilter filter = new AccountDetailFilter();
        filter.setAccount(account);
        filter.setDateRange(dateRange);
        filter.setTransactionType(transactionType);
        return filter;
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllTransactions(Account account) {
        return countAllTransactions(account, null, null);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, (Range<Date>)null, (TransactionType)null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, (Range<Date>)null, (TransactionType)null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, (Range<Date>)null, (TransactionType)null, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, (Range<Date>)null, (TransactionType)null, page, sort, fetchStrategy);
    }


    @Override
    @Transactional(readOnly=true)
    public Long countAllTransactions(Account account, Range<Date> dateRange) {
        return countAllTransactions(account, dateRange, null);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, dateRange, (TransactionType)null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, dateRange, (TransactionType)null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, dateRange, (TransactionType)null, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllTransactions(account, dateRange, (TransactionType)null, page, sort, fetchStrategy);
    }


    @Override
    @Transactional(readOnly=true)
    public Long countAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType) {
        return accountDetailDao.countAll(makeFilter(account, dateRange, transactionType));
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, FetchStrategy... fetchStrategy) {
        return accountDetailDao.getAll(makeFilter(account, dateRange, transactionType), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Sorting sort, FetchStrategy... fetchStrategy) {
        return accountDetailDao.getAll(makeFilter(account, dateRange, transactionType), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Pagination page, FetchStrategy... fetchStrategy) {
        return accountDetailDao.getAll(makeFilter(account, dateRange, transactionType), page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return accountDetailDao.getAll(makeFilter(account, dateRange, transactionType), page, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public BigDecimal getBalanceAsOfDate(Account account, boolean postPay, Date date) {
        return accountDetailDao.getBalanceAsOfDate(account, postPay, date);
    }

    //------------------------------------------------------------------------------------------
    // VerificationCode
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public VerificationCode newVerificationCode(User user, VerificationCode.CodeType codeType, FetchStrategy... fetchStrategy){
        VerificationCode code = user.newVerificationCode(codeType);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(code);
        } else {
            code = create(code);
            return getVerificationCodeById(code.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public VerificationCode getVerificationCodeById(String id, FetchStrategy... fetchStrategy) {
        return getVerificationCodeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public VerificationCode getVerificationCodeById(Long id, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public VerificationCode create(VerificationCode verificationCode) {
        return verificationCodeDao.create(verificationCode);
    }

    @Override
    @Transactional(readOnly=false)
    public VerificationCode update(VerificationCode verificationCode) {
        return verificationCodeDao.update(verificationCode);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(VerificationCode verificationCode) {
        verificationCodeDao.delete(verificationCode);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteVerificationCodes(List<VerificationCode> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(VerificationCode verificationCode : list) {
            delete(verificationCode);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllVerificationCodesForUser(User user) {
        return verificationCodeDao.countAllForUser(user);
    }

    @Override
    @Transactional(readOnly=true)
    public List<VerificationCode> getAllVerificationCodesForUser(User user, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getAllForUser(user, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<VerificationCode> getAllVerificationCodesForUser(User user, Sorting sort, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getAllForUser(user, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<VerificationCode> getAllVerificationCodesForUser(User user, Pagination page, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getAllForUser(user, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public VerificationCode getVerificationCodeForCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getForCodeTypeAndCodeValue(codeType, codeValue, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public VerificationCode getVerificationCodeForCodeValue(String codeValue, FetchStrategy... fetchStrategy) {
        return verificationCodeDao.getForCodeValue(codeValue, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public User getUserForVerificationCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue) {
        FetchStrategyJpaImpl<VerificationCode> fetchStrategy = new FetchStrategyJpaImpl<VerificationCode>();
        fetchStrategy.addEagerlyLoadedFieldForClass(VerificationCode_.user);
        VerificationCode code = getVerificationCodeForCodeTypeAndCodeValue(codeType, codeValue, fetchStrategy);
        if(code == null) {
            return null;
        }
        return code.getUser();
    }
    
    //------------------------------------------------------------------------------------------
    // PaymentOptions
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public PaymentOptions newPaymentOptions(PaymentType paymentType, String paymentAccount, PostalAddress postalAddress, FetchStrategy... fetchStrategy) {
        PaymentOptions po = new PaymentOptions();
        po.setPaymentAccount(paymentAccount);
        po.setPaymentType(paymentType);
        po.setPostalAddress(postalAddress);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(po);
        } else {
            po = create(po);
            return getPaymentOptionsById(po.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public PaymentOptions getPaymentOptionsById(String id, FetchStrategy... fetchStrategy) {
        return getPaymentOptionsById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PaymentOptions getPaymentOptionsById(Long id, FetchStrategy... fetchStrategy) {
        return paymentOptionsDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PaymentOptions create(PaymentOptions paymentOptions) {
        return paymentOptionsDao.create(paymentOptions);
    }

    @Override
    @Transactional(readOnly=false)
    public PaymentOptions update(PaymentOptions paymentOptions) {
        return paymentOptionsDao.update(paymentOptions);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PaymentOptions paymentOptions) {
        paymentOptionsDao.delete(paymentOptions);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePaymentOptions(List<PaymentOptions> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PaymentOptions paymentOptions : list) {
            delete(paymentOptions);
        }
    }

    //------------------------------------------------------------------------------------------
    // PostalAddress
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=true)
    public PostalAddress getPostalAddressById(String id, FetchStrategy... fetchStrategy) {
        return getPostalAddressById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PostalAddress getPostalAddressById(Long id, FetchStrategy... fetchStrategy) {
        return postalAddressDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public PostalAddress create(PostalAddress postalAddress) {
        return postalAddressDao.create(postalAddress);
    }

    @Override
    @Transactional(readOnly=false)
    public PostalAddress update(PostalAddress postalAddress) {
        return postalAddressDao.update(postalAddress);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PostalAddress postalAddress) {
        postalAddressDao.delete(postalAddress);
    }

    @Override
    @Transactional(readOnly=false)
    public void deletePostalAddress(List<PostalAddress> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(PostalAddress postalAddress : list) {
            delete(postalAddress);
        }
    }
}
