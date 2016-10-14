package com.adfonic.webservices.view.dsp;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.adfonic.domain.Publication;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.service.PublicationManager;

/**
 * A list wrapper which does nothing beyond providing an iterator which lets
 * exactly one forward pass through the list. The logical list it wraps is one of
 * Publications, as a Publication the exact format of which is determined by 
 * publicationExtractor. The Publication list is a resultset and this object holds
 * one page at a time moving forward.
 * <p>
 * The idea is not to provide fast streaming for a particular request (in which case we'd 
 * have worker threads or so doing the fetch), but just to avoid caching full resultset in
 * mem. There will be pauses b/n fetches. The caller is likely to be a service method 
 * invoked by a daily batch at the agency end. So it can take all the time it needs, just 
 * let the fetch size be controlled.
 * <p>
 *
 */
public class PublicationStreamingList implements List<Publication> {

    final PublicationFilter filter;

    final FetchStrategy fetchStrategy;
    final PublicationManager publicationManager;
    final int pageSize;

    List<Publication> publicationList;
    Iterator<Publication> internalIterator;
    int offset = 0;
    boolean nothingLeft = false;

    private Iterator<Publication> iterator = new Iterator<Publication>() {

        @Override
        public boolean hasNext() {
            if (nothingLeft) {
                return false;
            }

            if (internalIterator.hasNext()) {
                return true;
            }

            refetch();
            return hasNext();
        }


        @Override
        public Publication next() {
            return internalIterator.next();
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };


    public PublicationStreamingList(PublicationManager publicationManager, int pageSize, FetchStrategy fetchStrategy, PublicationFilter filter) {
        this.publicationManager = publicationManager;
        this.pageSize = pageSize;
        this.fetchStrategy = fetchStrategy;
        this.filter = filter;
        refetch();
    }


    private void refetch() {
        publicationList = publicationManager.getAllPublicationsReadOnly(filter, new Pagination(offset, pageSize, new Sorting(Direction.ASC, "id")), fetchStrategy);
        if (publicationList == null || publicationList.isEmpty()) {
            nothingLeft = true;
        } else {
            offset += pageSize;
            internalIterator = publicationList.iterator();
        }
    }


    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Iterator<Publication> iterator() {
        return iterator;
    }


    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }


    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean add(Publication e) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(Collection<? extends Publication> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(int index, Collection<? extends Publication> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Publication get(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Publication set(int index, Publication element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(int index, Publication element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Publication remove(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<Publication> listIterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<Publication> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<Publication> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

}
