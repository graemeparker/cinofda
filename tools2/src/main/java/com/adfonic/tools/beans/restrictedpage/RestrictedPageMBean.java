package com.adfonic.tools.beans.restrictedpage;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "restrictedpage", pattern = "/restrictedpage", viewId = "/WEB-INF/jsf/restrictedpage/restrictedpage.jsf") })
public class RestrictedPageMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {
    }
}
