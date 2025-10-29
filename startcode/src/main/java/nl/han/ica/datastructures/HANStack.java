package nl.han.ica.datastructures;

/**
 * Simple stack implementation backed by the provided HANLinkedList.
 */
public class HANStack<T> implements IHANStack<T> {

    private final HANLinkedList<T> linkedList;

    public HANStack() {
        linkedList = new HANLinkedList<>();
    }


    /**
     * pushes value T to the top of the stack
     * @param value value to push
     */
    @Override
    public void push(T value) {
        linkedList.addFirst(value);
    }

    /**
     * Pops (and removes) value at top of stack
     * @return popped value
     */
    @Override
    public T pop() {
        T value = linkedList.getFirst();
        linkedList.removeFirst();
        return value;
    }

    /**
     * Peeks at the top of the stack. Does not remove anything
     * @return value at the top of the stack
     */
    @Override
    public T peek() {
        return linkedList.getFirst();
    }
}