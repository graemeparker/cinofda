package com.adfonic.tools.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.PaymentOptions.PaymentType;
import com.adfonic.dto.address.PostalAddressDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.dto.transactions.PaymentOptionsDto;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.presentation.transaction.service.TransactionService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/adfonic-tools2-context.xml" })
public class TransactionServiceIT {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private LocationService locationService;

    @Test
    public void testSavePaymentOptions() {
        try {
            String paymentAccount = "paymentAccount";
            String paymentAccountModified = paymentAccount + "Modified";
            PaymentType paymentType = PaymentType.PAYPAL;
            PostalAddressDto postalAddress = locationService.getPostalAddressById(1L);

            CompanyAccountingDto company = transactionService.getCompanyById(2L);
            if (company != null) {
                if (company.getPaymentOptions() != null) {
                    company = transactionService.clearPaymentOptions(company);
                    assertNull(company.getPaymentOptions());
                }
            }
            PaymentOptionsDto paymentOptions = new PaymentOptionsDto();
            paymentOptions.setPaymentAccount(paymentAccount);
            paymentOptions.setPaymentType(paymentType);
            paymentOptions.setPostalAddress(postalAddress);
            // This test the creation of PaymentOptions, links to an existing
            // address
            // Uses the format passing the PaymentOptions explicitly
            company = transactionService.savePaymentOptions(company, paymentOptions);

            assertNotNull(company.getPaymentOptions());
            assertEquals(company.getPaymentOptions().getPaymentAccount(), paymentAccount);
            assertEquals(company.getPaymentOptions().getPaymentType(), paymentType);
            assertEquals(company.getPaymentOptions().getPostalAddress().getId(), postalAddress.getId());

            String firstName = "firstName";
            String lastName = "lastName";
            String address1 = "123 Any Street";
            String address1Modified = address1 + " #B";
            String city = "Any City";
            String state = "CA";
            String postcode = "12345";
            CountryDto country = locationService.getCountryByIsoCode("US");
            PostalAddressDto address = new PostalAddressDto();
            address.setFirstName(firstName);
            address.setLastName(lastName);
            address.setAddress1(address1);
            address.setCity(city);
            address.setState(state);
            address.setPostcode(postcode);
            address.setCountry(country);
            // This tests the update of PaymentOptions, and the automatic
            // creation of a PostalAddress
            // Uses the format where the PaymentOptions is passed as part of the
            // company
            company.getPaymentOptions().setPaymentAccount(paymentAccountModified);
            company.getPaymentOptions().setPostalAddress(address);
            company = transactionService.savePaymentOptions(company);
            assertNotNull(company.getPaymentOptions());
            assertEquals(company.getPaymentOptions().getPaymentAccount(), paymentAccountModified);
            assertNotNull(company.getPaymentOptions().getPostalAddress());
            assertEquals(company.getPaymentOptions().getPostalAddress().getFirstName(), firstName);
            assertEquals(company.getPaymentOptions().getPostalAddress().getLastName(), lastName);
            assertEquals(company.getPaymentOptions().getPostalAddress().getAddress1(), address1);
            assertEquals(company.getPaymentOptions().getPostalAddress().getCity(), city);
            assertEquals(company.getPaymentOptions().getPostalAddress().getState(), state);
            assertEquals(company.getPaymentOptions().getPostalAddress().getPostcode(), postcode);
            assertNotNull(company.getPaymentOptions().getPostalAddress().getCountry());
            assertEquals(company.getPaymentOptions().getPostalAddress().getCountry(), country);

            // Tests an automatic update to a PostalAddress as part of a
            // PaymentOptions update
            // Uses the format where the PaymentOptions is passed as part of the
            // company
            company.getPaymentOptions().getPostalAddress().setAddress1(address1Modified);
            company = transactionService.savePaymentOptions(company);
            assertNotNull(company.getPaymentOptions());
            assertNotNull(company.getPaymentOptions().getPostalAddress());
            assertEquals(company.getPaymentOptions().getPostalAddress().getAddress1(), address1Modified);

            address = company.getPaymentOptions().getPostalAddress();

            company = transactionService.clearPaymentOptions(company);
            assertNull(company.getPaymentOptions());

            locationService.deletePostalAddress(address);
            assertNull(locationService.getPostalAddressById(address.getId()));

        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }

}
