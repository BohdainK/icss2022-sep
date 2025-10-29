package nl.han.ica.datastructures;

public class ListNode<T> {
    private T value;
    private ListNode<T> next;

    ListNode(T value) {
        this.value = value;
        this.next = null;
    }

    public T getData() {
        return value;
    }

    public void setData(T value) {
        this.value = value;
    }

    public ListNode<T> getNext() {
        return next;
    }

    public void setNext(ListNode<T> next) {
        this.next = next;
    }
}
