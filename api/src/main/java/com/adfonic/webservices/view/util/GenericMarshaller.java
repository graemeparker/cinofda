package com.adfonic.webservices.view.util;

import java.io.PrintWriter;

public interface GenericMarshaller {

	public abstract void marshal(Object object, String wrapperName, PrintWriter writer) throws Exception;

}