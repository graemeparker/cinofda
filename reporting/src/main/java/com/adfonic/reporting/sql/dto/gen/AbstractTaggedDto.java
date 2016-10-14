package com.adfonic.reporting.sql.dto.gen;

/**
 * Class provided for the equality. Tagged items should ideally have their identity bound 
 * to the tag and not any constituent stuff. Otherwise, don't use
 * 
 * Also enforces that a Tagged item can't be without one
 * 
 * Abstract because just having a tag alone isn't useful.
 * 
 */
public abstract class AbstractTaggedDto implements Tagged {

    public AbstractTaggedDto(Tag tag) {
        if (tag == null) {
            throw new RuntimeException("Needs a Tag to be Tagged!");
        }
        this.tag = tag;
    }

    private Tag tag;


    @Override
    public Tag getTag() {
        return tag;
    }


    @Override
    public final boolean equals(Object o) {
        try {
            return ((Tagged) o).getTag().equals(tag);
        } catch (RuntimeException e) {
            return false;
        }
    }


    @Override
    public final int hashCode() {
        return tag.hashCode();
    }

}
