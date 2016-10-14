package com.adfonic.presentation.tags.aliasbean;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;

public class AliasBeanTag extends UIComponentELTag {
    private static final transient Logger LOG = Logger.getLogger(AliasBeanTag.class.getName());
    
    private String alias;
    private String value;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public String getComponentType() {
        return AliasBean.class.getName();
    }

    @Override
    protected void setProperties(UIComponent component) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Setting properties");
        }
        super.setProperties(component);
        if (component instanceof AliasBean) {
            AliasBean aliasBean = (AliasBean)component;
            aliasBean.setAlias(alias);
            aliasBean.setValue(value);
        } else {
            LOG.warning("Unexpected UIComponent type: " + (component == null ? "null" : component.getClass().getName()));
        }
    }
}