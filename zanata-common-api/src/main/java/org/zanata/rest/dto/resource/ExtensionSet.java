package org.zanata.rest.dto.resource;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.ExtensionValue;

import javax.annotation.Nonnull;

public class ExtensionSet<T extends ExtensionValue> extends
        AbstractCollection<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private @Nonnull Map<Class<?>, T> extensions = new LinkedHashMap<>();

    @Override
    public Iterator<T> iterator() {
        return extensions.values().iterator();
    }

    @Override
    public int size() {
        return extensions.size();
    }

    @Override
    public boolean add(T e) {
        this.extensions.put(e.getClass(), e);
        return true;
    };

    @SuppressWarnings("unchecked")
    public <E extends T> E findByType(Class<E> clz) {
        return (E) this.extensions.get(clz);
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    public <E extends T> E findOrAddByType(Class<E> clz) {
        E ext = findByType(clz);
        if (ext == null) {
            try {
                ext = clz.newInstance();
                add(ext);
            } catch (Throwable e) {
                throw new RuntimeException("unable to create instance", e);
            }
        }
        return ext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ExtensionSet)) {
            return false;
        }
        ExtensionSet<?> that = (ExtensionSet<?>) o;
        return extensions.equals(that.extensions);
    }

    @Override
    public int hashCode() {
        return extensions.hashCode();
    }

}
