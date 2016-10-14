package com.adfonic.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.StringUtils;

import com.adfonic.util.PasswordUtils;

/**
 * Represents an Adfonic internal/admin user.
 */
@Entity
@Table(name="ADFONIC_USER")
public class AdfonicUser extends BusinessKey {
    private static final long serialVersionUID = 2L;

    public enum Status {
        ACTIVE, DELETED;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EMAIL",length=255,nullable=false)
    private String email;
    @Column(name="PASSWORD",length=32,nullable=true)
    private String password;
    @Column(name="SALT",length=32,nullable=false)
    private String salt;
    @Column(name="FIRST_NAME",length=80,nullable=true)
    private String firstName;
    @Column(name="LAST_NAME",length=80,nullable=true)
    private String lastName;
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name="LOGIN_NAME",length=50,nullable=false)
    private String loginName;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ADFONIC_USER_ADMIN_ROLE",joinColumns=@JoinColumn(name="ADFONIC_USER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ADMIN_ROLE_ID",referencedColumnName="ID"))
    private Set<AdminRole> roles;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ADFONIC_USER_USER",joinColumns=@JoinColumn(name="ADFONIC_USER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="USER_ID",referencedColumnName="ID"))
    private Set<User> users;
    
    {
        users = new HashSet<User>();
    }

    public long getId() { return id; };

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFormattedEmail() {
        String fullName = getFullName();
        if (StringUtils.isNotEmpty(fullName)) {
            fullName = fullName.replaceAll("\"", "");
            if (StringUtils.isNotBlank(fullName)) {
                return "\"" + fullName + "\" <" + getEmail() + ">";
            }
        }
        return getEmail();
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        PasswordUtils.PasswordAndSalt passwordAndSalt = PasswordUtils.encodePassword(password);
        this.password = passwordAndSalt.getPassword();
        this.salt = passwordAndSalt.getSalt();
    }

    public boolean checkPassword(String entered) {
        return entered == null ? false : PasswordUtils.checkPassword(entered, password, salt);
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        StringBuilder bld = new StringBuilder();
        if (StringUtils.isNotBlank(firstName)) {
            bld.append(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            if (bld.length() > 0) {
                bld.append(' ');
            }
            bld.append(lastName);
        }
        return bld.toString();
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLoginName() {
        return loginName;
    }
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Set<AdminRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<AdminRole> roles) {
        this.roles = roles;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
