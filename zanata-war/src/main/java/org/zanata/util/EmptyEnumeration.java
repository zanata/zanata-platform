package org.zanata.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EmptyEnumeration<E> implements Enumeration<E> {
    private static final EmptyEnumeration<Object> INSTANCE
            = new EmptyEnumeration<Object>();
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
