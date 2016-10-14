package com.adfonic.geo.kdtree;

import com.adfonic.geo.Coordinates;

public class PriorityNode<T extends Coordinates> {

    BNode<T> node;
    double distance;

    public PriorityNode(BNode<T> node, double distance) {
        this.node = node;
        this.distance = distance;
    }

    public BNode<T> getNode() {
        return node;
    }

    public double getDistance() {
        return distance;
    }
}
