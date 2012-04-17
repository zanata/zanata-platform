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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.EntityStatus;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionGroupDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class VersionGroupDAO extends AbstractDAOImpl<HIterationGroup, Long>
{
   @In
   private FullTextEntityManager entityManager;

   @Logger
   Log log;

   public VersionGroupDAO()
   {
      super(HIterationGroup.class);
   }

   public VersionGroupDAO(Session session)
   {
      super(HIterationGroup.class, session);
   }

   public List<HIterationGroup> getAllActiveVersionGroups()
   {
      Query query = getSession().createQuery("from HIterationGroup g where g.status = :status");
      query.setParameter("status", EntityStatus.ACTIVE);
      return query.list();
   }

   public HIterationGroup getBySlug(String slug)
   {
      Criteria criteria = getSession().createCriteria(HIterationGroup.class);
      criteria.add(Restrictions.naturalId().set("slug", slug));
      criteria.setCacheable(true).setComment("VersionGroupDAO.getBySlug");
      return (HIterationGroup) criteria.uniqueResult();
   }

   public List<HPerson> getMaintainerBySlug(String slug)
   {
      Query q = getSession().createQuery("select g.maintainers from HIterationGroup as g where g.slug = :slug");
      q.setParameter("slug", slug);
      return q.list();
   }

   public List<HProjectIteration> findAllContainingName(String searchTerm) throws ParseException
   {
      QueryParser parser = new QueryParser(Version.LUCENE_29, "slug", new StandardAnalyzer(Version.LUCENE_29));
      org.apache.lucene.search.Query textQuery = parser.parse(searchTerm);
//      TermQuery projectQuery = new TermQuery(new Term(GroupSearchBridge.PROJECT_FIELD + GroupSearchBridge.PROJECT_FIELD, projectSlug));

      org.hibernate.search.jpa.FullTextQuery ftQuery = entityManager.createFullTextQuery(textQuery, HProjectIteration.class);

      List<HProjectIteration> matches = (List<HProjectIteration>) ftQuery.getResultList();
      return  matches;
   }
}
