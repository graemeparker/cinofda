package com.adfonic.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name="CATEGORY")
public class Category extends BusinessKey implements Named {
    private static final long serialVersionUID = 5L;

    /**
     * Name of the category that represents the default or uncategorized state.
     * This is a workaround to avoid using id=0 to convey the default.
     * NOTE: This MUST match the database exactly.
     */
    public static final String NOT_CATEGORIZED_NAME = "byyd Not Categorized";

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PARENT_ID",nullable=true)
    private Category parent;
    //@OrderColumn(name="CATEGORY_ORDER",nullable=false)
    @OrderBy("id")
    @Transient
    @OneToMany(mappedBy="parent")
    private List<Category> children;
    @Column(name="IAB_ID",length=16,nullable=true)
    private String iabId;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="CHANNEL_ID",nullable=false)
    private Channel channel;

    {
        this.children = new ArrayList<Category>();
    }

    Category() {}

    /** Creates a new category. */
    public Category(String name) {
        this(name, null);
    }

    Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public Category newChild(String name) {
        Category child = new Category(name, this);
        children.add(child);
        return child;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
    }

    public List<Category> getChildren() { return children; }

    public String getIabId() {
        return iabId;
    }
    public void setIabId(String iabId) {
        this.iabId = iabId;
    }

    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
