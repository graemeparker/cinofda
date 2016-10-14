package com.byyd.middleware.iface.service;

/**
 * Annotation to avoid spring load the class annotated using 
 * context:component-scan directive.
 * 
 * Spring configuration should be configured as follow:
 * 
 * <context:component-scan base-package="com.byyd.middleware">
 *    <context:exclude-filter type="annotation" expression="com.byyd.middleware.iface.service.NotAutoScan"/>
 * </context:component-scan>
 * 
 * @author damartin
 */
public @interface NotAutoScan {

}
