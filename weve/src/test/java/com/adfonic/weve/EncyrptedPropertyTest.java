package com.adfonic.weve;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.weve.test.CredentialsTestBean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:encrypted-properties-test-context.xml")
public class EncyrptedPropertyTest {
	
	@Autowired
	CredentialsTestBean credentials;

	@Test
	public void testPasswordIsDecryptedFromConfig() {
		assertThat(credentials.getUsername(), equalTo("myT3stUs3rn@me"));
		assertThat(credentials.getPassword(), equalTo("myT3stP@ssw0rd"));
	}

}
