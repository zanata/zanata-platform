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

import lombok.NoArgsConstructor;

import org.hibernate.Query;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.util.CloseableIterator;

/**
 * This class uses Hibernate's StatelessSession to iterate over large queries returning TransMemoryUnit.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Name("transMemoryStreamingDAO")
@Scope(ScopeType.EVENT)
@NoArgsConstructor
public class TransMemoryStreamingDAO extends StreamingDAO<TransMemoryUnit>
{

   public TransMemoryStreamingDAO(HibernateEntityManagerFactory emf)
   {
      super(emf);
   }

   public CloseableIterator<TransMemoryUnit> findTransUnitsByTM(TransMemory transMemory)
   {
      StreamingEntityIterator<TransMemoryUnit> iter = createIterator();
      try
      {
         Query q = iter.getSession().createQuery(
               "from TransMemoryUnit tu " +
               "inner join fetch tu.translationMemory tm " +
               "inner join fetch tu.transUnitVariants " +
               "where tm=:transMemory"
               );
         q.setParameter("transMemory", transMemory);
         q.setComment("TransMemoryStreamingDAO.findTransUnitsByTM");

         iter.setQuery(q);
         return iter;
      }
      catch (Throwable e)
      {
         iter.close();
         throw new RuntimeException(e);
      }

   }

}
