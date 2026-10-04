package dsa;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class SinglyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        final T data;
        Node<T> next;

        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;


    public void add(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override public boolean hasNext() { return current != null; }

            @Override public T next() {
                if (current == null) throw new NoSuchElementException();
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
}