package com.adfonic.geo.kdtree;

import static com.adfonic.geo.GeoUtils.distanceBetween;

import com.adfonic.geo.Coordinates;

public class FindNearest<T extends Coordinates> {
    private final double latitude;
    private final double longitude;

    public FindNearest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PriorityNode<T> findNearestOnTree(BNode<T> node) {
        BNode<T> currentBestNode = findNearestLeaf(node);

        return unwindsRecursion(currentBestNode, null, null);
    }

    private PriorityNode<T> unwindsRecursion(BNode<T> currentNode, PriorityNode<T> currentBest, BNode<T> stopNode) {

        PriorityNode<T> newBest = compareCurrentWithBest(currentNode, currentBest);

        BNode<T> parent = currentNode.getParent();

        if (parent == null || parent.samePlace(stopNode)) {
            return newBest;
        }


        double axisDistance = getAxisDistance(currentNode, parent);

        if (axisDistance < newBest.getDistance()) {
            newBest = getBestNodeOnOtherSide(currentNode, newBest, parent);
        }

        return unwindsRecursion(parent, newBest, stopNode);

    }

    private PriorityNode<T> getBestNodeOnOtherSide(BNode<T> currentNode, PriorityNode<T> newBest, BNode<T> parent) {
        BNode<T> otherSideNode = chooseOtherSide(currentNode, parent);

        if (otherSideNode == null) {
            return newBest;
        }

        BNode<T> nearestOtherSideLeaf = findNearestLeaf(otherSideNode);
        PriorityNode<T> otherSideNearest = unwindsRecursion(nearestOtherSideLeaf, newBest, parent);

        if (otherSideNearest.getDistance() < newBest.getDistance()) {
            return otherSideNearest;
        }

        return newBest;
    }

    private BNode<T> chooseOtherSide(BNode<T> currentNode, BNode<T> parent) {
        return parent.getLeftChild() == currentNode ? parent.getRightChild() : parent.getLeftChild();
    }

    private double getAxisDistance(BNode<T> currentNode, BNode<T> parent) {
        double axisDistance;
        if (currentNode.getAxis() == SplitAxis.LONGITUDE_AXIS) {
            axisDistance = distanceBetween(parent.getCoordinates().getLatitude(), longitude, latitude, longitude);
        } else {
            axisDistance = distanceBetween(latitude, parent.getCoordinates().getLongitude(), latitude, longitude);
        }
        return axisDistance;
    }

    private PriorityNode<T> compareCurrentWithBest(BNode<T> currentNode, PriorityNode<T> currentBest) {
        double currentDistance = distanceFromNode(currentNode);
//        System.out.println("unwindsRecursion " + currentNode.getCoordinates() + "  current best " + currentBestDistance + "    current node distance " + currentDistance);


        PriorityNode<T> newBest;
        if (currentBest == null || currentDistance < currentBest.getDistance()) {
            newBest = new PriorityNode<T>(currentNode, currentDistance);
        } else {
            newBest = currentBest;
        }
        return newBest;
    }

    private BNode<T> findNearestLeaf(BNode<T> node) {
        if (node == null) {
            return node;
        }
        BNode<T> next = getNextIfZeroOrOneLeafLeft(node);

        if (next != null) { 
            return next; 
        }

        if (node.getAxis() == SplitAxis.LATITUDE_AXIS) {
            next = latitude < node.getCoordinates().getLatitude() ? node.getLeftChild() : node.getRightChild();
        } else {
            next = longitude < node.getCoordinates().getLongitude() ? node.getLeftChild() : node.getRightChild();
        }
        return findNearestLeaf(next);
    }

    private BNode<T> getNextIfZeroOrOneLeafLeft(BNode<T> root) {

        if (root.getLeftChild() == null) {
            if (root.getRightChild() == null) {
                return root;
            } else {
                return root.getRightChild();
            }
        }

        if (root.getRightChild() == null) {
            return root.getLeftChild();
        }
        return null;
    }


    private double distanceFromNode(BNode<T> node) {
        if (node == null) {
            return Double.MAX_VALUE;
        }
        return distanceBetween(node.getCoordinates().getLatitude(), node.getCoordinates().getLongitude(), latitude, longitude);
    }
}
