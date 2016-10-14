package com.adfonic.tasks.xaudit.appnxs.dat;

/**
 * Part of the Appnexus approval process is that the creative spawns a new 
 * window. They said that adding the following 
 * {"creative":{"mobile":{"alternative_landing_page_url":":yoururl"}} 
 * would be enough for approval.
 * 
 * https://wiki.appnexus.com/display/adnexusdocumentation/Creative+Service#CreativeService-Mobile
 * 
 * An alternative landing page URL that can be viewed in a desktop browser for creatives that have a landing page targeted to a specific device, operating system, or carrier.
 * 
 * You must provide an auditable URL in order for your creative to pass auditing.
 * 
 * @author graemeparker
 *
 */
public class CreativeMobile {

    private String alternative_landing_page_url;

    public CreativeMobile() {
    }

    public CreativeMobile(String alternative_landing_page_url) {
        this.alternative_landing_page_url = alternative_landing_page_url;
    }

    public String getAlternative_landing_page_url() {
        return alternative_landing_page_url;
    }

    public void setAlternative_landing_page_url(String alternative_landing_page_url) {
        this.alternative_landing_page_url = alternative_landing_page_url;
    }

}
