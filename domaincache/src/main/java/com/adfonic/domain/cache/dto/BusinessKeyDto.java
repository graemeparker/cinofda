package com.adfonic.domain.cache.dto;

public abstract class BusinessKeyDto implements java.io.Serializable {
    private transient volatile Integer hashCode;
    private Long id;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
        hashCode = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        }
        
        if (BusinessKeyDto.class.isAssignableFrom(getClass()) && BusinessKeyDto.class.isAssignableFrom(o.getClass())) {
        	Long otherId = ((BusinessKeyDto)o).id; 
        	if (this.id == null) {
        		return otherId==null;
        	}
            return id.equals(otherId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            // Calculate a hash-friendly deterministic value for the given id
            int result = 17;
            result = 31 * result + (int)(id ^ (id >>> 32));

            hashCode = result; // "cache" it for immediate use next time
        }
        return hashCode;
    }
}
