/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package net.openl10n.flies.dao;

import java.util.List;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HTextFlow;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>
{

   public TextFlowDAO()
   {
      super(HTextFlow.class);
   }

   public TextFlowDAO(Session session)
   {
      super(HTextFlow.class, session);
   }

   /**
    * @param document
    * @param id
    * @return
    */
   public HTextFlow getById(HDocument document, String id)
   {
      return (HTextFlow) getSession().createCriteria(HTextFlow.class).add(Restrictions.naturalId().set("resId", id).set("document", document)).setCacheable(true).setComment("TextFlowDAO.getById").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> findByIdList(List<Long> idList)
   {
      Query query = getSession().createQuery("FROM HTextFlow WHERE id in (:idList)");
      query.setParameterList("idList", idList);
      query.setComment("TextFlowDAO.getByIdList");
      return query.list();
   }

   public HTextFlow getObsoleteById(HDocument document, String id)
   {
      return (HTextFlow) getSession().createCriteria(HTextFlow.class).add(Restrictions.naturalId().set("resId", id).set("document", document)).add(Restrictions.eq("obsolete", true)).setCacheable(true).setComment("TextFlowDAO.getObsoleteById").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<Long> getIdsByTargetState(LocaleId locale, ContentState state)
   {
      Query q = getSession().createQuery("select tft.textFlow.id from HTextFlowTarget tft where tft.locale.localeId=:locale and tft.state=:state");
      q.setParameter("locale", locale);
      q.setParameter("state", state);
      q.setComment("TextFlowDAO.getIdsByTargetState");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getNavigationByDocumentId(Long documentId, int offset, boolean reverse)
   {
      Criteria c = getSession().createCriteria(HTextFlow.class).add(Restrictions.eq("document.id", documentId)).add(Restrictions.eq("obsolete", false)).setComment("TextFlowDAO.getNavigationByDocumentId");

      if (reverse)
      {
         c.add(Restrictions.lt("pos", offset)).addOrder(Order.desc("pos"));
      }
      else
      {
         c.add(Restrictions.gt("pos", offset)).addOrder(Order.asc("pos"));
      }

      return c.list();

   }

   public List<HTextFlow> findEquivalents(HTextFlow textFlow)
   {
      // @formatter:off
      return getSession().createQuery(
         "select tf from HTextFlow tf " +
         "where tf.resId = :resid " +
         "and tf.document.docId = :docId " +
         "and tf.document.projectIteration.project = :project " +
         "and tf.document.projectIteration != :iteration ")
            .setParameter("docId", textFlow.getDocument().getDocId())
            .setParameter("project", textFlow.getDocument().getProjectIteration().getProject())
            .setParameter("iteration", textFlow.getDocument().getProjectIteration())
            .setParameter("resid", textFlow.getResId())
            .list();
      // @formatter:on
   }

}
