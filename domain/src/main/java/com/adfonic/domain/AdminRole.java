package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="ADMIN_ROLE")
public class AdminRole extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    AdminRole() {}

    public AdminRole(String name) {
    this.name = name;
    }

    public long getId() { return id; };

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
