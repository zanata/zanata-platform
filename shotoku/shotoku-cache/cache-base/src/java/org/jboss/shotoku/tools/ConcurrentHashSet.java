/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.shotoku.tools;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Collection;

/**
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class ConcurrentHashSet<K> implements ConcurrentSet<K> {
    private ConcurrentMap<K, Boolean> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<K, Boolean>();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public Iterator<K> iterator() {
        return map.keySet().iterator();
    }

    public Object[] toArray() {
        return map.keySet().toArray();
    }

    public <T>T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    public boolean add(K o) {
        return map.putIfAbsent(o, Boolean.TRUE) == null;
    }

    public boolean remove(Object o) {
        return map.keySet().remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    public boolean addAll(Collection<? extends K> c) {
        boolean ret = false;
        for (K o : c) {
            ret |= add(o);
        }

        return ret;
    }

    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        for (Object o : c) {
            if (!map.containsKey(o)) {
                map.remove(o);
            }
        }

        return ret;
    }

    public boolean removeAll(Collection<?> c) {
        boolean ret = false;
        for (Object o : c) {
            ret |= remove(o);
        }

        return ret;
    }

    public void clear() {
        map.clear();
    }
}
