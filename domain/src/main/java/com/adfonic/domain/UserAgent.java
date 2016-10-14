package com.adfonic.domain;

import java.util.Date;

import javax.persistence.*;

/**
 * User-Agent header and Model association
 */
@Entity
@Table(name="USER_AGENT")
public class UserAgent extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="UA_HEADER",length=255,nullable=false)
    private String userAgentHeader;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="MODEL_ID",nullable=true)
    private Model model;
    @Column(name="DATE_LAST_SEEN",nullable=true)
    private Date dateLastSeen;

    public long getId() { return id; };
    
    public String getUserAgentHeader() {
        return userAgentHeader;
    }
    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }

    public Model getModel() {
        return model;
    }
    public void setModel(Model model) {
        this.model = model;
    }

	public Date getDateLastSeen() {
		return dateLastSeen;
	}

	public void setDateLastSeen(Date dateLastSeen) {
		this.dateLastSeen = dateLastSeen;
	}
}
