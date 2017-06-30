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

import javax.annotation.Nonnull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;

/**
 * This class uses Hibernate's StatelessSession to iterate over large query
 * results using mysql streaming ResultSets.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public abstract class StreamingDAO<T> implements Serializable {

    private SessionFactory sessionFactory;

    public StreamingDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Note: unless this method throws an exception, the caller is responsible
     * for closing the Iterator, or a database connection may leak.
     *
     * @return a CloseableIterator
     */
    StreamingEntityIterator<T> createIterator() {
        @SuppressWarnings("null")
        @Nonnull
        Session session =
                sessionFactory.openSession();
        try {
            return new StreamingEntityIterator<T>(session);
        } catch (Throwable e) {
            session.close();
            throw new RuntimeException(e);
        }
    }

}
