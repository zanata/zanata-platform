/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.dao;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.hibernate.ScrollableResults;

class ScrollableResultsIterator implements Iterator<Object[]>, Closeable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ScrollableResultsIterator.class);
    private boolean closed;
    private Object[] nextRow = null;
    private final ScrollableResults scrollableResults;

    public ScrollableResultsIterator(ScrollableResults scrollableResults) {
        this.scrollableResults = scrollableResults;
    }

    @Override
    public void close() {
        if (!closed) {
            scrollableResults.close();
            closed = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    @Override
    public boolean hasNext() {
        try {
            if (nextRow != null) {
                return true;
            }
            if (closed) {
                return false;
            }
            if (scrollableResults.next()) {
                nextRow = scrollableResults.get();
                return true;
            } else {
                close();
                return false;
            }
        } catch (RuntimeException e) {
            // UGLY hack to work around
            // https://hibernate.atlassian.net/browse/HHH-2811
            if (e.getMessage() != null && (e.getMessage().contains(
                    "could not perform sequential read of results (forward)")
                    || e.getMessage().contains(
                            "could not doAfterTransactionCompletion sequential read of results (forward)"))) {
                log.debug("assuming empty ResultSet", e);
                close();
                return false;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Object[] next() {
        if (hasNext()) {
            Object[] next = nextRow;
            nextRow = null;
            return next;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
