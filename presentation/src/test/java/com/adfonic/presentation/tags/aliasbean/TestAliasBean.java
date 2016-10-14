package com.adfonic.presentation.tags.aliasbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestAliasBean extends AbstractAdfonicTest {
    @Test
    public void testGetFamily() {
        AliasBean bean = new AliasBean();
        assertEquals(AliasBean.COMPONENT_FAMILY, bean.getFamily());
    }
    
    @Test
    public void testGetRendererType() {
        AliasBean bean = new AliasBean();
        assertNull(bean.getRendererType());
    }

    static class FakeBean {}
    
    @Test
    public void testMakeAliasAndRemoveAlias() {
        final String alias = randomAlphaNumericString(10);
        final String value = randomAlphaNumericString(10);
        final FacesContext facesContext = mock(FacesContext.class, "facesContext");
        final ELContext elContext = mock(ELContext.class, "elContext");
        final Application application = mock(Application.class, "application");
        final ExpressionFactory expressionFactory = mock(ExpressionFactory.class, "expressionFactory");
        final ValueExpression valueVE = mock(ValueExpression.class, "valueVE");
        final FakeBean valueObj = new FakeBean();
        final ValueExpression aliasVE = mock(ValueExpression.class, "aliasVE");

        expect(new Expectations() {{
            // makeAlias
            oneOf (facesContext).getELContext(); will(returnValue(elContext));
            allowing (facesContext).getApplication(); will(returnValue(application));
            allowing (application).getExpressionFactory(); will(returnValue(expressionFactory));
            oneOf (expressionFactory).createValueExpression(elContext, value, Object.class); will(returnValue(valueVE));
            oneOf (valueVE).getValue(elContext); will(returnValue(valueObj));
            oneOf (expressionFactory).createValueExpression(elContext, alias, Object.class); will(returnValue(aliasVE));
            oneOf (aliasVE).setValue(elContext, valueObj);

            // removeAlias
            oneOf (facesContext).getELContext(); will(returnValue(elContext));
            oneOf (aliasVE).setValue(elContext, null);
        }});

        AliasBean bean = new AliasBean();
        bean.setAlias(alias);
        bean.setValue(value);

        // Shouldn't be active right off the bat
        assertFalse(bean.isActive());

        // Make the alias
        bean.makeAlias(facesContext);
        assertTrue(bean.isActive());

        // Redundant calls once active should do nothing
        bean.makeAlias(facesContext);
        assertTrue(bean.isActive());

        // Remove the alias
        bean.removeAlias(facesContext);
        assertFalse(bean.isActive());

        // Redundant calls once inactive should do nothing
        bean.removeAlias(facesContext);
        assertFalse(bean.isActive());
    }
    
    @Test
    public void testSaveState() {
        final String alias = randomAlphaNumericString(10);
        final String value = randomAlphaNumericString(10);
        final FacesContext facesContext = mock(FacesContext.class, "facesContext");

        expect(new Expectations() {{
        }});

        AliasBean bean = new AliasBean();
        bean.setAlias(alias);
        bean.setValue(value);
        
        Object state = bean.saveState(facesContext);
        assertEquals(Object[].class, state.getClass());
        
        Object[] stateValues = (Object[])state;
        assertEquals(2, stateValues.length);
        assertEquals(String[].class, stateValues[1].getClass());

        String[] stateStrings = (String[])stateValues[1];
        assertEquals(alias, stateStrings[0]);
        assertEquals(value, stateStrings[1]);
    }
    
    @Test
    public void testRestoreState() {
        final String alias = randomAlphaNumericString(10);
        final String value = randomAlphaNumericString(10);
        final FacesContext facesContext = mock(FacesContext.class, "facesContext");

        expect(new Expectations() {{
        }});

        AliasBean bean = new AliasBean();
        
        Object state = new Object[] { null, new String[] { alias, value } };
        bean.restoreState(facesContext, state);
        assertEquals(alias, bean.getAlias());
        assertEquals(value, bean.getValue());
    }
}