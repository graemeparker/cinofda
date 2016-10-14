package com.adfonic.adserver.plugin;

public class NoAdPluginException extends PluginException {
	private static final long serialVersionUID = 1L;
	
	public NoAdPluginException() {
        super("No ad available from plugin");
    }
    public NoAdPluginException(String msg) {
        super(msg);
    }
}
