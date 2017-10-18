package org.zanata.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EmptyEnumeration<E> implements Enumeration<E> {
    private static final EmptyEnumeration<?> INSTANCE =
            new EmptyEnumeration<>();

    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> instance() {
        return (Enumeration<T>) INSTANCE;
    }

    @Override
    public boolean hasMoreElements() {
        return false;
    }

    @Override
    public E nextElement() {
        throw new NoSuchElementException();
    }
}
