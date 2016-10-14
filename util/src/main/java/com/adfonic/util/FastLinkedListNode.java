package com.adfonic.util;

/**
 * Not thread-safe. No concurrent modification checks are ever performed.
 */
public class FastLinkedListNode<T> {
    /* package */FastLinkedListNode<T> previous;
    /* package */FastLinkedListNode<T> next;
    /* package */final T value;

    public FastLinkedListNode(T value) {
        this.value = value;
    }

    public FastLinkedListNode(FastLinkedListNode<T> previous, FastLinkedListNode<T> next, T value) {
        this.previous = previous;
        this.next = next;
        this.value = value;
    }
}
