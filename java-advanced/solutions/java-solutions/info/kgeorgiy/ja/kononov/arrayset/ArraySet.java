package info.kgeorgiy.ja.kononov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private final ArrayList<E> arraySet;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(null, null);
    }

    public ArraySet(Comparator<E> comparator) {
        this(null, comparator);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;

        if (collection == null) {
            arraySet = new ArrayList<>();
        } else {
            TreeSet<E> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(collection);
            arraySet = new ArrayList<>(treeSet);
        }
    }

    private ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator, boolean needToSort) {
        arraySet = new ArrayList<>(collection);
        this.comparator = comparator;
    }

    private boolean comparable(E a, E b) {
        if (comparator() != null) {
            return true;
        }

        return a instanceof Comparable && b instanceof Comparable;
    }

    @SuppressWarnings("unchecked")
    private boolean comparableBiggerOrEquals(E a, E b) {
        if (comparable(a, b)) {
            if (comparator != null) {
                return comparator.compare(a, b) >= 0;
            }

            return ((Comparable<E>) a).compareTo(b) >= 0;
        }
        return false;
    }

    private int binarySearch(E element) {
        int index = Collections.binarySearch(arraySet, element, comparator);

        if (index >= 0) return index;

        index = -1 * index - 1;
        return index;
    }

    private boolean correctSegment(int left, int right) {
        return left <= right && left >= 0 && right <= arraySet.size();
    }

    private SortedSet<E> subSet(E fromElement, E toElement, boolean fromStart, boolean toLast) {
        if (fromElement != null && toElement != null && comparableBiggerOrEquals(fromElement, toElement)) {
            throw new IllegalArgumentException("fromKey >= toKey");
        }

        int firstElemIndex, secondElemIndex;
        if (fromStart) {
            firstElemIndex = 0;
            secondElemIndex = binarySearch(toElement);
        } else if (toLast) {
            firstElemIndex = binarySearch(fromElement);
            secondElemIndex = arraySet.size();
        } else {
            firstElemIndex = binarySearch(fromElement);
            secondElemIndex = binarySearch(toElement);
        }

        if (correctSegment(firstElemIndex, secondElemIndex)) {
            return new ArraySet<>(arraySet.subList(firstElemIndex, secondElemIndex), comparator, false);
        }

        return new ArraySet<>(null, comparator);
    }

    private SortedSet<E> getSet(E fromElement, E toElement, boolean head, boolean tail) {
        if (size() > 0) {
            if (head && comparableBiggerOrEquals(toElement, first())) {
                return subSet(null, toElement, true, false);
            }

            if (tail && comparableBiggerOrEquals(last(), fromElement)) {
                return subSet(fromElement, null, false, true);
            }
        }
        return new ArraySet<>(null, comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(arraySet, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(arraySet).iterator();
    }

    @Override
    public int size() {
        return arraySet.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, toElement, false, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return getSet(null, toElement, true, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return getSet(fromElement, null, false, true);
    }

    @Override
    public E first() {
        if (size() > 0) {
            return arraySet.get(0);
        }

        throw new NoSuchElementException();
    }

    @Override
    public E last() {
        if (size() > 0) {
            return arraySet.get(size() - 1);
        }

        throw new NoSuchElementException();
    }
}
