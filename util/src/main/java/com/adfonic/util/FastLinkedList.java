package com.adfonic.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Not thread-safe. No concurrent modification checks are ever performed. No
 * range check ever performed. Most list ops will throw
 * UnsupportedOperationException. Can only iterate from head to tail, in one
 * direction. Just don't do anything stupid, and in return you get peak
 * performance.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
public class FastLinkedList<T> implements List<T> {
    
    private static final String UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE = "Don't try to get all fancy with this optimized list impl.";
    
    private FastLinkedList<T> removalReceiver;
    private FastLinkedListNode<T> head;
    private FastLinkedListNode<T> tail;
    private int size = 0;

    public FastLinkedList() {
    }

    /**
     * Constructor that uses a removal receiver. Whenever an element of this
     * list is removed, it will be added to the removal receiver list. This is
     * useful if you want to manage a pool of "free instances," for example.
     */
    public FastLinkedList(FastLinkedList<T> removalReceiver) {
        this.removalReceiver = removalReceiver;
    }

    @Override
    public boolean add(T value) {
        addNode(new FastLinkedListNode<T>(value));
        return true;
    }

    public void addNode(FastLinkedListNode<T> node) {
        addNode(node, 1);
    }

    public void addNode(FastLinkedListNode<T> node, int numAdding) {
        if (head == null) {
            head = node;
            node.previous = null;
        } else {
            tail.next = node;
            node.previous = tail;
        }

        tail = node;

        if (numAdding == 1) {
            // We're only adding one node, so make sure its "next" is nulled
            // out.
            node.next = null;
        } else {
            // Otherwise leave all the downstream links intact and update "tail"
            while (tail.next != null) {
                tail = tail.next;
            }
        }

        size += numAdding;
    }

    @Override
    public void clear() {
        if (size > 0 && removalReceiver != null) {
            // We need to make sure all elements get added to the removal
            // receiver
            removalReceiver.addNode(head, size);
        }
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new FastLinkedListIterator<T>(this);
    }

    public T removeNode(FastLinkedListNode<T> node) {
        if (head == node) {
            // The first node is being removed
            head = node.next;
            // Null out the head's previous node if head isn't null
            if (head != null) {
                head.previous = null;
            }
            // No else here, since if head is now null, both it and tail
            // will get updated as of the next add() call. This admittedly
            // has the potential to hold a reference to the removed value,
            // since "tail" still references it, but every instruction saved
            // translates to better performance. And it's assumed that this
            // list will be either finalized or reused and added to almost
            // immediately...which means tail will get updated and the
            // reference will be released.
        } else if (tail == node) {
            // The last node is being removed
            tail = node.previous;
            // Null out the next node on the new tail.
            // We don't need to null check tail, since it can't possibly
            // be null here...if tail was head, then it would have been
            // caught by the head removal check above.
            tail.next = null;
        } else {
            // A middle node is being removed
            node.next.previous = node.previous;
            node.previous.next = node.next;
        }

        // If we have a removal receiver, make sure it receives our discards
        if (removalReceiver != null) {
            removalReceiver.add(node.value);
        }

        --size;

        return node.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void add(int index, T value) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public T remove(int index) {
        if (index == 0) {
            if (head == null) {
                throw new IndexOutOfBoundsException("Attempt to remove from an empty list");
            } else {
                return removeNode(head);
            }
        } else {
            throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public T set(int index, T value) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
    }

    
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int idx = 0;
        for (Iterator iter = iterator(); iter.hasNext();) {
            array[idx++] = iter.next();
        }
        return array;
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        if (a == null || a.length < size) {
            throw new UnsupportedOperationException(UNSUPPORTED_OPEARTION_EXCEPTION_MESSAGE);
        }
        int idx = 0;
        for (Iterator iter = iterator(); iter.hasNext();) {
            a[idx++] = (T) iter.next();
        }
        return a;
    }

    /**
     * Totally not thread-safe. Doesn't check for concurrent modification.
     * Totally unsafe to call remove() prior to calling next() at least once.
     */
    private static final class FastLinkedListIterator<T> implements Iterator<T> {
        private final FastLinkedList<T> list;
        private FastLinkedListNode<T> currentNode;

        public FastLinkedListIterator(FastLinkedList<T> list) {
            this.list = list;
            currentNode = new FastLinkedListNode<T>(null, list.head, null);
        }

        @Override
        public boolean hasNext() {
            return currentNode.next != null;
        }

        @Override
        public T next() {
            if (currentNode.next==null){
                throw new NoSuchElementException();
            }
            currentNode = currentNode.next;
            return currentNode.value;
        }

        @Override
        public void remove() {
            list.removeNode(currentNode);
        }
    }
}
