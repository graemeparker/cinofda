package com.adfonic.tools.beans.timeout;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "sessiontimeout", pattern = "/sessiontimeout", viewId = "/WEB-INF/jsf/timeout/timeout.jsf") })
public class TimeoutMBean extends GenericAbstractBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {
    }
}
