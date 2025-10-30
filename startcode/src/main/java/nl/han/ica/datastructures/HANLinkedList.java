package nl.han.ica.datastructures;

import java.util.NoSuchElementException;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private ListNode<T> head;
    private int size;

    /**
     * Adds value to the front of the list
     *
     * @param value generic value to be added
     */
    @Override
    public void addFirst(T value) {
        if (this.head == null) {
            this.head = new ListNode<>(value);
        } else {
            ListNode<T> newNode = new ListNode<>(value);
            newNode.setNext(this.head);
            this.head = newNode;
        }
        this.size++;
    }

    /**
     * Clears list. Size equals 0 afterwards
     */
    @Override
    public void clear() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Adds value to index position
     *
     * @param index the position
     * @param value the value to add at index
     */
    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            addFirst(value);
            return;
        }

        ListNode<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.getNext();
        }

        ListNode<T> newNode = new ListNode<>(value);
        newNode.setNext(current.getNext());
        current.setNext(newNode);
        size++;
    }

    /**
     * Deletes value at position
     *
     * @param pos position where value is deleted
     */
    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Index: " + pos + ", Size: " + size);
        }

        if (pos == 0) {
            removeFirst();
            return;
        }

        ListNode<T> current = this.head;
        for (int i = 0; i < pos - 1; i++) {
            current = current.getNext();
        }
        current.setNext(current.getNext().getNext());
        size--;
    }

    /**
     * Returns generic value T at postion
     *
     * @param pos position to look up value
     * @return value at position pos
     */
    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException("Index: " + pos + ", Size: " + size);
        }

        if (pos == 0) {
            return getFirst(); //!!!!! head.getData();
        }

        ListNode<T> current = this.head;
        for (int i = 0; i < pos; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    /**
     * Removes first element
     */
    @Override
    public void removeFirst() {
        if (this.head == null) {
            throw new NoSuchElementException();
        }

        this.head = this.head.getNext();
        size--;

    }

    /**
     * Returns first element in O(n) time
     *
     * @return first element
     */
    @Override
    public T getFirst() {
        if (this.head == null) {
            throw new NoSuchElementException();
        }
        return this.head.getData();
    }

    /**
     * Determines size of the list, equals the number of stored items but not
     * the header node
     *
     * @return number of items in list
     */
    @Override
    public int getSize() {
        return this.size;
    }

}
