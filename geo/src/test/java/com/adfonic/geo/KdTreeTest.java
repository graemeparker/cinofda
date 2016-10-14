package com.adfonic.geo;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.adfonic.geo.kdtree.KdTree;

public class KdTreeTest {

    private KdTree<Coordinates> kdTree;

    @Before
    public void setUp() throws Exception {
        //(2,3), (5,4), (9,6), (4,7), (8,1), (7,2).


        Map<String, Coordinates> coordinatesMap = new HashMap<>();

        coordinatesMap.put("a", coord(2, 3));
        coordinatesMap.put("b", coord(5, 4));
        coordinatesMap.put("c", coord(9, 6));
        coordinatesMap.put("d", coord(4, 7));
        coordinatesMap.put("e", coord(8, 1));
        coordinatesMap.put("f", coord(7, 2));


        kdTree = new KdTree<>(coordinatesMap.values());

    }


    private static Coordinates coord(double latitude, double longitude) {
        return new SimpleCoordinates(latitude, longitude);
    }


    @Test
    public void findNearestWhenNearALeaf() throws Exception {

        assertThat(kdTree.getNearest(9, 2), is(coord(8,1)));

    }

    @Test
    public void findNearestWhenNearANonLeaf() throws Exception {

        assertThat(kdTree.getNearest(5, 3), is(coord(5,4)));

    }


    @Test
    public void findNearestWhenNearestOnTheOtherSide() throws Exception {

        assertThat(kdTree.getNearest(6.99, 0.01), is(coord(8,1)));

    }

    @Test
    public void findNearestWhenMatch() throws Exception {

        assertThat(kdTree.getNearest(9, 6), is(coord(9,6)));

    }


    @Test
    public void otherfindNearest() throws Exception {

        assertThat(kdTree.getNearest(2, 4.1), is(coord(2,3)));
        assertThat(kdTree.getNearest(0, 10), is(coord(4,7)));
        assertThat(kdTree.getNearest(4, 4), is(coord(5,4)));
        assertThat(kdTree.getNearest(6, 2.5), is(coord(7,2)));
        assertThat(kdTree.getNearest(10, 2), is(coord(8,1)));
        assertThat(kdTree.getNearest(8, 5), is(coord(9,6)));

    }
    @Test
    public void createKdTree() throws Exception {

        assertThat(kdTree.getRoot().getCoordinates(), is(coord(7,2)));
        assertThat(kdTree.getRoot().getLeftChild().getCoordinates(), is(coord(5,4)));
        assertThat(kdTree.getRoot().getRightChild().getCoordinates(), is(coord(9,6)));
        assertThat(kdTree.getRoot().getLeftChild().getLeftChild().getCoordinates(), is(coord(2,3)));
        assertThat(kdTree.getRoot().getLeftChild().getRightChild().getCoordinates(), is(coord(4,7)));
        assertThat(kdTree.getRoot().getRightChild().getLeftChild().getCoordinates(), is(coord(8,1)));
        assertNull(kdTree.getRoot().getRightChild().getRightChild());

    }
}
