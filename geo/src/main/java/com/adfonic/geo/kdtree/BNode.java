package com.adfonic.geo.kdtree;

import com.adfonic.geo.Coordinates;

public class BNode<T extends Coordinates> {

    private T coordinates;
    private BNode<T> leftChild;
    private BNode<T> rightChild;
    private SplitAxis axis;
    private BNode<T> parent = null;

    public BNode(T coordinates, BNode<T> leftChild, BNode<T> rightChild, SplitAxis axis) {
        this.coordinates = coordinates;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.axis = axis;
        
        if (leftChild != null){
            this.leftChild.parent = this;
        }
        
        if (rightChild != null){
            this.rightChild.parent = this;
        }

    }

    public T getCoordinates() {
        return coordinates;
    }

    public BNode<T> getLeftChild() {
        return leftChild;
    }

    public BNode<T> getRightChild() {
        return rightChild;
    }

    public BNode<T> getParent() {
        return parent;
    }


    public SplitAxis getAxis() {
        return axis;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        BNode bNode = (BNode) o;

        if (axis != bNode.axis) {
            return false;
        }
        if (!coordinates.equals(bNode.coordinates)){
            return false;
        }
        if (leftChild != null ? !leftChild.equals(bNode.leftChild) : bNode.leftChild != null){
            return false;
        }
        if (parent != null ? !parent.equals(bNode.parent) : bNode.parent != null){
            return false;
        }
        if (rightChild != null ? !rightChild.equals(bNode.rightChild) : bNode.rightChild != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = coordinates.hashCode();
        result = 31 * result + (leftChild != null ? leftChild.hashCode() : 0);
        result = 31 * result + (rightChild != null ? rightChild.hashCode() : 0);
        result = 31 * result + axis.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BNode{" +
               "coordinates=" + coordinates +
               ", axis=" + axis +
               '}';
    }

    public boolean samePlace(BNode<T> node) {
        if (node == null){
            return false;
        }
        return this.coordinates.getLatitude() == node.coordinates.getLatitude() && this.coordinates.getLongitude() == node.coordinates.getLongitude();
    }
}
