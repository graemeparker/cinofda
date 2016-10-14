package com.adfonic.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.util.Streams;



public class FacesContextHelper {
    public static FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {

            FacesContextFactory contextFactory  = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            // TODO: call request.getServletContext() directly instead of going through the session?
            facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(), request, response, lifecycle);

            // Set using our inner class
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

            // set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
            facesContext.setViewRoot(view);                
        }
        return facesContext;
    }
    public static Application getApplication(FacesContext facesContext) {
        return facesContext.getApplication();        
    }

    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }

    /**
     * Evaluate expression which is assumed to be located in a file in the classpath
     * @param templatePath - reference to template file (such as /templates/some_template.html)
     * @param values - values that will be put into context for resolution
     * @return
     */
	public static String evaluateTemplate(FacesContext fc, InputStream templateStream,
			Map<String, Object> values) throws IOException{
		
		//InputStream in = fc.getClass().getResourceAsStream(templatePath);
		
		String expression = Streams.asString(templateStream);
			
		return evaluateTemplate(fc,expression,values);
		
	}
	
	public static String evaluateTemplate(FacesContext fc, String expression, Map<String,Object> values){
		Application app = FacesContextHelper.getApplication(fc);
		
		ELResolver resolver = app.getELResolver();
			
		ELContext context = fc.getELContext();
		
		if (values != null){
            for (Map.Entry<String,Object> entry : values.entrySet()) {
                resolver.setValue(context, null, entry.getKey(), entry.getValue());
            }
		}
		
		String text = (String) app.evaluateExpressionGet(fc,expression,String.class);
		
		return text;
	}  
}
