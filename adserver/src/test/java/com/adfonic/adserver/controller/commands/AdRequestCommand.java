package com.adfonic.adserver.controller.commands;

import java.util.Map;

public interface AdRequestCommand {

	public String executeGetAdCommand(String adSpaceExternalId,Map<String,Object> queryMap) throws Exception;
}
