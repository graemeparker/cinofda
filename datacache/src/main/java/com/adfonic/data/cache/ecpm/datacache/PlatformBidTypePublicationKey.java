package com.adfonic.data.cache.ecpm.datacache;

import com.adfonic.domain.BidType;

public class PlatformBidTypePublicationKey {

    final long platformId;
    final long bidTypeValue;
    final long publicationId;
    final int hash;

    public PlatformBidTypePublicationKey(long platformId, BidType bidType, long publicationId) {
        this.platformId = platformId;
        this.bidTypeValue = bidType.ordinal();
        this.publicationId = publicationId;
        hash = calculateHash();
    }

    private int calculateHash() {
        int result = (int) (platformId ^ (platformId >>> 32));
        result = 31 * result + (int) (bidTypeValue ^ (bidTypeValue >>> 32));
        result = 31 * result + (int) (publicationId ^ (publicationId >>> 32));
        result = 31 * result + hash;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlatformBidTypePublicationKey that = (PlatformBidTypePublicationKey) o;

        if (bidTypeValue != that.bidTypeValue) return false;
        if (hash != that.hash) return false;
        if (platformId != that.platformId) return false;
        if (publicationId != that.publicationId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
