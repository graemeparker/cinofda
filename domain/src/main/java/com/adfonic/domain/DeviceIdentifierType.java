package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DEVICE_IDENTIFIER_TYPE")
public class DeviceIdentifierType extends BusinessKey {
    private static final long serialVersionUID = 4L;

    // Expose well-known system names to minimize hard-coding
    @Deprecated
    public static final String SYSTEM_NAME_ANDROID = "android";
    public static final String SYSTEM_NAME_DPID = "dpid";
    public static final String SYSTEM_NAME_HIFA = "hifa"; //sha1(IFA)
    public static final String SYSTEM_NAME_IFA = "ifa";
    @Deprecated
    public static final String SYSTEM_NAME_ODIN_1 = "odin-1";
    @Deprecated
    public static final String SYSTEM_NAME_OPENUDID = "openudid";
    @Deprecated
    public static final String SYSTEM_NAME_UDID = "udid";
    @Deprecated
    public static final String SYSTEM_NAME_ATID = "atid"; //AdTruth ID
    public static final String SYSTEM_NAME_MUID = "muid";
    public static final String SYSTEM_NAME_ADID = "adid";
    public static final String SYSTEM_NAME_ADID_MD5 = "adid_md5";
    public static final String SYSTEM_NAME_ADID_SHA1 = "adid_sha1";
    public static final String SYSTEM_NAME_GOUID = "gouid";
    @Deprecated
    // use IFA instead 
    public static final String SYSTEM_NAME_IDFA = "idfa";
    public static final String SYSTEM_NAME_IDFA_MD5 = "idfa_md5"; // hex(md5(ifa))

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(name = "NAME", length = 255, nullable = false)
    private String name;
    @Column(name = "SYSTEM_NAME", length = 32, nullable = false)
    private String systemName;
    @Column(name = "PRECEDENCE_ORDER", nullable = false)
    private int precedenceOrder;
    @Column(name = "HIDDEN", nullable = false)
    private boolean hidden;
    @Column(name = "VALIDATION_REGEX", length = 255, nullable = true)
    private String validationRegex;
    @Column(name = "SECURE", nullable = false)
    private boolean secure;
    @Column(name = "TRUSTE_ID_TYPE", length = 32, nullable = true)
    private String trusteIdType;

    @Override
    public long getId() {
        return id;
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public int getPrecedenceOrder() {
        return precedenceOrder;
    }

    public void setPrecedenceOrder(int precedenceOrder) {
        this.precedenceOrder = precedenceOrder;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public int compareTo(Object obj) {
        DeviceIdentifierType other = (DeviceIdentifierType) obj;
        return Integer.valueOf(precedenceOrder).compareTo(other.precedenceOrder);
    }

    public String getTrusteIdType() {
        return trusteIdType;
    }

    public void setTrusteIdType(String trusteIdType) {
        this.trusteIdType = trusteIdType;
    }
}
