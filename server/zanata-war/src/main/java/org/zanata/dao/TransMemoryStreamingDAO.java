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

import org.hibernate.Query;
import org.hibernate.ejb.HibernateEntityManagerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.util.CloseableIterator;
import org.zanata.util.Zanata;

/**
 * This class uses Hibernate's StatelessSession to iterate over large queries
 * returning TransMemoryUnit.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("transMemoryStreamingDAO")
@RequestScoped
/**
 * Note: unless the find* methods throw an exception, the caller is responsible
 * for closing the Iterator, or a database connection may leak.
 */
public class TransMemoryStreamingDAO extends StreamingDAO<TransMemoryUnit> {

    public TransMemoryStreamingDAO() {
        this(null);
    }

    @Inject
    public TransMemoryStreamingDAO(@Zanata HibernateEntityManagerFactory emf) {
        super(emf);
    }

    /**
     * Finds all the TransMemoryUnits for a given TransMemory.
     * <p>
     * NB: caller must close the iterator, or call next() until the iterator is
     * exhausted, or else a database connection will be leaked.
     *
     * @param transMemory
     * @return
     */
    public CloseableIterator<TransMemoryUnit> findTransUnitsByTM(
            TransMemory transMemory) {
        StreamingEntityIterator<TransMemoryUnit> iter = createIterator();
        try {
            Query q =
                    iter.getSession()
                            .createQuery(
                                    "FROM TransMemoryUnit tu FETCH ALL PROPERTIES "
                                            + "JOIN FETCH tu.transUnitVariants tuv FETCH ALL PROPERTIES "
                                            + "WHERE tu.translationMemory = :transMemory "
                                            + "");
            q.setParameter("transMemory", transMemory);
            q.setComment("TransMemoryStreamingDAO.findTransUnitsByTM");

            iter.initQuery(q);
            return iter;
        } catch (Throwable e) {
            iter.close();
            throw new RuntimeException(e);
        }

    }

    /**
     * Finds all TransMemoryUnits.
     * <p>
     * NB: caller must close the iterator, or call next() until the iterator is
     * exhausted, or else a database connection will be leaked.
     *
     * @param transMemory
     * @return
     */
    public CloseableIterator<TransMemoryUnit> findAllTransUnits() {
        StreamingEntityIterator<TransMemoryUnit> iter = createIterator();
        try {
            Query q =
                    iter.getSession()
                            .createQuery(
                                    "FROM TransMemoryUnit tu FETCH ALL PROPERTIES "
                                            + "JOIN FETCH tu.transUnitVariants tuv FETCH ALL PROPERTIES "
                                            + "");
            q.setComment("TransMemoryStreamingDAO.findAllTransUnits");
            iter.initQuery(q);
            return iter;
        } catch (Throwable e) {
            iter.close();
            throw new RuntimeException(e);
        }
    }

}
