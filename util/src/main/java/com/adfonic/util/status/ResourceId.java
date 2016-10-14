package com.adfonic.util.status;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author mvanek
 *
 */
public class ResourceId<ID extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ID id;

    private final String localHostname;

    private final String description;

    public ResourceId(ID id, String localhostName, String description) {
        if (id == null) {
            throw new IllegalArgumentException("Null id");
        }
        this.id = id;
        if (StringUtils.isEmpty(localhostName)) {
            throw new IllegalArgumentException("Empty localhost name");
        }
        this.localHostname = localhostName;

        this.description = description; //nullable
    }

    public ID getId() {
        return id;
    }

    public String getLocalHostname() {
        return localHostname;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ResourceId [id=" + id + ", description=" + description + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((localHostname == null) ? 0 : localHostname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceId other = (ResourceId) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (localHostname == null) {
            if (other.localHostname != null)
                return false;
        } else if (!localHostname.equals(other.localHostname))
            return false;
        return true;
    }

}
