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

import javax.annotation.Nonnull;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;

/**
 * This class uses Hibernate's StatelessSession to iterate over large queries returning HTextFlow.
 * Each of the public methods should have a variant which accepts a locale parametor, but until
 * HTextFlow.getTargetContents(LocaleId) can be implemented efficiently, we don't need them.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Name("textFlowStreamDAO")
@Scope(ScopeType.EVENT)

// TODO queries should only return Translated/Approved TFTs
// TODO build related queries using querydsl
public class TextFlowStreamDAO
{

   @In
   private HibernateEntityManagerFactory entityManagerFactory;
   // MIN_VALUE gives a hint to mysql JDBC driver to stream results instead of keeping everything in memory.
   // Note that this will tie up the JDBC connection until the ResultSet is closed.
   // See http://www.numerati.com/2012/06/26/reading-large-result-sets-with-hibernate-and-mysql/
   // and http://dev.mysql.com/doc/refman/5.5/en/connector-j-reference-implementation-notes.html
   private int fetchSize = Integer.MIN_VALUE;

   public TextFlowStreamDAO()
   {
   }

   public TextFlowStreamDAO(HibernateEntityManagerFactory emf)
   {
      this.entityManagerFactory = emf;
   }

   class HTextFlowIterator implements Iterator<HTextFlow>, Closeable
   {
      private final @Nonnull Session session;
      private int rowNum;
      private ScrollableResultsIterator srIter;

      public HTextFlowIterator(@Nonnull Session session)
      {
         this.session = session;
      }

      @Override
      public void close()
      {
         if (srIter != null)
         {
            srIter.close();
         }
         session.close();
      }

      @Override
      protected void finalize() throws Throwable
      {
         close();
      }

      @Override
      public boolean hasNext()
      {
         return srIter.hasNext();
      }

      @Override
      public HTextFlow next()
      {
         if (++rowNum % 1000 == 0) session.clear();
         HTextFlow tf = (HTextFlow) srIter.next()[0];
         session.evict(tf);
         return tf;
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException();
      }

      public void setQuery(Query q)
      {
         q.setFetchSize(fetchSize);
         q.setReadOnly(true);
         ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY);
         srIter = new ScrollableResultsIterator(scroll);
      }

   }

   private HTextFlowIterator createIterator()
   {
      @SuppressWarnings("null")
      @Nonnull Session session = entityManagerFactory.getSessionFactory().openSession();
      try
      {
         return new HTextFlowIterator(session);
      }
      catch (Throwable e)
      {
         session.close();
         throw new RuntimeException(e);
      }
   }

   /**
    * Returns all HTextFlows in all projects, eagerly fetches targets, document, iteration and project.  
    * Obsolete projects, iterations, documents and textflows are skipped.
    * @return
    */
   public @Nonnull Iterator<HTextFlow> findTextFlows()
   {
      HTextFlowIterator iter = createIterator();
      try
      {
         Query q = iter.session.createQuery(
               "from HTextFlow tf " + 
               "inner join fetch tf.targets target " + 
               "inner join fetch target.locale " + 
               "inner join fetch tf.document " + 
               "inner join fetch tf.document.locale " + 
               "inner join fetch tf.document.projectIteration " + 
               "inner join fetch tf.document.projectIteration.project " + 
               "where tf.document.projectIteration.project.status<>:OBSOLETE " + 
               "and tf.document.projectIteration.status<>:OBSOLETE " + 
               "and tf.document.obsolete=0 " + 
               "and tf.obsolete=0 "
               );
         q.setParameter("OBSOLETE", EntityStatus.OBSOLETE);
         q.setComment("TextFlowStatelessDAO.findTextFlows");
         iter.setQuery(q);
         return iter;
      }
      catch (Throwable e)
      {
         iter.close();
         throw new RuntimeException(e);
      }
   }

   /**
    * Returns all HTextFlows in project, eagerly fetches targets, document, iteration and project.
    * Obsolete iterations, documents and textflows are skipped.
    * @return
    */
   public @Nonnull Iterator<HTextFlow> findTextFlowsByProject(HProject hProject)
   {
      HTextFlowIterator iter = createIterator();
      try
      {
         Query q = iter.session.createQuery(
               "from HTextFlow tf " + 
               "inner join fetch tf.targets target " + 
               "inner join fetch target.locale " + 
               "inner join fetch tf.document " + 
               "inner join fetch tf.document.locale " + 
               "inner join fetch tf.document.projectIteration " + 
               "inner join fetch tf.document.projectIteration.project " + 
               "where tf.document.projectIteration.status<>:OBSOLETE " + 
               "and tf.document.obsolete=0 " + 
               "and tf.obsolete=0" +
               "and tf.document.projectIteration.project=:proj"
               );
         q.setParameter("OBSOLETE", EntityStatus.OBSOLETE);
         q.setParameter("proj", hProject);
         q.setComment("TextFlowStatelessDAO.findTextFlowsByProject");
         iter.setQuery(q);
         return iter;
      }
      catch (Throwable e)
      {
         iter.close();
         throw new RuntimeException(e);
      }
   }

   /**
    * Returns all HTextFlows in project iteration, eagerly fetches targets, document, iteration and project.
    * Obsolete documents and textflows are skipped.
    * @return
    */
   public @Nonnull Iterator<HTextFlow> findTextFlowsByProjectIteration(HProjectIteration hProjectIteration)
   {
      HTextFlowIterator iter = createIterator();
      try
      {
         Query q = iter.session.createQuery(
               "from HTextFlow tf " + 
               "inner join fetch tf.targets target " + 
               "inner join fetch target.locale " + 
               "inner join fetch tf.document " + 
               "inner join fetch tf.document.locale " + 
               "inner join fetch tf.document.projectIteration " + 
               "inner join fetch tf.document.projectIteration.project " + 
               "where tf.document.obsolete=0 " + 
               "and tf.obsolete=0" +
               "and tf.document.projectIteration=:iter"
               );
         q.setParameter("iter", hProjectIteration);
         q.setComment("TextFlowStatelessDAO.findTextFlowsByProjectIteration");

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
