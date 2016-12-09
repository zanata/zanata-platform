/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface CloseableIterator<E> extends Iterator<E>, Closeable {
    /**
     * Decorates an ordinary Iterator to satisfy the CloseableIterator
     * interface. Note: this should only be used for ordinary iterators
     * (eg iterating a collection in memory), because it doesn't actually
     * close anything.
     * @param iterator the ordinary iterator to wrap
     * @param <E> the type of elements returned by this iterator
     * @return the decorated Iterator
     */
    static <E> CloseableIterator<E> closeable(Iterator<E> iterator) {
        return new CloseableIterator<E>() {
            @Override
            public void close() throws IOException {
                // do nothing
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }
        };
    }
}
