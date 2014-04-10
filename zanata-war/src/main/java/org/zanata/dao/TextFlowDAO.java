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
package org.zanata.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.search.FilterConstraintToQuery;
import org.zanata.search.FilterConstraints;
import org.zanata.webtrans.shared.model.DocumentId;
import lombok.extern.slf4j.Slf4j;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long> {
    private static final long serialVersionUID = 1L;

    // TODO replace all getSession() code to use entityManager

    @In
    LocaleDAO localeDAO;

    public TextFlowDAO() {
        super(HTextFlow.class);
    }

    public TextFlowDAO(Session session) {
        super(HTextFlow.class, session);
    }

    public HTextFlow getById(HDocument document, String id) {
        return (HTextFlow) getSession().byNaturalId(HTextFlow.class)
                .using("resId", id).using("document", document).load();
    }

    @SuppressWarnings("unchecked")
    public List<HTextFlow> findByIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<HTextFlow>();
        }
        Query query =
                getSession()
                        .createQuery("FROM HTextFlow WHERE id in (:idList)");
        query.setParameterList("idList", idList);
        // caching could be expensive for long idLists
        query.setCacheable(false).setComment("TextFlowDAO.getByIdList");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<HTextFlow> getNavigationByDocumentId(DocumentId documentId,
            HLocale hLocale, ResultTransformer resultTransformer,
            FilterConstraints filterConstraints) {
        FilterConstraintToQuery toModalNavigationQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        filterConstraints, documentId);

        String hql = toModalNavigationQuery.toModalNavigationQuery();
        Query query = getSession().createQuery(hql);
        return toModalNavigationQuery.setQueryParameters(query, hLocale)
                .setResultTransformer(resultTransformer).list();
    }



    public int getTotalWords() {
        Query q =
                getSession()
                        .createQuery(
                                "select sum(tf.wordCount) from HTextFlow tf where tf.obsolete=0");
        q.setCacheable(true).setComment("TextFlowDAO.getTotalWords");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public int getTotalTextFlows() {
        Query q = getSession().createQuery("select count(*) from HTextFlow");
        q.setCacheable(true).setComment("TextFlowDAO.getTotalTextFlows");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public int getTotalActiveTextFlows() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlow tf where tf.obsolete=0");
        q.setCacheable(true).setComment("TextFlowDAO.getTotalActiveTextFlows");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public int getTotalObsoleteTextFlows() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlow tf where tf.obsolete=1");
        q.setCacheable(true)
                .setComment("TextFlowDAO.getTotalObsoleteTextFlows");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public int getCountByDocument(Long documentId) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
        q.setParameter("id", documentId);
        q.setCacheable(true).setComment("TextFlowDAO.getCountByDocument");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    @SuppressWarnings("unchecked")
    public List<HTextFlow> getTextFlowsByDocumentId(DocumentId documentId,
            int startIndex, int maxSize) {
        Query q =
                getSession()
                        .createQuery(
                                "from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
        q.setParameter("id", documentId.getId());
        q.setFirstResult(startIndex);
        q.setMaxResults(maxSize);
        q.setCacheable(true).setComment("TextFlowDAO.getTextFlowsByDocumentId");
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<HTextFlow> getAllTextFlowsByDocumentId(DocumentId documentId) {
        Query q =
                getSession()
                        .createQuery(
                                "from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
        q.setParameter("id", documentId.getId());
        q.setCacheable(true).setComment(
                "TextFlowDAO.getAllTextFlowsByDocumentId");
        return q.list();
    }

    /**
     * for a given locale, we can filter it by content state or search in source
     * and target.
     *
     *
     * @param documentId
     *            document id (NOT the String type docId)
     * @param hLocale
     *            locale
     * @param constraints
     *            filter constraints
     * @param firstResult
     *            start index
     * @param maxResult
     *            max result
     * @return a list of HTextFlow that matches the constraint.
     * @see org.zanata.service.impl.TextFlowSearchServiceImpl#findTextFlows(org.zanata.webtrans.shared.model.WorkspaceId,
     *      org.zanata.search.FilterConstraints)
     */
    public List<HTextFlow> getTextFlowByDocumentIdWithConstraints(
            DocumentId documentId, HLocale hLocale,
            FilterConstraints constraints, int firstResult, int maxResult) {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(constraints,
                        documentId);
        String queryString = constraintToQuery.toEntityQuery();
        log.debug("\n query {}\n", queryString);

        Query textFlowQuery = getSession().createQuery(queryString);
        constraintToQuery.setQueryParameters(textFlowQuery, hLocale);
        textFlowQuery.setFirstResult(firstResult).setMaxResults(maxResult);
        textFlowQuery.setCacheable(true).setComment(
                "TextFlowDAO.getTextFlowByDocumentIdWithConstraint");

        @SuppressWarnings("unchecked")
        List<HTextFlow> result = textFlowQuery.list();
        log.debug("{} textFlow for locale {} filter by {}", result.size(),
                hLocale.getLocaleId(), constraints);
        return result;
    }

    public List<HTextFlow> getAllTextFlowByDocumentIdWithConstraints(
            DocumentId documentId, HLocale hLocale,
            FilterConstraints constraints) {
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(constraints,
                        documentId);
        String queryString = constraintToQuery.toEntityQuery();
        log.debug("\n query {}\n", queryString);

        Query textFlowQuery = getSession().createQuery(queryString);
        constraintToQuery.setQueryParameters(textFlowQuery, hLocale);
        textFlowQuery.setCacheable(true).setComment(
                "TextFlowDAO.getAllTextFlowByDocumentIdWithConstraint");

        @SuppressWarnings("unchecked")
        List<HTextFlow> result = textFlowQuery.list();
        log.debug("{} textFlow for locale {} filter by {}", result.size(),
                hLocale.getLocaleId(), constraints);
        return result;
    }
}
