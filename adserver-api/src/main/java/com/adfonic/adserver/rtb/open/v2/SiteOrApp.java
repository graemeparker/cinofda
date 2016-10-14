package com.adfonic.adserver.rtb.open.v2;

import java.util.List;

public class SiteOrApp {

    /**
     * Exchange-specific site ID. / Exchange-specific app ID.
     */
    private String id;

    /**
     * Site name (may be aliased at the publisher’s request) / App name (may be aliased at the publisher’s request)
     */
    private String name;

    /**
     * Domain of the site (e.g., “mysite.foo.com”). / Domain of the app (e.g., “mygame.foo.com”)
     */
    private String domain;

    /**
     * Array of IAB content categories of the site. Refer to List 5.1
     */
    private List<String> cat;

    /**
     * Details about the Publisher (Section 3.2.8) of the site.
     */
    private Publisher publisher;

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getCat() {
        return cat;
    }

    public void setCat(List<String> cat) {
        this.cat = cat;
    }

}
