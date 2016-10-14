package com.adfonic.adserver.controller.commands;

import java.util.Map;

public interface ClickThroughRequestCommand {

	public String executeClickThroughCommand(String adSpaceExternalId,String impressionExternalId,Map<String,Object> queryMap) throws Exception;
}
