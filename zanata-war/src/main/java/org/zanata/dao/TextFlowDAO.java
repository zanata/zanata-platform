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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
        FilterConstraintToQuery constraintToQuery =
                FilterConstraintToQuery.filterInSingleDocument(
                        filterConstraints, documentId);

        String hql = constraintToQuery.toModalNavigationQuery();
        Query query = getSession().createQuery(hql);
        return constraintToQuery.setQueryParameters(query, hLocale)
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

    public int countActiveTextFlowsInDocument(Long documentId) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HTextFlow tf where tf.obsolete=0 and tf.document.id = :documentId");
        q.setParameter("documentId", documentId);
        q.setCacheable(true).setComment(
                "TextFlowDAO.countActiveTextFlowsInDocument");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0 : totalCount.intValue();
    }

    public List<HTextFlow> getTextFlowsByDocumentId(Long documentId,
            Integer offset, Integer maxResults) {
        Query q =
                getSession()
                        .createQuery(
                                "from HTextFlow tf where tf.obsolete=0 and tf.document.id = :documentId order by tf.pos");
        q.setParameter("documentId", documentId);

        if (offset != null) {
            q.setFirstResult(offset);
        }
        if (maxResults != null) {
            q.setMaxResults(maxResults);
        }
        q.setCacheable(true).setComment("TextFlowDAO.getTextFlowsByDocumentId");
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

    public long countActiveTextFlowsInProjectIteration(Long versionId) {
        String query =
                "select count(*) from HTextFlow tf where tf.obsolete = 0 " +
                        "and tf.document.obsolete = 0 " +
                        "and tf.document.projectIteration.id=:versionId";

        Query q = getSession().createQuery(query);
        q.setParameter("versionId", versionId);
        q.setCacheable(true).setComment(
                "TextFlowDAO.countTextFlowsInProjectIteration");
        return (Long) q.uniqueResult();
    }

    /**
     * This will eagerly fetch HTextFlow properties including targets and target
     * histories. It will use a literal list in IN clause. Mysql has no limit on
     * In clause itself but performance wide, IN is only faster when the number
     * is relatively small i.e. less than 500. We are using this query in
     * translation batch update which is 100 per batch.
     *
     * @see org.zanata.model.HTextFlow
     * @see <a href="http://stackoverflow.com/a/1532454/345718">MySQL number of
     *      items within “in clause”/a>
     *
     * @param document
     *            document
     * @param resIds
     *            resID list
     * @return a map with HTextFlow resId as key and HTextFlow as value
     */
    public Map<String, HTextFlow> getByDocumentAndResIds(HDocument document,
            List<String> resIds) {
        Query query = getSession()
                .getNamedQuery(HTextFlow.QUERY_GET_BY_DOC_AND_RES_ID_BATCH)
                .setParameter("document", document)
                .setParameterList("resIds", resIds)
                .setComment("TextFlowDAO.getByDocumentAndResIds");
        @SuppressWarnings("unchecked")
        List<HTextFlow> results = query.list();
        ImmutableMap.Builder<String, HTextFlow> builder = ImmutableMap.builder();
        for (HTextFlow textFlow : results) {
            builder.put(textFlow.getResId(), textFlow);
        }

        return builder.build();
    }

}
