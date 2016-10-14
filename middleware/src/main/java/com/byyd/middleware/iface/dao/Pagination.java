package com.byyd.middleware.iface.dao;


public class Pagination {
    private final int offset;
    private final int limit;
    private final Sorting sorting;


    public Pagination(int offset, int limit) {
        this(offset, limit, null);
    }

    public Pagination(int offset, int limit, Sorting sorting) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }

        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        this.offset = offset;
        this.limit = limit;
        this.sorting = sorting;
    }

    public Pagination(Pagination page) {
        this(page.getOffet(), page.getLimit(), page.getSorting());
    }

    public Pagination(Pagination page, Sorting sort) {
        this(page.getOffet(), page.getLimit(), sort);
    }

    public int getLimit() {
        return limit;
    }

    public int getOffet() {
        return offset;
    }

    public Sorting getSorting() {
        return sorting;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pagination)) {
            return false;
        }
        Pagination that = (Pagination) obj;
        boolean offsetEqual = this.offset == that.offset;
        boolean limitEqual = this.limit == that.limit;
        boolean sortEqual = this.sorting == null ? that.sorting == null : this.sorting.equals(that.sorting);

        return offsetEqual && limitEqual && sortEqual;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + offset;
        result = 31 * result + limit;
        result = 31 * result + (null == sorting ? 0 : sorting.hashCode());

        return result;
    }

}
