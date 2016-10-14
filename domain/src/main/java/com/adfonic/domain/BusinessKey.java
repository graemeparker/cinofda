package com.adfonic.domain;

import org.apache.commons.lang.ClassUtils;

/**
 * Provides helper methods to get equals, hashCode, and toString
 * from key values.
 */
@SuppressWarnings("rawtypes")
public abstract class BusinessKey implements HasPrimaryKeyId, Comparable, java.io.Serializable {

	private static final long serialVersionUID = 1L;
    
    private transient volatile Integer hashCode;

    @Override
    public int hashCode() {
        if (hashCode == null) {
            long id = getId();
            if (id == 0) {
                // The object isn't persisted, it has no id.  Fall back on the
                // superclass hashCode method.  Note that we're deliberately
                // NOT setting the transient hashCode value here.  Postpone the
                // calc-and-cache of hashCode until we have a nonzero id.
                return super.hashCode();
            }

            // Calculate a hash-friendly deterministic value for the given id
            int result = 17;
            result = 31 * result + (int)(id ^ (id >>> 32));

            hashCode = result; // "cache" it for immediate use next time
        }
        return hashCode;
    }

    @Override
    @SuppressWarnings({"unchecked" })
    public boolean equals(Object o) {
        //System.out.println("equals(this=" + this.toString() + "," + o.toString() + ")");
        if (o == null) return false;
        if (o == this) return true;
        final Class thisClass = getClass();
        final Class otherClass = o.getClass();
        if (!thisClass.isAssignableFrom(otherClass) &&
            !otherClass.isAssignableFrom(thisClass)) return false;

        long id = this.getId();
        if (id == 0) {
            // The object isn't persisted, it has no id.  Fall back on the
            // superclass equals method.
            return super.equals(o);
        }
        else {
            // If they're both HasPrimaryKeyId, just see if the ids are equal
            return id == ((HasPrimaryKeyId)o).getId();
        }
    }

    
    @SuppressWarnings({"unchecked" })
	public int compareTo(Object o) {
        //System.out.println("compareTo(this=" + this.toString() + "," + o.toString() + ")");
        if (o == null) return 1; // Sort nulls first
        if (o == this) return 0;
        final Class thisClass = getClass();
        final Class otherClass = o.getClass();
        if (!thisClass.isAssignableFrom(otherClass) &&
            !otherClass.isAssignableFrom(thisClass)) {
            throw new IllegalArgumentException("Can't compare " + thisClass + " to " + otherClass);
        }
        
        // Just compare ids
        Comparable lObj = this.getId();
        Comparable rObj = ((HasPrimaryKeyId)o).getId();
        return lObj.compareTo(rObj);
    }

    /**
     * If the object is an instance of the Named interface, this
     * returns the value of getName().  Otherwise, creates a human-readable
     * representation of the object, consisting of the short class
     * name and the id (i.e. "AdSpace/123").  This value is not
     * intended to be unique or stable.
     */
    @Override
    public String toString() {
        if (this instanceof Named) {
            return ((Named)this).getName();
        }
        return ClassUtils.getShortClassName(getClass()) + "/" + getId();
    }
}
