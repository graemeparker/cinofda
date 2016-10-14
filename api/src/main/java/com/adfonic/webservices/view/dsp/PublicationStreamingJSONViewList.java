package com.adfonic.webservices.view.dsp;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.simple.JSONObject;

import com.adfonic.domain.Publication;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.service.PublicationManager;

/**
 * @see PublicationStreamingList
 * 
 * A PublicationStreamingList wrapper which does nothing beyond providing an iterator 
 * which lets exactly one forward pass through the list. The logical list it wraps is 
 * one of Publications, as a JSONPublication the exact contents of which is determined by 
 * publicationExtractor.
 *
 */
public class PublicationStreamingJSONViewList implements List<JSONObject> {

    final PublicationExtractor publicationExtractor;

    final private Iterator<Publication> publicationListIterator;

    private Iterator<JSONObject> iterator = new Iterator<JSONObject>() {

        @Override
        public boolean hasNext() {
            return publicationListIterator.hasNext();
        }


        @Override
        public JSONObject next() {
            return publicationExtractor.getPublicationJSON(publicationListIterator.next());
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };


    public PublicationStreamingJSONViewList(PublicationManager publicationManager, int pageSize, FetchStrategy fetchStrategy, PublicationExtractor publicationExtractor, PublicationFilter filter) {
        this.publicationExtractor = publicationExtractor;
        this.publicationListIterator = new PublicationStreamingList(publicationManager, pageSize, fetchStrategy, filter).iterator();
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
    public Iterator<JSONObject> iterator() {
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
    public boolean add(JSONObject e) {
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
    public boolean addAll(Collection<? extends JSONObject> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean addAll(int index, Collection<? extends JSONObject> c) {
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
    public JSONObject get(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public JSONObject set(int index, JSONObject element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void add(int index, JSONObject element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public JSONObject remove(int index) {
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
    public ListIterator<JSONObject> listIterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<JSONObject> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<JSONObject> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

}
