package com.adfonic.presentation.tags.aliasbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.faces.component.UIComponent;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestAliasBeanTag extends AbstractAdfonicTest {
    @Test
    public void testGetRendererType() {
        AliasBeanTag tag = new AliasBeanTag();
        assertNull(tag.getRendererType());
    }

    @Test
    public void testGetComponentType() {
        AliasBeanTag tag = new AliasBeanTag();
        assertEquals(AliasBean.class.getName(), tag.getComponentType());
    }

    @Test
    public void testSetProperties_normal() {
        final AliasBean aliasBean = mock(AliasBean.class, "aliasBean");
        final String alias = randomAlphaNumericString(10);
        final String value = randomAlphaNumericString(10);
        
        expect(new Expectations() {{
            oneOf (aliasBean).setAlias(alias);
            oneOf (aliasBean).setValue(value);
        }});

        AliasBeanTag tag = new AliasBeanTag();
        tag.setAlias(alias);
        tag.setValue(value);
        tag.setProperties(aliasBean);
    }

    @Test
    public void testSetProperties_null() {
        final UIComponent component = null;
        
        expect(new Expectations() {{
        }});

        AliasBeanTag tag = new AliasBeanTag();
        tag.setProperties(component);
    }

    @Test
    public void testSetProperties_nonAliasBean() {
        final UIComponent nonAliasBeanComponent = mock(UIComponent.class, "component");
        
        expect(new Expectations() {{
        }});

        AliasBeanTag tag = new AliasBeanTag();
        tag.setProperties(nonAliasBeanComponent);
    }
}