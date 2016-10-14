package com.adfonic.presentation.tags.aliasbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

@FacesComponent("com.adfonic.presentation.tags.aliasbean.AliasBean")
public class AliasBean extends UIComponentBase {
    private static final transient Logger LOG = Logger.getLogger(AliasBean.class.getName());

    static final String COMPONENT_FAMILY = "javax.faces.Data";

    private String alias;
    private String value;
    private ValueExpression aliasVE;
    private transient boolean active = false;
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    boolean isActive() {
        return active;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    void makeAlias(FacesContext facesContext) {
        if (isActive()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("NOT Making alias, already active");
            }
            return;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Making alias=" + alias + ", value=" + value);
        }

        ELContext elContext = facesContext.getELContext();

        // Parse the value expression
        ValueExpression valueVE = facesContext.getApplication().getExpressionFactory().createValueExpression(elContext, value, Object.class);
        Object valueObj = valueVE.getValue(elContext);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Initialized value object: " + (valueObj == null ? "null" : valueObj.getClass().getName()));
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Setting up aliasVE");
        }
        // Set up the alias expression and give it the evaluated value
        aliasVE = facesContext.getApplication().getExpressionFactory().createValueExpression(elContext, alias, Object.class);
        aliasVE.setValue(elContext, valueObj);

        active = true;
    }

    void removeAlias(FacesContext facesContext) {
        active = false;

        if (aliasVE != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Removing alias");
            }
            // Null out the value of the alias expression
            aliasVE.setValue(facesContext.getELContext(), null);
            aliasVE = null;
        }
    }

    private String[] saveState() {
        return new String[] { alias, value };
    }
    
    @Override
    public Object saveState(FacesContext facesContext) {
        LOG.fine("saveState was called");
        return new Object[] { super.saveState(facesContext), saveState() };
    }

    private void restoreState(Object state) {
        String[] values = (String[])state;
        alias = values[0];
        value = values[1];
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state) {
        LOG.fine("restoreState was called");
        Object[] values = (Object[])state;
        super.restoreState(facesContext, values[0]);
        restoreState(values[1]);
    }

    @Override
    public Object processSaveState(FacesContext facesContext) {
        LOG.fine("processSaveState was called");
        if (isTransient()) {
            return null;
        }

        makeAlias(facesContext);

        Map<String, Object> facetMap = null;
        for (Map.Entry<String, UIComponent> entry  : getFacets().entrySet()) {
            if (facetMap == null) {
                facetMap = new HashMap<String, Object>();
            }
            UIComponent component = (UIComponent)entry.getValue();
            if (!component.isTransient()) {
                facetMap.put(entry.getKey(), component.processSaveState(facesContext));
            }
        }
        List<Object> childrenList = (getChildCount() > 0) ? processChildrenList(facesContext) : null;

        removeAlias(facesContext);

        return new Object[] { saveState(facesContext), facetMap, childrenList };
    }

    private List<Object> processChildrenList(FacesContext facesContext) {
        List<Object> childrenList = null;
        for (UIComponent child : getChildren()) {
            if (!child.isTransient()) {
                if (childrenList == null) {
                    childrenList = new ArrayList<Object>(getChildCount());
                }
                childrenList.add(child.processSaveState(facesContext));
            }
        }
        return childrenList;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void processRestoreState(FacesContext facesContext, Object state) {
        LOG.fine("processRestoreState was called");
        
        Object myState = ((Object[])state)[0];
        restoreState(facesContext, myState);
        
        makeAlias(facesContext);

        Map facetMap = (Map)((Object[])state)[1];
        List childrenList = (List)((Object[])state)[2];
        for (Map.Entry<String, UIComponent> entry : getFacets().entrySet()) {
            Object facetState = facetMap.get(entry.getKey());
            if (facetState != null) {
                ((UIComponent)entry.getValue()).processRestoreState(facesContext, facetState);
            } else {
                facesContext.getExternalContext().log("No state found to restore facet " + entry.getKey());
            }
        }
        if (getChildCount() > 0) {
            int idx = 0;
            for (UIComponent child : getChildren()) {
                Object childState = childrenList.get(idx++);
                if (childState != null) {
                    child.processRestoreState(facesContext, childState);
                } else {
                    facesContext.getExternalContext().log("No state found to restore child of component " + getId());
                }
            }
        }

        removeAlias(facesContext);
    }

    @Override
    public void encodeBegin(FacesContext facesContext) throws java.io.IOException {
        LOG.fine("encodeBegin was called, making alias");
        makeAlias(facesContext);
    }

    @Override
    public void encodeEnd(FacesContext facesContext) throws java.io.IOException {
        LOG.fine("encodeEnd was called, removing alias");
        removeAlias(facesContext);
    }
    
    @Override
    public void processDecodes(FacesContext facesContext) {
        LOG.fine("Processing decodes");
        makeAlias(facesContext);
        super.processDecodes(facesContext);
        removeAlias(facesContext);
    }

    @Override
    public void processUpdates(FacesContext facesContext) {
        LOG.fine("Processing updates");
        makeAlias(facesContext);
        super.processUpdates(facesContext);
        removeAlias(facesContext);
    }
    
    @Override
    public void processValidators(FacesContext facesContext) {
        LOG.fine("Processing validators");
        makeAlias(facesContext);
        super.processValidators(facesContext);
        removeAlias(facesContext);
    }
}