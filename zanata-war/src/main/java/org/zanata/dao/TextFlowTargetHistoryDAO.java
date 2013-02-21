/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;


@Name("textFlowTargetHistoryDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowTargetHistoryDAO extends AbstractDAOImpl<HTextFlowTargetHistory, Long>
{
   
   public TextFlowTargetHistoryDAO()
   {
      super(HTextFlowTargetHistory.class);
   }

   public TextFlowTargetHistoryDAO(Session session)
   {
      super(HTextFlowTargetHistory.class, session);
   }

   public boolean findContentInHistory(HTextFlowTarget target, List<String> contents)
   {
      Query query;
      
      // use named queries for the smaller more common cases
      if( contents.size() <= 6 )
      {
         query = getSession().getNamedQuery(HTextFlowTargetHistory.getQueryNameMatchingHistory(contents.size()));
      }
      else
      {
         StringBuilder queryStr = new StringBuilder("select count(*) from HTextFlowTargetHistory t where t.textFlowTarget = ? and size(t.contents) = ?");
         for( int i=0; i<contents.size(); i++ )
         {
            queryStr.append(" and contents[" + i + "] = ?");
         }
         query = getSession().createQuery(queryStr.toString());
      }
      query.setParameter(0, target);
      query.setParameter(1, contents.size());
      int paramPos = 2;
      for( String c : contents )
      {
         query.setParameter(paramPos++, c);
      }
      query.setComment("TextFlowTargetHistoryDAO.findContentInHistory-"+contents.size());
      return (Long)query.uniqueResult() != 0;
   }

   public boolean findConflictInHistory(HTextFlowTarget target, Integer verNum, String username)
   {
      Query query = getSession().createQuery("select count(*) from HTextFlowTargetHistory t where t.textFlowTarget.id =:id and t.textFlowRevision > :ver and t.lastModifiedBy.account.username != :username");
      query.setParameter("id", target.getId());
      query.setParameter("ver", verNum);
      query.setParameter("username", username);
      query.setComment("TextFlowTargetHistoryDAO.findConflictInHistory");
      Long count = (Long) query.uniqueResult();
      return count != 0;
   }

}
