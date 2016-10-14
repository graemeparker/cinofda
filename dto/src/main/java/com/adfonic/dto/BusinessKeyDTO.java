package com.adfonic.dto;

import java.io.Serializable;

import org.jdto.annotation.Source;

public abstract class BusinessKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    protected static final int DEFAULT_PRIME = 31;

    @Source(value = "id")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = DEFAULT_PRIME;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BusinessKeyDTO other = (BusinessKeyDTO) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }

    public boolean persisted() {
        return id != null && id > 0;
    }

}
