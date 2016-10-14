package com.adfonic.tools.beans.addappsite;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "addappsite", pattern = "/addappsite", viewId = "/WEB-INF/jsf/addappsite/addappsite.jsf"),
        @URLMapping(id = "addapp", pattern = "/addapp", viewId = "/WEB-INF/jsf/addappsite/addapp.jsf"),
        @URLMapping(id = "addsite", pattern = "/addsite", viewId = "/WEB-INF/jsf/addappsite/addsite.jsf") })
public class AddAppSiteMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {

    }
}
