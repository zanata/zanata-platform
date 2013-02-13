/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionGroupDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class VersionGroupDAO extends AbstractDAOImpl<HIterationGroup, Long>
{
   public VersionGroupDAO()
   {
      super(HIterationGroup.class);
   }

   public VersionGroupDAO(Session session)
   {
      super(HIterationGroup.class, session);
   }

   @SuppressWarnings("unchecked")
   public List<HIterationGroup> getAllActiveVersionGroups()
   {
      Query query = getSession().createQuery("from HIterationGroup g where g.status = :status");
      query.setParameter("status", EntityStatus.ACTIVE);
      query.setComment("VersionGroupDAO.getAllActiveVersionGroups");
      return query.list();
   }

   @SuppressWarnings("unchecked")
   public List<HIterationGroup> getAllObsoleteVersionGroups()
   {
      Query query = getSession().createQuery("from HIterationGroup g where g.status = :status");
      query.setParameter("status", EntityStatus.OBSOLETE);
      query.setComment("VersionGroupDAO.getAllObsoleteVersionGroups");
      return query.list();
   }

   public HIterationGroup getBySlug(String slug)
   {
      return (HIterationGroup) getSession().byNaturalId(HIterationGroup.class).using("slug", slug).load();
   }

   public List<HPerson> getMaintainerBySlug(String slug)
   {
      Query q = getSession().createQuery("select g.maintainers from HIterationGroup as g where g.slug = :slug");
      q.setParameter("slug", slug);
      q.setComment("VersionGroupDAO.getMaintainerBySlug");
      @SuppressWarnings("unchecked")
      List<HPerson> results = q.list();
      return results;
   }

   public List<HIterationGroup> searchLikeSlugAndName(String searchTerm)
   {
      Query query = getSession().createQuery("from HIterationGroup g where lower(g.slug) LIKE :searchTerm OR lower(g.name) LIKE :searchTerm AND g.status = :status");
      query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
      query.setParameter("status", EntityStatus.ACTIVE);
      query.setComment("VersionGroupDAO.searchLikeSlugAndName");
      return query.list();
   }
}
