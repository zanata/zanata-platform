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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.model.Activity;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ActivityDAO extends AbstractDAOImpl<Activity, Long>
{
   private static final long serialVersionUID = 1L;

   public ActivityDAO()
   {
      super(Activity.class);
   }

   public ActivityDAO(Session session)
   {
      super(Activity.class, session);
   }

   @SuppressWarnings("unchecked")
   public Activity findActivity(long personId, EntityType contextType, long contextId, ActivityType actionType, Date approxTime)
   {
      Query query = getSession().createQuery("FROM Activity a WHERE a.actor.id = :personId "
            + "AND a.contextId = :contextId "
            + "AND a.actionType = :actionType "
            + "AND a.contextType = :contextType "
            + "AND :approxTime = a.approxTime");
      query.setParameter("personId", personId);
      query.setParameter("contextId", contextId);
      query.setParameter("actionType", actionType);
      query.setParameter("contextType", contextType);
      query.setTimestamp("approxTime", approxTime);
      query.setCacheable(true);
      query.setComment("activityDAO.findActivity");
      return (Activity) query.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<Activity> findLatestActivitiesForContext(long personId, long contextId, int offset, int maxResults)
   {
      Query query = getSession().createQuery("FROM Activity a WHERE a.actor.id = :personId "
            + "AND a.contextId = :contextId "
            + "order by a.lastChanged DESC");
      query.setParameter("personId", personId);
      query.setParameter("contextId", contextId);
      query.setMaxResults(maxResults);
      query.setFirstResult(offset);
      query.setCacheable(true);
      query.setComment("activityDAO.findActivities");
      return query.list();
   }

   public List<Activity> findLatestActivities(long personId, int offset, int maxResults)
   {
      Query query = getSession().createQuery("FROM Activity a WHERE a.actor.id = :personId "
            + "order by a.lastChanged DESC");
      query.setParameter("personId", personId);
      query.setMaxResults(maxResults);
      query.setFirstResult(offset);
      query.setCacheable(true);
      query.setComment("activityDAO.findLatestActivities");
      return query.list();
   }

   public int getActivityCountByActor(Long personId)
   {
      Query q = getSession().createQuery("select count(*) from Activity a where a.actor.id = :personId");
      q.setParameter("personId", personId);
      q.setCacheable(true);
      q.setComment("activityDAO.getActivityCountByActor");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
      {
         return 0;
      }
      return totalCount.intValue();
   }
}
