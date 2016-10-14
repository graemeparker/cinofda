package com.adfonic.data.cache.ecpm.key;

public abstract class LongLongKey {
    final long leftLong;
    final long rightLong;
    final int hash;

    protected LongLongKey(long leftLong, long rightLong) {
        this.leftLong = leftLong;
        this.rightLong = rightLong;

        hash = calculateHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongLongKey longLong = (LongLongKey) o;

        if (leftLong != longLong.leftLong) return false;
        if (rightLong != longLong.rightLong) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash;

    }

    private int calculateHash() {
        int result = (int) (leftLong ^ (leftLong >>> 32));
        result = 31 * result + (int) (rightLong ^ (rightLong >>> 32));
        return result;
    }


}
