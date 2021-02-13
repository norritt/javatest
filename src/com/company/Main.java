package com.company;

import java.text.MessageFormat;
import java.util.*;

class SuperIterator implements Iterator<Integer> {
    private final List<Iterator<Integer>> pool = new ArrayList<>();
    private TreeMap<Integer, Queue<Iterator<Integer>>> iteratorMap = null;

    public SuperIterator(Iterable<Iterator<Integer>> iterators) {
        iterators.forEach(this.pool::add);
    }

    private void initializeMap() {
        iteratorMap = new TreeMap<>();
        pool.forEach(iter -> {
            if (iter.hasNext()) {
                addIteratorToMap(iter);
            }
        });
    }

    private void addIteratorToMap(Iterator<Integer> iter) {
        var val = iter.next();
        Queue<Iterator<Integer>> result = new ArrayDeque<>();
        if (!iteratorMap.containsKey(val)) {
            iteratorMap.put(val, result);
        }
        var ret = iteratorMap.get(val);
        ret.add(iter);
    }

    @Override
    public boolean hasNext() {
        if (iteratorMap == null) {
            initializeMap();
        }
        return iteratorMap != null && iteratorMap.size() != 0;
    }

    @Override
    public Integer next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var entry = iteratorMap.firstEntry();
        var iter = getIteratorFromMap(entry);
        if (iter == null) {
            throw new NoSuchElementException();
        }
        if (iter.hasNext()) {
            addIteratorToMap(iter);
        }
        return entry.getKey();
    }

    private Iterator<Integer> getIteratorFromMap(Map.Entry<Integer, Queue<Iterator<Integer>>> entry) {
        var queue = entry.getValue();
        var iter = queue.poll();
        if (queue.isEmpty()) {
            iteratorMap.pollFirstEntry();
        }
        return iter;
    }
}

public class Main {
    public static void main(String[] args) {
        var first = List.of(1, 5, 7, 23, 33, 35, 45, 66, 345, 634).iterator();
        var second = List.of(1, 3, 4, 9, 12, 12, 15, 33, 35, 634, 654, 788).iterator();
        var expected = List.of(1, 1, 3, 4, 5, 7, 9, 12, 12, 15, 23, 33, 33, 35, 35, 45, 66, 345, 634, 634, 654).iterator();
//        var first = List.of(1).iterator();
//        var second = List.of(3, 4, 5, 7).iterator();
//        var expected = List.of(1, 3, 4, 5, 7).iterator();
//        var first = List.of(1).iterator();
//        var second = List.of(2).iterator();
//        var expected = List.of(1, 2).iterator();
        var sup = new SuperIterator(List.of(first, second));
        while (expected.hasNext() && sup.hasNext()) {
            int expVal = expected.next();
            int gotVal = sup.next();
            assert(expVal == gotVal);
            System.out.println(MessageFormat.format("expected = {0}, got = {1}", expVal, gotVal));
        }
        System.out.println(MessageFormat.format("expected = {0}, got = {1}", expected.hasNext(), sup.hasNext()));
        assert (expected.hasNext() == sup.hasNext());
    }
}
