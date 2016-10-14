package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.PreProcessorImpl.RuleSet;
import com.adfonic.util.FileUpdateMonitor;
import com.adfonic.util.Subnet;

public class TestPreProcessorImpl extends BaseAdserverTest{


	private PreProcessorImpl preProcessorImpl;
	

    @Before
	public void initTests(){
		
		preProcessorImpl = new PreProcessorImpl();
	}
	
	/*
	 * Pass targeting context as anything null or whatever and ruleSetRef is empty
	 */
	@Test
	public void testPreProcessorImpl01_preProcessRequest() throws BlacklistedException{
		final TargetingContext targetingContext = null;
		
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	/*
	 * Pass targeting context without IP address and ruleSetRef is empty
	 */
	@Test
	public void testPreProcessorImpl02_preProcessRequest() throws BlacklistedException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * and ip address is also white listed and effective uSerAgent is null
	 */
	@Test
	public void testPreProcessorImpl03_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "21.55.23.45";
		final String effectiveUserAgent = null;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		final Set<String> whiteListedIps = ruleSet.getWhitelistedIps();
		whiteListedIps.add(ipAddress);
		
		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));
				
		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * and ip address is also not white listed and also its not blacklisted
	 * effective uSerAgent is null
	 */
	@Test
	public void testPreProcessorImpl04_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "21.55.23.45";
		final Long tarettingIpAddress = 22552345L;
		final String effectiveUserAgent = null;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		
		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
				
		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	/*
	 * Pass targeting context with null IP address and ruleSetRef is initialized
	 * effective uSerAgent is null
	 */
	@Test
	public void testPreProcessorImpl05_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = null;
		final String effectiveUserAgent = null;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		
		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));
				
		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * ip address is also not white listed but ip Address is blacklisted
	 * effective uSerAgent is null
	 */
	@Test(expected=BlacklistedException.class)
	public void testPreProcessorImpl06_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "21.55.23.45";
		final Long tarettingIpAddress = 22552345L;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		ruleSet.getBlacklistedIps().add(ipAddress);
		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
				
		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * and ip address is also not white listed and ip address is white listest by subnet list
	 * effective uSerAgent is null
	 */
	@Test
	public void testPreProcessorImpl07_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "250.200.10.90";
		final Long tarettingIpAddress = 4207413850L;
		final String effectiveUserAgent = null;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		Subnet subnet = new Subnet(ipAddress);
		ruleSet.getWhitelistedSubnets().add(subnet);

		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));

		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * and ip address is also not white listed and ip address is not white listest by subnet list
	 * effective uSerAgent is null
	 */
	@Test
	public void testPreProcessorImpl08_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "250.200.10.90";
		final Long tarettingIpAddress = 4207413850L;
		final String effectiveUserAgent = null;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		Subnet subnet = new Subnet("123.34.56.67");
		ruleSet.getWhitelistedSubnets().add(subnet);

		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));

		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	/*
	 * Pass targeting context with some IP address and ruleSetRef is initialized
	 * and ip address is also not white listed and ip address is black listed by subnet list
	 * effective uSerAgent is null
	 */
	@Test(expected=BlacklistedException.class)
	public void testPreProcessorImpl09_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "250.200.10.90";
		final Long tarettingIpAddress = 4207413850L;
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		Subnet subnet = new Subnet(ipAddress);
		ruleSet.getBlacklistedSubnets().add(subnet);

		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
			//oneOf (targetingContext).getEffectiveUserAgent();
				//will(returnValue(effectiveUserAgent));

		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	
	@Test
	public void testPreProcessorImpl10_preProcessRequest() throws BlacklistedException, InvalidIpAddressException{
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final String ipAddress = "250.200.10.90";
		final Long tarettingIpAddress = 4207413850L;
		final String effectiveUserAgent = randomAlphaNumericString(20);
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);
		Subnet subnet = new Subnet("123.34.56.67");
		ruleSet.getBlacklistedSubnets().add(subnet);

		
		expect(new Expectations() {{
			oneOf (targetingContext).getAttribute(Parameters.IP);
				will(returnValue(ipAddress));
			oneOf (targetingContext).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
				will(returnValue(tarettingIpAddress));
			oneOf (targetingContext).getEffectiveUserAgent();
				will(returnValue(effectiveUserAgent));
			oneOf (targetingContext).setUserAgent(effectiveUserAgent);

		}});
		preProcessorImpl.preProcessRequest(targetingContext);
	}
	@Test
	public void testPreProcessorImpl11_destroy() throws RuntimeException{
		final FileUpdateMonitor rulesFileUpdateMonitor = mock(FileUpdateMonitor.class,"rulesFileUpdateMonitor");
		inject(preProcessorImpl, "rulesFileUpdateMonitor", rulesFileUpdateMonitor);
		
		expect(new Expectations() {{
		    oneOf (rulesFileUpdateMonitor).stop();
		}});
		
		preProcessorImpl.destroy();
	}
	@Test
	public void testPreProcessorImpl12_destroy() throws RuntimeException{
		final FileUpdateMonitor rulesFileUpdateMonitor = null;
		inject(preProcessorImpl, "rulesFileUpdateMonitor", rulesFileUpdateMonitor);
		
		preProcessorImpl.destroy();
	}
	
	@Test
	public void testPreProcessorImpl13_getModifiedUserAgent() throws RuntimeException{
		String userAgent = randomAlphaNumericString(20);
		final RuleSet ruleSet = new RuleSet();
		preProcessorImpl.ruleSetRef.set(ruleSet);

		String modifiedUserAgent = preProcessorImpl.getModifiedUserAgent(userAgent);
		assertEquals(userAgent, modifiedUserAgent);
		
	}
	
}
