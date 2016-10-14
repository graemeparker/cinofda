package com.adfonic.domain;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.adfonic.domain.MediaType;

public class TestMediaType {
	@Test
	public void test() {
        assertFalse(MediaType.BANNER_HTML.isMarkupRequired());
        assertFalse(MediaType.TEXT_HTML.isMarkupRequired());
        assertTrue(MediaType.HTML.isMarkupRequired());
        assertTrue(MediaType.HTML_JS.isMarkupRequired());
    }
}
