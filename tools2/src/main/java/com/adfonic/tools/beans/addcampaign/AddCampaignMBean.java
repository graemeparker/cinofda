package com.adfonic.tools.beans.addcampaign;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "campaign", pattern = "/addcampaign", viewId = "/WEB-INF/jsf/addcampaign/addcampaign.jsf") })
public class AddCampaignMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {
    }
}
