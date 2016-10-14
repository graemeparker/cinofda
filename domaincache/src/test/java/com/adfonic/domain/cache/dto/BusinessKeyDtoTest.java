package com.adfonic.domain.cache.dto;

import org.junit.Assert;
import org.junit.Test;

public class BusinessKeyDtoTest {

	@Test
	public void testEqualsObject() {
		
		BusinessKeyDtoImpl obj1 = new BusinessKeyDtoImpl(123L);

		BusinessKeyDtoImpl obj2 = new BusinessKeyDtoImpl(123L);
		
		Assert.assertTrue(obj1.equals(obj1));
		Assert.assertFalse(obj1.equals(null));
		Assert.assertFalse(obj1.equals(new Object()));
		Assert.assertTrue(obj1.equals(obj2));
	}
	
	@Test
	public void testNullIds() {
		
		Assert.assertTrue(new BusinessKeyDtoImpl(null).equals(new BusinessKeyDtoImpl(null)));
		Assert.assertFalse(new BusinessKeyDtoImpl(null).equals(new BusinessKeyDtoImpl(123L)));
		Assert.assertFalse(new BusinessKeyDtoImpl(123L).equals(new BusinessKeyDtoImpl(null)));
		Assert.assertTrue(new BusinessKeyDtoImpl(123L).equals(new BusinessKeyDtoImpl(123L)));
		Assert.assertFalse(new BusinessKeyDtoImpl(123L).equals(new BusinessKeyDtoImpl(555L)));
	}

	@Test
	public void testDifferentLongsOfThaSameValue() {
		Assert.assertTrue(new BusinessKeyDtoImpl(new Long(123)).equals(new BusinessKeyDtoImpl(new Long(123))));
	}
	
	@Test
	public void testEqualsNotAssignableToEachOther() {
		
		BusinessKeyDtoImpl obj1 = new BusinessKeyDtoImpl(new Long(123));
		BusinessKeyDtoImplA obj2 = new BusinessKeyDtoImplA(new Long(123));
		
		Assert.assertTrue(obj1.equals(obj2));
		Assert.assertTrue(obj2.equals(obj1));
	}
	
	class BusinessKeyDtoImpl extends BusinessKeyDto {
		private static final long serialVersionUID = 1L;
		BusinessKeyDtoImpl(Long id) {
			super.setId(id);
		}
	}
	class BusinessKeyDtoImplA extends BusinessKeyDto {
		private static final long serialVersionUID = 1L;
		BusinessKeyDtoImplA(Long id) {
			super.setId(id);
		}
	}

}
