package com.adfonic.util;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subnet implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final String IP_ADDRESS_REGEX = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("^" + IP_ADDRESS_REGEX + "$");
    private static final Pattern CIDR_PATTERN = Pattern.compile("^(" + IP_ADDRESS_REGEX + ")(?:/(\\d{1,2}))?$");

    // RFC1918 24-bit block, 10.0.0.0 - 10.255.255.255
    public static final Subnet PRIVATE_CLASS_A = new Subnet("10.0.0.0/8");
    // RFC1918 20-bit block, 172.16.0.0 - 172.31.255.255
    public static final Subnet PRIVATE_CLASS_B = new Subnet("172.16.0.0/12");
    // RFC1918 16-bit block, 192.168.0.0 - 192.168.255.255
    public static final Subnet PRIVATE_CLASS_C = new Subnet("192.168.0.0/16");

    private final String cidr;
    private long low;
    private long high;
    private transient volatile InetAddress lowAddress = null;
    private transient volatile InetAddress highAddress = null;

    public Subnet(String cidr) {
        Matcher matcher = CIDR_PATTERN.matcher(cidr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Bad CIDR notation: " + cidr);
        }

        String netip = matcher.group(1);
        int bits = 32;
        if (matcher.groupCount() == 6) {
            String bitsStr = matcher.group(6);
            if (bitsStr != null) {
                bits = Integer.parseInt(matcher.group(6));
            }
        }

        if (bits <= 0 || bits > 32) {
            throw new IllegalArgumentException("Bad CIDR notation: " + cidr);
        } else if (bits == 32) {
            // It's just a single IP address
            low = high = getIpAddressValue(netip);
        } else {
            // Construct a mask used to filter the low value. i.e. if
            // the CIDR is "/24", the mask is 24 ones followed by 8 zeros.
            long mask = 0;
            for (int k = 1; k <= bits; ++k) {
                mask |= (1 << (32 - k));
            }

            // We need to & the low value against the mask, since you
            // could theoretically specify "123.45.67.89/8", which is
            // really the same as "123.0.0.0/8".
            low = getIpAddressValue(netip) & mask;

            // To construct the high value, we take the low value and
            // turn on all the bits to the right of the mask.
            high = low;
            for (int k = 0; k < 32 - bits; ++k) {
                high |= (1 << k);
            }
        }

        this.cidr = netip + "/" + bits;
    }
    
    /** Is a given IP address on a well-known RFC1918 private network */
    public static boolean isOnPrivateNetwork(String ip) {
        long value = getIpAddressValue(ip);
        return PRIVATE_CLASS_C.contains(value) || PRIVATE_CLASS_B.contains(value) || PRIVATE_CLASS_A.contains(value);
    }

    public String getCidr() {
        return cidr;
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }

    public InetAddress getLowAddress() {
        if (lowAddress == null) {
            synchronized (this) {
                if (lowAddress == null) {
                    lowAddress = getInetAddress(low);
                }
            }
        }
        return lowAddress;
    }

    public InetAddress getHighAddress() {
        if (highAddress == null) {
            synchronized (this) {
                if (highAddress == null) {
                    highAddress = getInetAddress(high);
                }
            }
        }
        return highAddress;
    }

    public boolean contains(String ip) {
        return contains(getIpAddressValue(ip));
    }

    public boolean contains(long ipAddressValue) {
        return ipAddressValue >= low && ipAddressValue <= high;
    }

    public static boolean isIpAddress(String ip) {
        Matcher matcher = IP_ADDRESS_PATTERN.matcher(ip);
        if (matcher.matches()) {
            return true;
        } else {
            // Hack for now...someday support IPv6 addresses...ugh...
            return "0:0:0:0:0:0:0:1%0".equals(ip);
        }
    }

    public static long getIpAddressValue(String ip) {
        Matcher matcher = IP_ADDRESS_PATTERN.matcher(ip);
        if (!matcher.matches()) {
            // Hack for now...someday support IPv6 addresses...ugh...
            if ("0:0:0:0:0:0:0:1%0".equals(ip)) {
                return getIpAddressValue("127.0.0.1");
            } else {
                throw new IllegalArgumentException("Not an IP address: " + ip);
            }
        }
        return (Long.parseLong(matcher.group(1)) << 24) | (Long.parseLong(matcher.group(2)) << 16) | (Long.parseLong(matcher.group(3)) << 8) | Long.parseLong(matcher.group(4));
    }

    public static InetAddress getInetAddress(long value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value & 0xFF000000) >> 24);
        bytes[1] = (byte) ((value & 0xFF0000) >> 16);
        bytes[2] = (byte) ((value & 0xFF00) >> 8);
        bytes[3] = (byte) (value & 0xFF);
        try {
            return InetAddress.getByAddress(bytes);
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Value doesn't represent an IP address: " + value, e);
        }
    }

    @Override
    public String toString() {
        return "Subnet[" + cidr + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cidr == null) ? 0 : cidr.hashCode());
        result = prime * result + (int) (high ^ (high >>> 32));
        result = prime * result + (int) (low ^ (low >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Subnet other = (Subnet) obj;
        if (cidr == null) {
            if (other.cidr != null) {
                return false;
            }
        } else if (!cidr.equals(other.cidr)) {
            return false;
        }
        if (high != other.high) {
            return false;
        }
        if (low != other.low) {
            return false;
        }
        return true;
    }

}
