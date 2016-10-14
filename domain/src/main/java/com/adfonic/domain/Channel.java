package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name="CHANNEL")
public class Channel extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;
    
    /**
     * Name of the channel that represents the default or uncategorized state.
     * This is a workaround to avoid using id=0 to convey the default.
     * NOTE: This MUST match the database exactly.
     */
    public static final String NOT_CATEGORIZED_NAME = "Uncategorized";
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @OneToMany(mappedBy="channel")
    @Transient
    private Set<Category> categories;

    {
        this.categories = new HashSet<Category>();
    }
    
    Channel() {}

    public Channel(String name) {
        this.name = name;
    }

    public long getId() { return id; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Set<Category> getCategories() {
        return categories;
    }
}
