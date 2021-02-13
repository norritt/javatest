package com.company;

import javax.management.InvalidAttributeValueException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class SuperIterator implements Iterator<Integer> {
    private static final class InternalIterator implements Iterator<Integer> {
        private final Iterator<Integer> iterator;
        private Integer lastValue;
        private boolean hasValue;

        public InternalIterator(Iterator<Integer> iterator) {
            this.iterator = iterator;
        }

        public boolean hasValue() { return hasValue; }
        public Integer getLastValue() throws InvalidAttributeValueException {
            if (hasValue) {
                return lastValue;
            }
            throw new InvalidAttributeValueException();
        }
        public Integer next() throws NoSuchElementException {
            lastValue = iterator.next();
            hasValue = true;
            return lastValue;
        }
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }

    private final List<InternalIterator> pool = new ArrayList<>();

    public SuperIterator(Iterable<Iterator<Integer>> iterators) {
        iterators.forEach(iter -> this.pool.add(new InternalIterator(iter)));
    }

    @Override
    public boolean hasNext() {
        return !pool.isEmpty();
    }

    @Override
    public Integer next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        for (var iter: pool) {
            if (!iter.hasValue()) {
                iter.next();
            }
        }
        pool.sort((val1, val2) -> {
            try {
                return val1.getLastValue().compareTo(val2.getLastValue());
            } catch (InvalidAttributeValueException e) {
                throw new NoSuchElementException(e.toString());
            }
        });
        var it = pool.iterator();
        if (!it.hasNext())
        {
            throw new NoSuchElementException("iterators are over");
        }
        var minimal = it.next();
        try {
            var val = minimal.getLastValue();
            pool.removeIf(iter -> !iter.hasNext());
            if (minimal.hasNext()) {
                minimal.next();
            }
            return val;
        } catch (InvalidAttributeValueException e) {
            throw new NoSuchElementException(e.toString());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        var first = List.of(1, 5, 7, 23, 33, 35, 45, 66, 345, 634).iterator();
        var second = List.of(3, 4, 9, 12, 12, 15, 33, 35, 634, 654, 788).iterator();
        var expected = List.of(1, 3, 4, 5, 7, 9, 12, 12, 15, 23, 33, 33, 35, 35, 45, 66, 345, 634, 634, 654).iterator();
//        var first = List.of(1).iterator();
//        var second = List.of(3, 4, 5, 7).iterator();
//        var expected = List.of(1, 3, 4, 5, 7).iterator();
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
