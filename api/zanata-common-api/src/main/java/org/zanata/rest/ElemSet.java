package org.zanata.rest;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// NB don't add state in subclasses, or you will break the equals method
public abstract class ElemSet<T> implements Set<T> {

    private final Set<T> impl;

    protected abstract T valueOfElem(String value);

    public ElemSet(String values) {
        impl = new HashSet<T>();
        if (values != null) {
            Iterable<String> splitValues = Splitter.on(';').split(values);
            for (String val : splitValues) {
                T elem = valueOfElem(val);
                add(elem);
            }
        }
    }

    @Override
    public boolean add(T e) {
        return impl.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return impl.addAll(c);
    }

    @Override
    public void clear() {
        impl.clear();
    }

    @Override
    public boolean contains(Object o) {
        return impl.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return impl.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return impl.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return impl.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return impl.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return impl.retainAll(c);
    }

    @Override
    public int size() {
        return impl.size();
    }

    @Override
    public Object[] toArray() {
        return impl.toArray();
    }

    @Override
    @SuppressWarnings("all")
    public <T> T[] toArray(T[] a) {
        return impl.toArray(a);
    }

    @Override
    public String toString() {
        return Joiner.on(';').join(this);
    }

    // method is final because equals is final
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((impl == null) ? 0 : impl.hashCode());
        return result;
    }

    // method is final to enforce transitive mixed-type equality (we want
    // Collections-style equals semantics).
    // see
    // http://www.angelikalanger.com/Articles/JavaSolutions/SecretsOfEquals/Equals.html
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ElemSet)) {
            return false;
        }
        ElemSet<?> other = (ElemSet<?>) obj;
        if (impl == null) {
            if (other.impl != null) {
                return false;
            }
        } else if (!impl.equals(other.impl)) {
            return false;
        }
        return true;
    }

}
