package com.byyd.middleware.account.service;

import static com.byyd.middleware.iface.dao.SortOrder.desc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.AccountType;
import com.adfonic.domain.AffiliateProgram;
import com.adfonic.domain.Country;
import com.adfonic.domain.PaymentOptions;
import com.adfonic.domain.PaymentOptions.PaymentType;
import com.adfonic.domain.PostalAddress;
import com.adfonic.domain.TransactionType;
import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.domain.VerificationCode.CodeType;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;
import com.adfonic.util.TimeZoneUtils;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.Sorting;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class AccountManagerIT {
    
    @Autowired
    private AccountManager accountManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private CommonManager commonManager;
    

    //----------------------------------------------------------------------------------------------------------------
    
    @Test
    public void TestAffiliateProgram() {
        String name = "Testing" + System.currentTimeMillis();
        String affiliateId = "ID" + System.currentTimeMillis();
        String description = "Description" + System.currentTimeMillis();
        Double depositBonus = 0.5;
        AffiliateProgram affiliateProgram = null;
        try {
            affiliateProgram = accountManager.newAffiliateProgram(name, affiliateId, BigDecimal.valueOf(depositBonus));
            long id = affiliateProgram.getId();
            assertTrue(id > 0L);

            assertEquals(name, affiliateProgram.getName());
            assertEquals(affiliateId, affiliateProgram.getAffiliateId());
            assertEquals(depositBonus, new Double(affiliateProgram.getDepositBonus().doubleValue()));

            assertEquals(affiliateProgram, accountManager.getAffiliateProgramById(id));
            assertEquals(affiliateProgram, accountManager.getAffiliateProgramById(Long.toString(id)));
            assertEquals(affiliateProgram, accountManager.getAffiliateProgramByAffiliateId(affiliateId));

            String newName = name + " Changed";
            affiliateProgram.setName(newName);
            affiliateProgram.setDescription(description);
            affiliateProgram = accountManager.update(affiliateProgram);
            assertEquals(newName, affiliateProgram.getName());
            assertEquals(description, affiliateProgram.getDescription());

        } catch(Exception e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
        } finally {
            accountManager.delete(affiliateProgram);
            assertNull(accountManager.getAffiliateProgramById(affiliateProgram.getId()));
        }

    }
    
    @Test
    public void testNewAffiliateProgram() {
        String apTest = "test-affiliate-program-" +  System.currentTimeMillis();
        AffiliateProgram affiliateProgram = new AffiliateProgram();
        affiliateProgram.setName(apTest);
        affiliateProgram.setAffiliateId(apTest);
        affiliateProgram.setDepositBonus(new BigDecimal("1.1"));
        affiliateProgram = accountManager.newAffiliateProgram(apTest, apTest, new BigDecimal("1.1"));
        assertNotNull(affiliateProgram.getId());

        assertNotNull(accountManager.getAllAffiliatePrograms());
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAccountByIdWithInvalidId() {
        assertNull(accountManager.getAccountById(0L));
    }

    @Test
    public void testAccount() {
       Account account = null;
       double balance = 1000.00;
       double reducedAmount = 500;
       try {
           account = accountManager.newAccount(AccountType.ADVERTISER);
           assertNotNull(account);
           long id = account.getId();
           assertTrue(id > 0L);

           account = accountManager.getAccountById(id);
           assertNotNull(account);
           assertEquals(account.getId(), id);

           account = accountManager.getAccountById(Long.toString(id));
           assertNotNull(account);
           assertEquals(account.getId(), id);

           account = accountManager.addToBalance(account, BigDecimal.valueOf(balance));
           assertEquals(account.getBalance().doubleValue(), balance, 0);

           account = accountManager.subtractFromBalance(account, BigDecimal.valueOf(reducedAmount));
           assertEquals(account.getBalance().doubleValue(), balance - reducedAmount, 0);

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           accountManager.delete(account);
           assertNull(accountManager.getAccountById(account.getId()));
       }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetAccountDetailByIdWithInvalidId() {
        assertNull(accountManager.getAccountDetailById(0L));
    }

    @Test
    public void testAccountDetail() {
       Account account = accountManager.getAccountById(1L);
       TransactionType transactionType = TransactionType.ADVERTISER_SPEND;
       double amount = 500;
       double tax = 20;
       Date transactionTime = new Date();
       String description = "description";
       String ioReference = "IO Reference";
       String opportunityId = "Opportunity ID";
       Date dateRangeStart = DateUtils.getStartOfDay(transactionTime, TimeZoneUtils.getDefaultTimeZone());
       Date dateRangeEnd = DateUtils.getEndOfDay(transactionTime, TimeZoneUtils.getDefaultTimeZone());
       AccountDetail accountDetail = null;
       try {
           double originalBalance = account.getBalance().doubleValue();
           accountDetail = accountManager.newAccountDetail(account, transactionTime, BigDecimal.valueOf(amount), BigDecimal.valueOf(tax),
        		   transactionType, description, ioReference, opportunityId);
           assertNotNull(accountDetail);
           long id = accountDetail.getId();
           assertTrue(id > 0L);
           assertEquals(account, accountDetail.getAccount());
           assertEquals(amount, accountDetail.getAmount().doubleValue(), 0);
           assertEquals(description, accountDetail.getDescription());
           assertEquals(ioReference, accountDetail.getReference());
           assertEquals(opportunityId, accountDetail.getOpportunity());
           assertEquals(tax, accountDetail.getTax().doubleValue(), 0);
           assertEquals(amount + tax, accountDetail.getTotal().doubleValue(), 0);
           assertEquals(transactionType, accountDetail.getTransactionType());
           assertEquals(transactionTime, accountDetail.getTransactionTime());
           assertTrue(accountDetail.getAccount().getBalance().doubleValue() == originalBalance + amount);

           accountDetail = accountManager.getAccountDetailById(id);
           assertNotNull(accountDetail);
           assertEquals(accountDetail.getId(), id);

           accountDetail = accountManager.getAccountDetailById(Long.toString(id));
           assertNotNull(accountDetail);
           assertEquals(accountDetail.getId(), id);

           Long count = accountManager.countAllTransactions(account);
           assertTrue(count > 0);
           count = accountManager.countAllTransactions(account, new Range<Date>(dateRangeStart, dateRangeEnd));
           assertTrue(count > 0);
           count = accountManager.countAllTransactions(account, new Range<Date>(dateRangeStart, dateRangeEnd), transactionType);
           assertTrue(count > 0);

           List<AccountDetail> transactions = accountManager.getAllTransactions(account, new Sorting(desc("transactionTime")));
           assertTrue(transactions != null && transactions.size() > 0);
           assertEquals(transactions.get(0).getId(), accountDetail.getId());

           double newTax = 30;
           accountDetail.setTax(BigDecimal.valueOf(newTax));
           accountDetail.setTotal(BigDecimal.valueOf(amount + newTax));
           accountDetail = accountManager.update(accountDetail);
           accountDetail = accountManager.getAccountDetailById(accountDetail.getId());
           assertEquals(newTax, accountDetail.getTax().doubleValue(), 0);
           assertEquals(amount + newTax, accountDetail.getTotal().doubleValue(), 0);

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           accountManager.delete(accountDetail);
           assertNull(accountManager.getAccountDetailById(accountDetail.getId()));
       }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetVerificationCodeByIdWithInvalidId() {
        assertNull(accountManager.getVerificationCodeById(0L));
    }

    @Test
    public void testVerificationCode() {
       User user = userManager.getUserById(1L);
       CodeType codeType = CodeType.REMEMBER_ME;
       VerificationCode verificationCode = null;
       try {
           verificationCode = accountManager.newVerificationCode(user, codeType);
           assertNotNull(verificationCode);
           long id = verificationCode.getId();
           assertTrue(id > 0);

           verificationCode = accountManager.getVerificationCodeById(id);
           assertNotNull(verificationCode);
           assertEquals(verificationCode.getId(), id);

           verificationCode = accountManager.getVerificationCodeById(Long.toString(id));
           assertNotNull(verificationCode);
           assertEquals(verificationCode.getId(), id);

           String codeValue = verificationCode.getCode();
           verificationCode = accountManager.getVerificationCodeForCodeTypeAndCodeValue(codeType, codeValue);
           assertNotNull(verificationCode);
           assertEquals(verificationCode.getId(), id);

           Long count = accountManager.countAllVerificationCodesForUser(user);
           assertTrue(count > 0L);

           List<VerificationCode> codes = accountManager.getAllVerificationCodesForUser(user);
           assertTrue(codes != null && codes.contains(verificationCode));

           VerificationCode vc = accountManager.getVerificationCodeForCodeTypeAndCodeValue(codeType, codeValue);
           assertEquals(vc.getId(), verificationCode.getId());

           User u = accountManager.getUserForVerificationCodeTypeAndCodeValue(codeType, codeValue);
           assertEquals(user.getId(), u.getId());

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           accountManager.delete(verificationCode);
           assertNull(accountManager.getVerificationCodeById(verificationCode.getId()));
       }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testPaymentOptions() {
       PostalAddress postalAddress = accountManager.getPostalAddressById(1L);
       PaymentType paymentType = PaymentType.CHEQUE;
       String paymentAccount = "Testing" + System.currentTimeMillis();
       PaymentOptions paymentOptions = null;
       try {
           paymentOptions = accountManager.newPaymentOptions(paymentType, paymentAccount, postalAddress);
           assertNotNull(paymentOptions);
           long id = paymentOptions.getId();
           assertTrue(id > 0);

           assertEquals(paymentOptions, accountManager.getPaymentOptionsById(id));
           assertEquals(paymentOptions, accountManager.getPaymentOptionsById(Long.toString(id)));

           String newPaymentAccount = paymentAccount + "Changed";
           paymentOptions.setPaymentAccount(newPaymentAccount);
           paymentOptions = accountManager.update(paymentOptions);
           paymentOptions = accountManager.getPaymentOptionsById(id);
           assertEquals(newPaymentAccount, paymentOptions.getPaymentAccount());

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           accountManager.delete(paymentOptions);
           assertNull(accountManager.getPaymentOptionsById(paymentOptions.getId()));
       }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testPostalAddress() {
       String firstName = "FirstName" + System.currentTimeMillis();
       String lastName = "LastName" + System.currentTimeMillis();
       String address1 = "Address" + System.currentTimeMillis();
       String address2 = null;
       String city = "City" + System.currentTimeMillis();
       String state = "CA";
       String postcode = "90210";
       Country country = commonManager.getCountryByIsoCode("US");
       PostalAddress postalAddress = null;
       try {
           postalAddress = new PostalAddress();
           postalAddress.setFirstName(firstName);
           postalAddress.setLastName(lastName);
           postalAddress.setAddress1(address1);
           postalAddress.setAddress2(address2);
           postalAddress.setCity(city);
           postalAddress.setState(state);
           postalAddress.setPostcode(postcode);
           postalAddress.setCountry(country);
           postalAddress = accountManager.create(postalAddress);

           assertNotNull(postalAddress);
           long id = postalAddress.getId();
           assertTrue(id > 0);

           assertEquals(postalAddress, accountManager.getPostalAddressById(id));
           assertEquals(postalAddress, accountManager.getPostalAddressById(Long.toString(id)));

           String newAddress1 = address1 + "Changed";
           postalAddress.setAddress1(newAddress1);
           postalAddress = accountManager.update(postalAddress);
           postalAddress = accountManager.getPostalAddressById(id);
           assertEquals(newAddress1, postalAddress.getAddress1());

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           accountManager.delete(postalAddress);
           assertNull(accountManager.getPostalAddressById(postalAddress.getId()));
       }
    }

}