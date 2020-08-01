import java.util.LinkedList;


public class ArrayDequeSolution<Item> extends LinkedList<Item> {
    public void printDeque() {
        System.out.println("dummy");
    }

    public Item getRecursive(int i) {
        return get(i);
    }

    public Item removeFirst() {
        try {
            return super.removeFirst();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Item removeLast() {
        try {
            return super.removeLast();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
