package com.adfonic.tasks.combined.truste.dao;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore("rely on local database")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/optout-dao-test-context.xml" })
public class OptOutServiceDaoImplTest {

	@Autowired
	private OptOutServiceDaoImpl testObj;

	@Test
	public void oneHIFA() {
		Assert.assertNotNull(testObj);

		Integer saveOptIn = testObj.saveUserPreferences("5dc5d98318071f1a8fb78a83837c2c09539aec57~7", true);
		Assert.assertEquals(1, saveOptIn.intValue());
	}
	
	@Ignore("stored procedure check first id length")
	@Test
	public void oneIFA() {
		Assert.assertNotNull(testObj);

		Integer saveOptIn = testObj.saveUserPreferences("671ea9ae-31d8-43af-a51c-deb70547b571~6", true);
		Assert.assertEquals(1, saveOptIn.intValue());
	}
	
	@Ignore("stored procedure check first id length")
	@Test
	public void oneAnID() {
		Assert.assertNotNull(testObj);
		
		Integer saveOptIn = testObj.saveUserPreferences("671ea9ae31d843af~4", true);
		Assert.assertEquals(1, saveOptIn.intValue());
	}
	
	@Test
	public void twoHifaIfa() {
		Assert.assertNotNull(testObj);

		Integer saveOptIn = testObj.saveUserPreferences("5dc5d98318071f1a8fb78a83837c2c09539aec57~7"//
				+"|671ea9ae-31d8-43af-a51c-deb70547b571~6", true);
		Assert.assertEquals(2, saveOptIn.intValue());
	}

	@Test
	public void threeHifaIfaAnID() {
		Assert.assertNotNull(testObj);

		String ids = "5dc5d98318071f1a8fb78a83837c2c09539aec57~7"//
				+ "|671ea9ae-31d8-43af-a51c-deb70547b571~6"//
				+ "|378c2c09539aec57~4";
		Integer saveOptIn = testObj.saveUserPreferences(ids, true);
		Assert.assertEquals(3, saveOptIn.intValue());
		// TODO check the records were actually inserted
	}
	
	@Test
	public void threeVariousPipeEnd() {
		Assert.assertNotNull(testObj);
		
		String ids = "5dc5d98318071f1a8fb78a83837c2c09539aec57~7"//
				+ "|671ea9ae-31d8-43af-a51c-deb70547b571~6"//
				+ "|378c2c09539aec57~4"//
				+ "|";
		Integer saveOptIn = testObj.saveUserPreferences(ids, true);
		Assert.assertEquals(-3, saveOptIn.intValue());
	}

}
