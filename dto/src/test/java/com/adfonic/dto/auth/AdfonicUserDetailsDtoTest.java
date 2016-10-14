package com.adfonic.dto.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jdto.DTOBinder;
import org.jdto.DTOBinderFactory;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Company;
import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.adfonic.test.AbstractAdfonicTest;

public class AdfonicUserDetailsDtoTest extends AbstractAdfonicTest {

    DTOBinder dtoBinder;

    @Before
    public void setUp() throws Exception {
        dtoBinder = DTOBinderFactory.getBinder();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testBindAdfonicUserDetailsDto() {
        final String roleCompanyAdminName = "CompanyAdmin";
        final Role mockCompanyAdminRole = mock(Role.class, roleCompanyAdminName);

        final String companyName = "Acme Ltd";
        final AccountType accountType = AccountType.ADVERTISER;

        final Company company = new Company(companyName);
        company.setAccountTypeFlag(accountType);
        company.getRoles().add(mockCompanyAdminRole);

        final String email = "someone@adfpnic.com";
        final String password = "somepassword";
        final User.Status status = User.Status.VERIFIED;

        final String roleUserName = Role.USER_ROLE_USER;
        final Role mockUserRole = mock(Role.class, roleUserName);

        final String roleAdvertiserName = Role.USER_ROLE_ADVERTISER;
        final Role mockAdvertiserUserRole = mock(Role.class, roleAdvertiserName);

        final Set<Role> userRoles = new HashSet<Role>();
        userRoles.add(mockAdvertiserUserRole);
        userRoles.add(mockUserRole);

        final User mockUser = mock(User.class);

        expect(new Expectations() {
            {
                allowing(mockUser).getEmail();
                will(returnValue(email));
                allowing(mockUser).getPassword();
                will(returnValue(password));
                allowing(mockUser).getCompany();
                will(returnValue(company));
                allowing(mockUser).getStatus();
                will(returnValue(status));
                allowing(mockUser).getRoles();
                will(returnValue(userRoles));

                allowing(mockCompanyAdminRole).getName();
                will(returnValue(roleCompanyAdminName));

                allowing(mockUserRole).getName();
                will(returnValue(roleUserName));

                allowing(mockAdvertiserUserRole).getName();
                will(returnValue(roleAdvertiserName));

            }
        });

        AdfonicUserDetailsDto adfonicUserDetailsDto = dtoBinder.bindFromBusinessObject(AdfonicUserDetailsDto.class, mockUser);

        assertEquals(adfonicUserDetailsDto.getUsername(), email);
        assertEquals(adfonicUserDetailsDto.getStatus(), status.toString());
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleCompanyAdminName));
        assertTrue(adfonicUserDetailsDto.isEnabled());
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleUserName));
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleAdvertiserName));

        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(new SimpleGrantedAuthority(roleCompanyAdminName)));
        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(new SimpleGrantedAuthority(roleUserName)));
        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(new SimpleGrantedAuthority(roleAdvertiserName)));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testBindAdfonicUserDetailsDtoNotEnabled() {
        final String roleCompanyAdminName = "CompanyAdmin";
        final Role mockCompanyAdminRole = mock(Role.class, roleCompanyAdminName);

        final String companyName = "Acme Ltd";
        final AccountType accountType = AccountType.ADVERTISER;

        final Company company = new Company(companyName);
        company.setAccountTypeFlag(accountType);
        company.getRoles().add(mockCompanyAdminRole);

        final String email = "someone@adfpnic.com";
        final String password = "somepassword";
        final User.Status status = User.Status.UNVERIFIED;

        final String roleUserName = Role.USER_ROLE_USER;
        final Role mockUserRole = mock(Role.class, roleUserName);

        final String roleAdvertiserName = Role.USER_ROLE_ADVERTISER;
        final Role mockAdvertiserUserRole = mock(Role.class, roleAdvertiserName);

        final Set<Role> userRoles = new HashSet<Role>();
        userRoles.add(mockAdvertiserUserRole);
        userRoles.add(mockUserRole);

        final User mockUser = mock(User.class);

        expect(new Expectations() {
            {
                allowing(mockUser).getEmail();
                will(returnValue(email));
                allowing(mockUser).getPassword();
                will(returnValue(password));
                allowing(mockUser).getCompany();
                will(returnValue(company));
                allowing(mockUser).getStatus();
                will(returnValue(status));
                allowing(mockUser).getRoles();
                will(returnValue(userRoles));

                allowing(mockCompanyAdminRole).getName();
                will(returnValue(roleCompanyAdminName));

                allowing(mockUserRole).getName();
                will(returnValue(roleUserName));

                allowing(mockAdvertiserUserRole).getName();
                will(returnValue(roleAdvertiserName));
            }
        });

        AdfonicUserDetailsDto adfonicUserDetailsDto = dtoBinder.bindFromBusinessObject(AdfonicUserDetailsDto.class, mockUser);

        GrantedAuthority companyAdmin = new SimpleGrantedAuthority(roleCompanyAdminName);

        assertEquals(adfonicUserDetailsDto.getUsername(), email);
        assertEquals(adfonicUserDetailsDto.getStatus(), status.toString());
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleCompanyAdminName));
        assertFalse(adfonicUserDetailsDto.isEnabled());
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleUserName));
        assertThat(adfonicUserDetailsDto.getRoles(), hasItem(roleAdvertiserName));

        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(companyAdmin));
        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(new SimpleGrantedAuthority(roleUserName)));
        assertThat(adfonicUserDetailsDto.getAuthorities(), (Matcher) hasItem(new SimpleGrantedAuthority(roleAdvertiserName)));
    }

}
