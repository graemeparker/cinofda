package com.adfonic.adserver;

import org.junit.Test;

public class TestInvalidIpAddressException extends BaseAdserverTest {

	@Test
	public void testInvalidIpAddressException1(){
		new InvalidIpAddressException("Some Message");
	}
	@Test
	public void testInvalidIpAddressException2(){
		new InvalidIpAddressException("Some Message",new Exception());
	}
}
