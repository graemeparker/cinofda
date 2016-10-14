package com.adfonic.adserver.plugin;

public class PluginException extends Exception {
    static final long serialVersionUID = 1L;
    public PluginException(Throwable t) {
        super(t);
    }
    public PluginException(String msg) {
        super(msg);
    }
    public PluginException(String msg, Throwable t) {
        super(msg, t);
    }
}
