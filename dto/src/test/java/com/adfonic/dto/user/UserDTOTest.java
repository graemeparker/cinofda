package com.adfonic.dto.user;

import static org.junit.Assert.assertEquals;

import org.jdto.DTOBinder;
import org.jdto.DTOBinderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Company;
import com.adfonic.domain.Country;
import com.adfonic.domain.User;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.country.CountryDto;

public class UserDTOTest {

    DTOBinder dtoBinder;

    @Before
    public void setUp() throws Exception {
        dtoBinder = DTOBinderFactory.getBinder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBindUserToUserDTO() {
        final String companyName = "Acme Ltd";
        final AccountType accountType = AccountType.ADVERTISER;

        final Company company = new Company(companyName);
        company.setAccountTypeFlag(accountType);

        final CompanyDto companyDto = new CompanyDto();
        companyDto.setName(companyName);
        // companyDto.setAccountTypeFlag(accountType);

        final String countryName = "Great Britain";
        final Country country = new Country(countryName, "", "", "", null, false, null, 0, 0, 0);

        final CountryDto countryDto = new CountryDto();
        countryDto.setName(countryName);

        final String email = "someone@adfpnic.com";
        final String firstName = "David";
        final String lastName = "Crocks";
        final String password = "Pa55w0rd";
        final String formattedEmail = firstName + " " + lastName + " <" + email + ">";
        final Long id = new Long(1L);
        final String phoneNumber = "9309230910";
        final User.Status status = User.Status.VERIFIED;
        final String alias = "dacrock";

        final User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getCountry()).thenReturn(country);
        Mockito.when(mockUser.getEmail()).thenReturn(email);
        Mockito.when(mockUser.getFirstName()).thenReturn(firstName);
        Mockito.when(mockUser.getLastName()).thenReturn(lastName);
        Mockito.when(mockUser.getPassword()).thenReturn(password);
        Mockito.when(mockUser.getCompany()).thenReturn(company);
        Mockito.when(mockUser.getId()).thenReturn(id);
        Mockito.when(mockUser.getFormattedEmail()).thenReturn(formattedEmail);
        Mockito.when(mockUser.getPhoneNumber()).thenReturn(phoneNumber);
        Mockito.when(mockUser.getStatus()).thenReturn(status);
        Mockito.when(mockUser.getAlias()).thenReturn(alias);

        UserDTO userDTO = dtoBinder.bindFromBusinessObject(UserDTO.class, mockUser);

        assertEquals(userDTO.getEmail(), email);
        assertEquals(userDTO.getPhoneNumber(), phoneNumber);
        assertEquals(userDTO.getId(), id);
        assertEquals(userDTO.getFormattedEmail(), formattedEmail);
        assertEquals(userDTO.getFirstName(), firstName);
        assertEquals(userDTO.getLastName(), lastName);
        assertEquals(userDTO.getStatus(), status.toString());
        assertEquals(userDTO.getAccountTypeFlags(), new Integer(accountType.bitValue()));
        assertEquals(userDTO.getAlias(), alias);
    }

}
