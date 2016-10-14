package com.adfonic.adserver;

import static com.adfonic.adserver.KryoUtils.*;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.esotericsoftware.kryo.Kryo;

public class Click extends Impression {
    private Date expireTime;
    private String ipAddress;
    private String userAgentHeader;
    private boolean tracked;

    public Click() {}
    
    /**
     * Copy constructor to create a Click from an Impression
     * @param impression the Impression from which the click originated
     * @param creationTime the time at which the click occurred
     * @param expireTime the time at which the click should expire (no
     * longer be install or conversion trackable, etc.)
     * @param ipAddress the IP address from which the click originated
     * @param userAgentHeader the User-Agent header from which the click originated
     */
    public Click(Impression impression, Date creationTime, Date expireTime, String ipAddress, String userAgentHeader) {
        super(impression);
        setCreationTime(creationTime); // override impression.creationTime
        this.expireTime = expireTime;
        this.ipAddress = ipAddress;
        this.userAgentHeader = userAgentHeader;
    }

    @Override
    public void readObjectData(Kryo kryo, ByteBuffer buffer) {
        super.readObjectData(kryo, buffer);
        expireTime = getDate(buffer);
        ipAddress = getString(buffer);
        userAgentHeader = getString(buffer);
    }

    @Override
    public void writeObjectData(Kryo kryo, ByteBuffer buffer) {
        super.writeObjectData(kryo, buffer);
        putDate(buffer, expireTime);
        putString(buffer, ipAddress);
        putString(buffer, userAgentHeader);
    }

    public Date getExpireTime() {
        return expireTime;
    }
    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }
    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }
    
    public boolean isTracked() {
		return tracked;
	}
	public void setTracked(boolean tracked) {
		this.tracked = tracked;
	}

	@Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        return super.buildToString(builder)
            .append("expireTime", expireTime)
            .append("ipAddress", ipAddress)
            .append("userAgentHeader", userAgentHeader)
            .toString();
    }
}
