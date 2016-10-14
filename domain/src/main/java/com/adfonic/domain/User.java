package com.adfonic.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.*;

import org.apache.commons.lang.StringUtils;

import com.adfonic.util.PasswordUtils;
import com.adfonic.util.TimeZoneUtils;

/**
 * Represents a user of the web site.
 *
 * This class contains a password hashing algorithm that has been adapted
 * from the example at http://www.owasp.org/index.php/Hashing_Java.
 */
@Entity
@Table(name="USER")
public class User extends BusinessKey {
    private static final long serialVersionUID = 7L;

    public enum Status {
	UNVERIFIED, VERIFIED, PASSWORD_RESET, DISABLED;
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EMAIL",length=255,nullable=false)
    private String email;
    @Column(name="PASSWORD",length=32,nullable=true)
    private String password;
    @Column(name="SALT",length=32,nullable=false)
    private String salt;
    @Column(name="ALIAS",length=32,nullable=true)
    private String alias;
    @Column(name="STATUS",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name="LAST_LOGIN",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    @Column(name="TIME_ZONE",length=80,nullable=true)
    private String timeZone;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="COMPANY_ID",nullable=false)
    private Company company;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="USER_ROLE",joinColumns=@JoinColumn(name="USER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ROLE_ID",referencedColumnName="ID"))
    private Set<Role> roles;
    @Column(name="FIRST_NAME",length=80,nullable=true)
    private String firstName;
    @Column(name="LAST_NAME",length=80,nullable=true)
    private String lastName;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="COUNTRY_ID",nullable=true)
    private Country country;
    @Column(name="PHONE_NUMBER",length=80,nullable=true)
    private String phoneNumber;
    @Column(name="EMAIL_OPT_IN",nullable=false)
    private boolean emailOptIn;
    @Column(name="SECURITY_QUESTION",length=255,nullable=true)
    private String securityQuestion;
    @Column(name="SECURITY_ANSWER",length=255,nullable=true)
    private String securityAnswer;
    @Column(name="REFERRAL_TYPE",length=80,nullable=true)
    private String referralType;
    @Column(name="REFERRAL_TYPE_OTHER",length=255,nullable=true)
    private String referralTypeOther;
    @Column(name="PREFERENCES",length=255, nullable=true)
    private String preferences;

    /** Optional developer key, generated by Adfonic. */
    @Column(name="DEVELOPER_KEY",length=80,nullable=true)
    private String developerKey;
    
    @Column(name="CREATION_TIME",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="ADVERTISER_USER",joinColumns=@JoinColumn(name="USER_ID",referencedColumnName="ID"),inverseJoinColumns=@JoinColumn(name="ADVERTISER_ID",referencedColumnName="ID"))
    private Set<Advertiser> advertisers;
    
    {
        roles = new HashSet<Role>();
        advertisers = new HashSet<Advertiser>();
    }
    
    User() {}

    /** Use factory method on Company to create. */
    public User(Company company, String email, String password) {
    	this.company = company;
    	this.email = email;
    	setPassword(password);
    	this.status = Status.UNVERIFIED;
    	this.creationTime = new Date();
    }

    public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}

	public long getId() { return id; };
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns a friendly email string in the format
     * "Firstname Lastname" <email@domain.tld>
     * Removes any quotation marks that may be in the first or last names.
     * Returns just email@domain.tld if first/last names are not set.
     */
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

    public Date getLastLogin() {
	return lastLogin;
    }
    public void updateLastLogin() {
	lastLogin = new Date();
    }

    public TimeZone getTimeZone() {
	if (timeZone == null) { 
	    return company.getDefaultTimeZone(); 
	} else {
	    return TimeZoneUtils.getTimeZoneNonBlocking(timeZone);
	}
    }
    public void setTimeZone(TimeZone timeZone) {
	this.timeZone = timeZone.getID();
    }

    public Company getCompany() {
	return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Role> getRoles() {
	return roles;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

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

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
    }

    public boolean getEmailOptIn() { return emailOptIn; }
    public void setEmailOptIn(boolean emailOptIn) {
	this.emailOptIn = emailOptIn;
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) {
	this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) {
	this.securityAnswer = securityAnswer;
    }

    public String getReferralType() {
	return referralType;
    }
    public void setReferralType(String referralType) {
	this.referralType = referralType;
    }
    
    public String getReferralTypeOther() {
	return referralTypeOther;
    }
    public void setReferralTypeOther(String referralTypeOther) {
	this.referralTypeOther = referralTypeOther;
    }

    public boolean checkPassword(String entered) {
        return entered == null ? false : PasswordUtils.checkPassword(entered, password, salt);
    }
 
    public String getPassword() { return password; }

    public void setPassword(String password) {
        PasswordUtils.PasswordAndSalt passwordAndSalt = PasswordUtils.encodePassword(password);
        this.password = passwordAndSalt.getPassword();
        this.salt = passwordAndSalt.getSalt();
    }

    public VerificationCode newVerificationCode(VerificationCode.CodeType codeType) {
	return new VerificationCode(this, codeType);
    }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getDeveloperKey() { return developerKey; }
    public void setDeveloperKey(String developerKey) {
	this.developerKey = developerKey;
    }

    public Set<Advertiser> getAdvertisers() {
        return advertisers;
    }
    
    public Date getCreationTime() { 
        return creationTime; 
    }
    
    public String getRolesAsString() {
        return NamedUtils.namedCollectionToString(roles);
    }    
}
