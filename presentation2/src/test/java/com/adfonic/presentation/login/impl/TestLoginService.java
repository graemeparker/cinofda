package com.adfonic.presentation.login.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jdto.DTOBinder;
import org.jdto.DTOBinderFactory;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.Company;
import com.adfonic.domain.User;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;

public class TestLoginService extends AbstractAdfonicTest {

	UserManager mockUserManager;

	LoginServiceImpl loginService = new LoginServiceImpl();

	DTOBinder dtoBinder = DTOBinderFactory.getBinder();

	@Before
	public void setUp() throws Exception {
		mockUserManager = mock(UserManager.class);
		inject(loginService, "dtoBinder", dtoBinder);
		inject(loginService, "companyManager", mockUserManager);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	@Ignore
	public void unsuccesfullLogin() {
		final String email = "invalid.email@adfonic.com";

		expect(new Expectations() {
			{
				oneOf(mockUserManager).getUserByEmail(email);
				will(returnValue(null));

			}
		});

		UserDTO actualReturn = loginService.doLogin(email);

		assertEquals(null, actualReturn);
	}

	@Test
	@Ignore
	public void succesfullLogin() {

		final String companyName = "Acme Ltd";
		final AccountType accountType = AccountType.ADVERTISER;

		final Company company = new Company(companyName);
		company.setAccountTypeFlag(accountType);

		final String email = "valid.email@adfonic.com";
		final String password = "somepasswd";
		final String firstName = "David";
		final String lastName = "Crocks";
		final String formattedEmail = firstName + " " + lastName + " <" + email + ">";
		final Long id = new Long(1L);
		final String phoneNumber = "9309230910";
		final User.Status status = User.Status.VERIFIED;
		final FetchStrategy fcmock = mock(FetchStrategy.class);

		final User mockUser = mock(User.class);

		expect(new Expectations() {
			{
				oneOf(mockUserManager).getUserByEmail(email, fcmock);
				will(returnValue(mockUser));
				oneOf(mockUserManager).updateLastLogin(mockUser);
				will(returnValue(mockUser));
				allowing(mockUser).getEmail();
				will(returnValue(email));
				allowing(mockUser).getFirstName();
				will(returnValue(firstName));
				allowing(mockUser).getLastName();
				will(returnValue(lastName));
				allowing(mockUser).getPassword();
				will(returnValue(password));
				allowing(mockUser).getCompany();
				will(returnValue(company));
				allowing(mockUser).getId();
				will(returnValue(id));
				allowing(mockUser).getFormattedEmail();
				will(returnValue(formattedEmail));
				allowing(mockUser).getPhoneNumber();
				will(returnValue(phoneNumber));
				allowing(mockUser).getStatus();
				will(returnValue(status));
			}
		});

		UserDTO actualReturn = loginService.doLogin(email);

		assertNotNull(actualReturn);
		assertEquals(email, actualReturn.getEmail());
		assertEquals(phoneNumber, actualReturn.getPhoneNumber());
		assertEquals(id, actualReturn.getId());
		assertEquals(formattedEmail, actualReturn.getFormattedEmail());
		assertEquals(firstName, actualReturn.getFirstName());
		assertEquals(lastName, actualReturn.getLastName());
		assertEquals(company.getName(), actualReturn.getCompany());
		assertEquals(status.toString(), actualReturn.getStatus());
		assertEquals(new Integer(accountType.bitValue()), actualReturn.getAccountTypeFlags());
		assertEquals("advertiser", actualReturn.getUserType());

	}
}
