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

import lombok.NoArgsConstructor;
import lombok.val;

import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.util.CloseableIterator;

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
@NoArgsConstructor
public class TextFlowStreamDAO extends StreamingDAO<HTextFlow>
{

   public TextFlowStreamDAO(HibernateEntityManagerFactory emf)
   {
      super(emf);
   }

   /**
    * Returns all HTextFlows in all projects, eagerly fetches targets, document, iteration and project.  
    * Obsolete projects, iterations, documents and textflows are skipped.
    * @return
    */
   public @Nonnull CloseableIterator<HTextFlow> findTextFlows()
   {
      val iter = createIterator();
      try
      {
         val q = iter.getSession().createQuery(
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
         q.setComment("TextFlowStreamDAO.findTextFlows");
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
   public @Nonnull CloseableIterator<HTextFlow> findTextFlowsByProject(HProject hProject)
   {
      val iter = createIterator();
      try
      {
         val q = iter.getSession().createQuery(
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
         q.setComment("TextFlowStreamDAO.findTextFlowsByProject");
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
   public @Nonnull CloseableIterator<HTextFlow> findTextFlowsByProjectIteration(HProjectIteration hProjectIteration)
   {
      val iter = createIterator();
      try
      {
         val q = iter.getSession().createQuery(
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
         q.setComment("TextFlowStreamDAO.findTextFlowsByProjectIteration");

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
