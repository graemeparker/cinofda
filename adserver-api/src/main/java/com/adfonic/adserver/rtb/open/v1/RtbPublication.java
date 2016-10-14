package com.adfonic.adserver.rtb.open.v1;

import java.util.List;

/**
 * Base class for App and Site
 */
public abstract class RtbPublication {

    private String name;

    private String pid;

    private String pub;

    private String domain;

    private List<String> cat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
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
