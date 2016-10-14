package com.adfonic.geo.kdtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.adfonic.geo.Coordinates;
import com.adfonic.geo.GeoLocationStrategy;

public class KdTree<T extends Coordinates> implements GeoLocationStrategy<T> {

    //implementation of algorithm to find nearest point using K-d trees
    //see:  http://en.wikipedia.org/wiki/K-d_tree

    private final Comparator<T> latitudeSort;
    private final Comparator<T> longitudeSort;

    private final BNode<T> root;

    public KdTree(Collection<T> values) {

        latitudeSort = createLatitudeComparator();

        longitudeSort = createLongitudeComparator();

        root = createSubTree(new ArrayList<T>(values), 0);
    }

    private BNode<T> createSubTree(List<T> list, int deep) {
        if (list.isEmpty()) {
            return null;
        }

        int size = list.size();

        SplitAxis axis = SplitAxis.getAxisFromDeep(deep);

        if (size == 1){
            return new BNode<T>(list.get(0),  null,  null, axis);
        }

        if (axis == SplitAxis.LATITUDE_AXIS){
            Collections.sort(list, latitudeSort);
        } else {
            Collections.sort(list, longitudeSort);
        }

        int median = size / 2;

        List<T> leftList = list.subList(0, median);
        List<T> rightList = list.subList(median + 1, size);

        return new BNode<T>(list.get(median), createSubTree(leftList, deep + 1), createSubTree(rightList, deep + 1), axis);
    }

    public BNode<T> getRoot() {
        return root;
    }

    private Comparator<T> createLongitudeComparator() {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Double.compare(o1.getLongitude(), o2.getLongitude());
            }
        };
    }

    private Comparator<T> createLatitudeComparator() {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Double.compare(o1.getLatitude(), o2.getLatitude());
            }
        };
    }


    @Override
    public T getNearest(double latitude, double longitude) {

        FindNearest<T> algo = new FindNearest<T>(latitude, longitude);

        return algo.findNearestOnTree(root).getNode().getCoordinates();

    }



}
