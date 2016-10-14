package com.adfonic.adserver;

import org.apache.velocity.Template;

public interface TemplateBuilder {
    Template build(String templateName, String templateText);
}
