package com.adfonic.domain;

import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name="VERIFICATION_CODE")
public class VerificationCode extends BusinessKey {
    private static final long serialVersionUID = 1L;

    public enum CodeType {
	REGISTRATION, CHANGE_EMAIL, RESET_PASSWORD, REMEMBER_ME 
    };

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="CODE",length=255,nullable=false)
    private String code;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID",nullable=false)
    private User user;
    @Column(name="CODE_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private CodeType codeType;

    {
	this.code = UUID.randomUUID().toString();
    }

    VerificationCode() {}

    // Use factory method on User to construct
    VerificationCode(User user, CodeType codeType) {
	this.user = user;
	this.codeType = codeType;
    }

    public long getId() { return id; };
    
    public String getCode() { return code; }

    public User getUser() { return user; }

    public CodeType getCodeType() { return codeType; }
}
