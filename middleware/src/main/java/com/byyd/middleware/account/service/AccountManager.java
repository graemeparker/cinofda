package com.byyd.middleware.account.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
import com.adfonic.util.Range;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface AccountManager extends BaseManager {

    //------------------------------------------------------------------------------------------
    // AffilateProgram
    //------------------------------------------------------------------------------------------
    AffiliateProgram newAffiliateProgram(String name, String affiliateId, BigDecimal depositBonus, FetchStrategy... fetchStrategy);

    AffiliateProgram getAffiliateProgramById(String id, FetchStrategy... fetchStrategy);
    AffiliateProgram getAffiliateProgramById(Long id, FetchStrategy... fetchStrategy);
    AffiliateProgram update(AffiliateProgram affiliateProgram);
    void delete(AffiliateProgram affiliateProgram);
    void deleteAffiliatePrograms(List<AffiliateProgram> list);

    AffiliateProgram getAffiliateProgramByAffiliateId(String affiliateId, FetchStrategy... fetchStrategy);

    Long countAllAffiliatePrograms();
    List<AffiliateProgram> getAllAffiliatePrograms(FetchStrategy... fetchStrategy);
    List<AffiliateProgram> getAllAffiliatePrograms(Sorting sort, FetchStrategy... fetchStrategy);
    List<AffiliateProgram> getAllAffiliatePrograms(Pagination page, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Account
    //------------------------------------------------------------------------------------------
    Account newAccount(AccountType accountType, FetchStrategy... fetchStrategy);

    Account getAccountById(String id, FetchStrategy... fetchStrategy);
    Account getAccountById(Long id, FetchStrategy... fetchStrategy);
    Account create(Account account);
    Account update(Account account);
    void delete(Account account);
    void deleteAccounts(List<Account> list);
    
    Double getAccountAdvertisersBalanceForCompany(Long companyId);

    Account addToBalance(Account account, BigDecimal amount, FetchStrategy... fetchStrategy);
    Account subtractFromBalance(Account account, BigDecimal amount, FetchStrategy... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // AccountDetail
    //------------------------------------------------------------------------------------------
    AccountDetail newAccountDetail(Account account, Date transactionTime, BigDecimal amount, BigDecimal tax, TransactionType transactionType, String description, String reference, String opportunity);

    AccountDetail getAccountDetailById(String id, FetchStrategy... fetchStrategy);
    AccountDetail getAccountDetailById(Long id, FetchStrategy... fetchStrategy);
    AccountDetail getAccountDetailByAccountOrderByTimeDesc(Account account, FetchStrategy... fetchStrategy);
    AccountDetail update(AccountDetail accountDetail);
    void delete(AccountDetail accountDetail);
    void deleteAccountDetails(List<AccountDetail> list);

    Long countAllTransactions(Account account);
    List<AccountDetail> getAllTransactions(Account account, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Sorting sort, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Pagination page, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
    
    Long countAllTransactions(Account account, Range<Date> dateRange);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Sorting sort, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Pagination page, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);

    Long countAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Sorting sort, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Pagination page, FetchStrategy... fetchStrategy);
    List<AccountDetail> getAllTransactions(Account account, Range<Date> dateRange, TransactionType transactionType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy);
    
    BigDecimal getBalanceAsOfDate(Account account, boolean postPay, Date date);

    //------------------------------------------------------------------------------------------
    // VerificationCode
    //------------------------------------------------------------------------------------------
    VerificationCode newVerificationCode(User user, VerificationCode.CodeType codeType, FetchStrategy... fetchStrategy);

    VerificationCode getVerificationCodeById(String id, FetchStrategy... fetchStrategy);
    VerificationCode getVerificationCodeById(Long id, FetchStrategy... fetchStrategy);
    VerificationCode create(VerificationCode verificationCode);
    VerificationCode update(VerificationCode verificationCode);
    void delete(VerificationCode verificationCode);
    void deleteVerificationCodes(List<VerificationCode> list);

    Long countAllVerificationCodesForUser(User user);
    List<VerificationCode> getAllVerificationCodesForUser(User user, FetchStrategy... fetchStrategy);
    List<VerificationCode> getAllVerificationCodesForUser(User user, Sorting sort, FetchStrategy... fetchStrategy);
    List<VerificationCode> getAllVerificationCodesForUser(User user, Pagination page, FetchStrategy... fetchStrategy);

    VerificationCode getVerificationCodeForCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue, FetchStrategy... fetchStrategy);
    VerificationCode getVerificationCodeForCodeValue(String codeValue, FetchStrategy... fetchStrategy);
    User getUserForVerificationCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue);
    
    //------------------------------------------------------------------------------------------
    // PaymentOptions
    //------------------------------------------------------------------------------------------
    PaymentOptions newPaymentOptions(PaymentType paymentType, String paymentAccount, PostalAddress postalAddress, FetchStrategy... fetchStrategy);

    PaymentOptions getPaymentOptionsById(String id, FetchStrategy... fetchStrategy);
    PaymentOptions getPaymentOptionsById(Long id, FetchStrategy... fetchStrategy);
    PaymentOptions create(PaymentOptions paymentOptions);
    PaymentOptions update(PaymentOptions paymentOptions);
    void delete(PaymentOptions paymentOptions);
    void deletePaymentOptions(List<PaymentOptions> list);

    //------------------------------------------------------------------------------------------
    // PostalAddress
    //------------------------------------------------------------------------------------------
    PostalAddress getPostalAddressById(String id, FetchStrategy... fetchStrategy);
    PostalAddress getPostalAddressById(Long id, FetchStrategy... fetchStrategy);
    PostalAddress create(PostalAddress postalAddress);
    PostalAddress update(PostalAddress postalAddress);
    void delete(PostalAddress postalAddress);
    void deletePostalAddress(List<PostalAddress> list);
}
