/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.zanata.common.EntityStatus;
import org.zanata.hibernate.search.CaseInsensitiveWhitespaceAnalyzer;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.ProjectRole;

@RequestScoped
public class ProjectDAO extends AbstractDAOImpl<HProject, Long> {
    @Inject @FullText
    private FullTextEntityManager entityManager;

    public ProjectDAO() {
        super(HProject.class);
    }

    public ProjectDAO(Session session) {
        super(HProject.class, session);
    }

    public @Nullable
    HProject getBySlug(@Nonnull String slug) {
        if (!StringUtils.isEmpty(slug)) {
            return (HProject) getSession().byNaturalId(HProject.class)
                    .using("slug", slug).load();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<HProject> getOffsetList(int offset, int count,
                    boolean filterOutActive, boolean filterOutReadOnly,
                    boolean filterOutObsolete) {

        String condition =
                constructFilterCondition(filterOutActive, filterOutReadOnly,
                    filterOutObsolete);

        StringBuilder sb = new StringBuilder();
        sb.append("Select distinct p from HProject p ")
            .append(condition)
            .append("order by UPPER(p.name) asc");
        Query q = getSession().createQuery(sb.toString())
            .setMaxResults(count)
            .setFirstResult(offset)
            .setCacheable(true)
            .setComment("ProjectDAO.getOffsetList");
        return q.list();
    }

    public int getFilterProjectSize(boolean filterOutActive,
            boolean filterOutReadOnly, boolean filterOutObsolete) {
        String condition = constructFilterCondition(filterOutActive,
                filterOutReadOnly, filterOutObsolete);
        String query = "select count(*) from HProject p " + condition;
        Query q = getSession().createQuery(query);
        q.setCacheable(true).setComment("ProjectDAO.getFilterProjectSize");
        Long totalCount = (Long) q.uniqueResult();

        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    private String constructFilterCondition(boolean filterOutActive,
            boolean filterOutReadOnly, boolean filterOutObsolete) {
        StringBuilder condition = new StringBuilder();
        if (filterOutActive || filterOutReadOnly || filterOutObsolete) {
            condition.append("where ");
        }

        if (filterOutActive) {
            // TODO bind this as a parameter
            condition.append("p.status <> '" + EntityStatus.ACTIVE.getInitial()
                    + "' ");
        }

        if (filterOutReadOnly) {
            if (filterOutActive) {
                condition.append("and ");
            }

            // TODO bind this as a parameter
            condition.append("p.status <> '"
                    + EntityStatus.READONLY.getInitial() + "' ");
        }

        if (filterOutObsolete) {
            if (filterOutActive || filterOutReadOnly) {
                condition.append("and ");
            }

            // TODO bind this as a parameter
            condition.append("p.status <> '"
                    + EntityStatus.OBSOLETE.getInitial() + "' ");
        }
        return condition.toString();
    }

    @SuppressWarnings("unchecked")
    public List<HProjectIteration> getAllIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug order by t.creationDate");
        q.setParameter("projectSlug", slug);
        q.setCacheable(true).setComment("ProjectDAO.getAllIterations");
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<HProjectIteration> getActiveIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.ACTIVE);
        q.setCacheable(true).setComment("ProjectDAO.getActiveIterations");
        return q.list();
    }

    /**
     * @param projectId
     *            project id
     * @return number of non-obsolete (active and read only) iterations under given project
     */
    public int getTranslationCandidateCount(Long projectId) {
        Query q = getSession()
                .createQuery(
                        "select count(*) from HProjectIteration it where it.project.id = :projectId and it.status <> :status")
                .setParameter("projectId", projectId)
                .setParameter("status", EntityStatus.OBSOLETE).setCacheable(true)
                .setComment("ProjectDAO.getTranslationCandidateCount");
        return ((Long) q.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<HProjectIteration> getReadOnlyIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.READONLY);
        q.setCacheable(true).setComment("ProjectDAO.getReadOnlyIterations");
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<HProjectIteration> getObsoleteIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.OBSOLETE);
        q.setCacheable(true).setComment("ProjectDAO.getObsoleteIterations");
        return q.list();
    }

    public int getTotalProjectCount() {
        String query = "select count(*) from HProject";
        Query q = getSession().createQuery(query.toString());
        q.setCacheable(true).setComment("ProjectDAO.getTotalProjectCount");
        Long totalCount = (Long) q.uniqueResult();

        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalActiveProjectCount() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HProject p where p.status = :status");
        q.setParameter("status", EntityStatus.ACTIVE);
        q.setCacheable(true)
                .setComment("ProjectDAO.getTotalActiveProjectCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalReadOnlyProjectCount() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HProject p where p.status = :status");
        q.setParameter("status", EntityStatus.READONLY);
        q.setCacheable(true).setComment(
                "ProjectDAO.getTotalReadOnlyProjectCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalObsoleteProjectCount() {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from HProject p where p.status = :status");
        q.setParameter("status", EntityStatus.OBSOLETE);
        q.setCacheable(true).setComment(
                "ProjectDAO.getTotalObsoleteProjectCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public List<HProject> searchProjects(@Nonnull String searchQuery,
            int maxResult, int firstResult, boolean includeObsolete)
            throws ParseException {
        FullTextQuery query = buildSearchQuery(searchQuery, includeObsolete);
        if(maxResult > 0) {
            query.setMaxResults(maxResult);
        }
        return query.setFirstResult(firstResult)
                .getResultList();
    }

    public int getQueryProjectSize(@Nonnull String searchQuery,
            boolean includeObsolete) throws ParseException {
        return searchProjects(searchQuery, -1, 0, includeObsolete).size();
    }

    private FullTextQuery buildSearchQuery(@Nonnull String searchQuery,
        boolean includeObsolete) throws ParseException {
        String queryText = QueryParser.escape(searchQuery);

        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(buildSearchFieldQuery(queryText, "slug"), BooleanClause.Occur.SHOULD);
        booleanQuery.add(buildSearchFieldQuery(queryText, "name"), BooleanClause.Occur.SHOULD);
        booleanQuery.add(buildSearchFieldQuery(queryText, "description"), BooleanClause.Occur.SHOULD);

        if (!includeObsolete) {
            TermQuery obsoleteStateQuery =
                    new TermQuery(new Term(IndexFieldLabels.ENTITY_STATUS,
                            EntityStatus.OBSOLETE.toString().toLowerCase()));
            booleanQuery.add(obsoleteStateQuery, BooleanClause.Occur.MUST_NOT);
        }

        return entityManager.createFullTextQuery(booleanQuery, HProject.class);
    }

    /**
     * Build BooleanQuery on single lucene field by splitting searchQuery with
     * white space.
     *
     * @param searchQuery
     *            - query string, will replace hypen with space and escape
     *            special char
     * @param field
     *            - lucene field
     */
    private BooleanQuery buildSearchFieldQuery(@Nonnull String searchQuery,
        @Nonnull String field) throws ParseException {
        BooleanQuery query = new BooleanQuery();

        //escape special character search
        searchQuery = QueryParser.escape(searchQuery);

        for(String searchString: searchQuery.split("\\s+")) {
            QueryParser parser = new QueryParser(field,
                    new CaseInsensitiveWhitespaceAnalyzer());
            query.add(parser.parse(searchString + "*"), BooleanClause.Occur.MUST);
        }
        return query;
    }

    public List<HProject> findAllTranslatedProjects(HAccount account, int maxResults) {
        Query q =
                getSession()
                        .createQuery(
                                "select distinct tft.textFlow.document.projectIteration.project " +
                                "from HTextFlowTarget tft " +
                                "where tft.translator = :translator")
                        .setParameter("translator", account)
                        .setMaxResults(maxResults);
        return q.list();
    }

    public int getTranslatedProjectCount(HAccount account) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(distinct tft.textFlow.document.projectIteration.project) " +
                                        "from HTextFlowTarget tft " +
                                        "where tft.translator = :translator"
                        )
                        .setParameter("translator", account);
        return ((Long) q.uniqueResult()).intValue();
    }



    /**
     * @param project A project
     * @param account The user for which the last translated date.
     * @return A date indicating the last time on which account's user
     * translated project.
     */
    public Date getLastTranslatedDate(HProject project, HAccount account) {
        Query q =
                getSession()
                        .createQuery(
                                "select max (tft.lastChanged) " +
                                        "from HTextFlowTarget tft " +
                                        "where tft.translator = :translator " +
                                        "and tft.textFlow.document.projectIteration.project = :project"
                        )
                        .setParameter("translator", account)
                        .setParameter("project", project)
                        .setCacheable(true);
        return (Date) q.uniqueResult();
    }

    /**
     * @param project A project
     * @return A date indicating the last time when any user translated project
     */
    public Date getLastTranslatedDate(HProject project) {
        Query q =
                getSession()
                        .createQuery(
                                "select max (tft.lastChanged) " +
                                        "from HTextFlowTarget tft " +
                                        "where tft.textFlow.document.projectIteration.project = :project"
                        )
                        .setParameter("project", project)
                        .setCacheable(true);
        return (Date) q.uniqueResult();
    }

    /**
     * @param project A project
     * @return A date indicating the last time when any user translated project
     */
    public HPerson getLastTranslator(HProject project) {
        Query q =
                getSession()
                        .createQuery(
                                "select tft.translator " +
                                "from HTextFlowTarget tft " +
                                "where tft.textFlow.document.projectIteration.project = :project " +
                                "order by tft.lastChanged desc"
                        )
                        .setParameter("project", project)
                        .setMaxResults(1)
                        .setCacheable(true);
        List results = q.list();
        if (results.isEmpty()) {
            return null;
        } else {
            return (HPerson) results.get(0);
        }
    }

    public List<HProject> getProjectsForMaintainer(HPerson maintainer,
            String filter, int firstResult, int maxResults) {
        final String sqlFilter = filter == null ? "" : filter.toLowerCase();
        Query q = getSession().createQuery(
                "select m.project from HProjectMember m " +
                        "where m.person = :maintainer " +
                        "and m.role = :role " +
                        "and m.project.status <> :obsolete " +
                        "and (lower(m.project.name) like :filter " +
                        "or lower(m.project.slug) like :filter) " +
                        "order by m.project.name")
                .setParameter("maintainer", maintainer)
                .setParameter("role", ProjectRole.Maintainer)
                .setParameter("obsolete", EntityStatus.OBSOLETE)
                .setParameter("filter", "%" + sqlFilter + "%")
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);
        return q.list();
    }

    public int getMaintainedProjectCount(HPerson maintainer, String filter) {
        final String sqlFilter = filter == null ? "" : filter.toLowerCase();
        Query q = getSession().createQuery(
                "select count(m) from HProjectMember m " +
                        "where m.person = :maintainer " +
                        "and m.role = :role " +
                        "and m.project.status <> :obsolete " +
                        "and (lower(m.project.name) like :filter " +
                        "or lower(m.project.slug) like :filter)")
                .setParameter("maintainer", maintainer)
                .setParameter("role", ProjectRole.Maintainer)
                .setParameter("obsolete", EntityStatus.OBSOLETE)
                .setParameter("filter", "%" + sqlFilter + "%");
        return ((Long) q.uniqueResult()).intValue();
    }
}
