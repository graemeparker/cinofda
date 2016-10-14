package com.adfonic.valves;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TestRemoteAddressByPathValve {

	@InjectMocks
	private RemoteAddressByPathValve allowRequestValve;
	private NextValve nextValve;
	
	@Mock
	private Request request;
	@Mock
	private Response response;
	
	@Before
	public void setup() {
		nextValve = new NextValve();
		allowRequestValve.setNext(nextValve);
		allowRequestValve.setPath("/internal/*");
		allowRequestValve.setAllow("127\\.0\\.0\\.1");
	}
	
	@Test
	public void shouldAllowRequest() throws Exception {
		performRequest("/internal", "127.0.0.1", allowRequestValve);
		assertTrue("Allow /internal", nextValve.wasInvoked());
	}
	
	@Test
	public void shouldNotAllowRequest() throws Exception {
		performRequest("/internal", "86.1.1.1", allowRequestValve);
		assertFalse("Should not allow /internal", nextValve.wasInvoked());
		verify(response).sendError(404);
	}
	
	@Test
	public void shouldAllowRequestForNonBlockedPath() throws Exception {
		performRequest("/hello", "86.1.1.1", allowRequestValve);
		assertTrue("Allow /hello", nextValve.wasInvoked());
	}
	
	@Test
	public void shouldAllowRequestForNonBlockedPathFromLocal() throws Exception {
		performRequest("/hello", "127.0.0.1", allowRequestValve);
		assertTrue("Allow /hello", nextValve.wasInvoked());
	}
	
	private void performRequest(String path, String remoteAddr, Valve valve) throws Exception {
		given(request.getRemoteAddr()).willReturn(remoteAddr);
		given(request.getRequestURI()).willReturn(path);
		valve.invoke(request, response);
	}
	
	private class NextValve extends ValveBase {
		private boolean invoked = false;
		public boolean wasInvoked() {
			return invoked;
		}
		
		public void invoke(Request request, Response response) throws IOException, ServletException {
			invoked = true;
		}
	}
}
