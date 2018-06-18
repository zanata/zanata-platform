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

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.hibernate.Criteria;
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
import org.zanata.seam.security.CurrentUser;
import org.zanata.security.ZanataIdentity;

import static org.zanata.hibernate.search.IndexFieldLabels.FULL_SLUG_FIELD;

@RequestScoped
public class ProjectDAO extends AbstractDAOImpl<HProject, Long> {
    private static final long serialVersionUID = 6188033047151363778L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject @FullText
    private FullTextEntityManager entityManager;
    @Inject
    private CurrentUser currentUser;
    @Inject
    private ZanataIdentity identity;

    public ProjectDAO() {
        super(HProject.class);
    }

    public ProjectDAO(Session session, CurrentUser currentUser) {
        super(HProject.class, session);
        this.currentUser = currentUser;
    }

    public ProjectDAO(FullTextEntityManager entityManager, Session session,
            ZanataIdentity identity) {
        super(HProject.class, session);
        this.entityManager = entityManager;
        this.identity = identity;
    }

    public @Nullable
    HProject getBySlug(@Nonnull String slug) {
        if (!StringUtils.isEmpty(slug)) {
            return (HProject) getSession().byNaturalId(HProject.class)
                    .using("slug", slug).load();
        }
        return null;
    }

    public List<HProject> getOffsetList(int offset, int count,
                    boolean filterOutActive, boolean filterOutReadOnly,
                    boolean filterOutObsolete) {

        HPerson person = currentUser.isLoggedIn() ?
                currentUser.getPerson() : null;

        String condition =
                constructFilterCondition(filterOutActive, filterOutReadOnly,
                    filterOutObsolete, person);

        StringBuilder sb = new StringBuilder();
        sb.append("select distinct p, UPPER(p.name) from HProject p ")
            .append(condition)
            .append("order by UPPER(p.name) asc");
        Query q = getSession().createQuery(sb.toString());

        if (count > 0) {
            q.setMaxResults(count);
        }
        if (offset > 0) {
            q.setFirstResult(offset);
        }

        if (person != null) {
            q.setParameter("person", person);
        }

        q.setCacheable(true)
                .setComment("ProjectDAO.getOffsetList");
        @SuppressWarnings("unchecked")
        List<Object[]> list = q.list();
        return list.stream().map(obj -> (HProject) obj[0])
                .collect(Collectors.toList());
    }

    /**
     * If all filters are false, return all projects accessible to logged-in
     * user.
     */
    public int getFilterProjectSize(boolean filterOutActive,
            boolean filterOutReadOnly, boolean filterOutObsolete) {
        HPerson person = currentUser.isLoggedIn() ?
                currentUser.getPerson() : null;

        String condition = constructFilterCondition(filterOutActive,
                filterOutReadOnly, filterOutObsolete, person);
        String query = "select count(distinct p) from HProject p " + condition;
        Query q = getSession().createQuery(query);
        if (person != null) {
            q.setParameter("person", person);
        }
        q.setCacheable(true)
                .setComment("ProjectDAO.getFilterProjectSize");
        Long totalCount = (Long) q.uniqueResult();

        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    /**
     * May return private projects only if person is non-null and is one of
     * the project members
     * IMPORTANT: This method potentially returns duplicate results.
     * Callers must use 'distinct' to filter out duplication
     */
    private String constructFilterCondition(boolean filterOutActive,
            boolean filterOutReadOnly, boolean filterOutObsolete,
            @Nullable HPerson person) {
        StringBuilder condition = new StringBuilder();

        if (person != null) {
            condition.append("left join p.localeMembers lm left join p.members m ")
                    .append("where ((p.privateProject is TRUE and (m.person =:person or lm.person =:person)) ")
                    .append("or (p.privateProject is FALSE)) ");
        } else {
            condition.append("where p.privateProject is FALSE ");
        }

        if (filterOutActive || filterOutReadOnly || filterOutObsolete) {
            condition.append("and ");
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

    public List<HProjectIteration> getAllIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug order by t.creationDate");
        q.setParameter("projectSlug", slug);
        q.setCacheable(true).setComment("ProjectDAO.getAllIterations");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> list = q.list();
        return list;
    }

    public List<HProjectIteration> getActiveIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.ACTIVE);
        q.setCacheable(true).setComment("ProjectDAO.getActiveIterations");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> list = q.list();
        return list;
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

    public List<HProjectIteration> getReadOnlyIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.READONLY);
        q.setCacheable(true).setComment("ProjectDAO.getReadOnlyIterations");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> list = q.list();
        return list;
    }

    public List<HProjectIteration> getObsoleteIterations(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
        q.setParameter("projectSlug", slug).setParameter("status",
                EntityStatus.OBSOLETE);
        q.setCacheable(true).setComment("ProjectDAO.getObsoleteIterations");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> list = q.list();
        return list;
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
        @SuppressWarnings("unchecked")
        List<HProject> projects = query.setFirstResult(firstResult)
                .getResultList();
        return projects.stream()
                .filter(project -> identity.hasPermission(project, "read"))
                .collect(Collectors.toList());
    }

    public int getQueryProjectSize(@Nonnull String searchQuery,
            boolean includeObsolete) throws ParseException {
        return searchProjects(searchQuery, -1, 0, includeObsolete).size();
    }

    private FullTextQuery buildSearchQuery(@Nonnull String searchQuery,
        boolean includeObsolete) throws ParseException {

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        // for slug, we do prefix search (so people can search with '-' in it
        booleanQuery.add(buildSearchFieldQuery(searchQuery, FULL_SLUG_FIELD, true), BooleanClause.Occur.SHOULD);
        // for name and description, we split the word using the same analyzer and search as is
        booleanQuery.add(buildSearchFieldQuery(searchQuery, "name", false), BooleanClause.Occur.SHOULD);
        booleanQuery.add(buildSearchFieldQuery(searchQuery, "description", false), BooleanClause.Occur.SHOULD);
        if (!includeObsolete) {
            TermQuery obsoleteStateQuery =
                    new TermQuery(new Term(IndexFieldLabels.ENTITY_STATUS,
                            EntityStatus.OBSOLETE.toString().toLowerCase()));
            booleanQuery.add(obsoleteStateQuery, BooleanClause.Occur.MUST_NOT);
        }

        BooleanQuery luceneQuery = booleanQuery.build();
        return entityManager.createFullTextQuery(luceneQuery, HProject.class);
    }

    /**
     * Build BooleanQuery on single lucene field by splitting searchQuery with
     * white space.
     *
     * @param searchQuery
     *            - query string, will escape special char
     * @param field
     *            - lucene field
     * @param wildcard
     *            - whether append wildcard to the end
     */
    private BooleanQuery buildSearchFieldQuery(@Nonnull String searchQuery,
        @Nonnull String field, boolean wildcard) throws ParseException {
        BooleanQuery.Builder query = new BooleanQuery.Builder();

        for(String searchString: searchQuery.split("\\s+")) {
            QueryParser parser = new QueryParser(field,
                    new CaseInsensitiveWhitespaceAnalyzer());
            //escape special character search

            String escaped = QueryParser.escape(searchString);
            escaped = wildcard && !StringUtils.endsWith(searchQuery, "*") ?
                    escaped + "*" : escaped;
            query.add(parser.parse(escaped), BooleanClause.Occur.MUST);
        }
        return query.build();
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
        @SuppressWarnings("unchecked")
        List<HProject> list = q.list();
        return list;
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
        @SuppressWarnings("unchecked")
        List<HPerson> results = q.list();
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
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
        @SuppressWarnings("unchecked")
        List<HProject> list = q.list();
        return list;
    }

    @NativeQuery
    public List<HProject> getProjectsForMember(HPerson person,
            String filter, int firstResult, int maxResults) {
        final String sqlFilter = StringUtils.isBlank(filter) ? "" : filter.toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("select distinct * from (")
                .append("(select project.* from HProject project ")
                .append("join HProject_Member m on m.projectId = project.id ")
                .append("where m.personId = :personId ")
                .append("and project.status <> :obsolete ")
                .append("and (lower(project.name) like :filter ")
                .append("or lower(project.slug) like :filter)) ")
                .append("union all ")
                .append("(select project.* from HProject project ")
                .append("join HProject_LocaleMember lm on lm.projectId = project.id ")
                .append("where lm.personId = :personId ")
                .append("and project.status <> :obsolete ")
                .append("and (lower(project.name) like :filter ")
                .append("or lower(project.slug) like :filter))")
                .append(") as projects order by name");

        Query q = getSession().createSQLQuery(sb.toString())
                .addEntity("project", HProject.class)
                .setParameter("personId", person.getId())
                .setParameter("obsolete", EntityStatus.OBSOLETE.getInitial())
                .setParameter("filter", "%" + sqlFilter + "%")
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);
        @SuppressWarnings("unchecked")
        List<HProject> list = q.list();
        return list;
    }

    @NativeQuery
    public int getProjectsForMemberCount(HPerson person, String filter) {
        final String sqlFilter = StringUtils.isBlank(filter) ? "" : filter.toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) from (");
        sb.append("select distinct * from (")
                .append("(select project.* from HProject project ")
                .append("join HProject_Member m on m.projectId = project.id ")
                .append("where m.personId = :personId ")
                .append("and project.status <> :obsolete ")
                .append("and (lower(project.name) like :filter ")
                .append("or lower(project.slug) like :filter)) ")
                .append("union all ")
                .append("(select project.* from HProject project ")
                .append("join HProject_LocaleMember lm on lm.projectId = project.id ")
                .append("where lm.personId = :personId ")
                .append("and project.status <> :obsolete ")
                .append("and (lower(project.name) like :filter ")
                .append("or lower(project.slug) like :filter))")
                .append(") as projects");
        sb.append(") as counts");

        Query q = getSession().createSQLQuery(sb.toString())
                .setParameter("personId", person.getId())
                .setParameter("obsolete", EntityStatus.OBSOLETE.getInitial())
                .setParameter("filter", "%" + sqlFilter + "%");
        return ((BigInteger) q.uniqueResult()).intValue();
    }

    public int getTotalDocCount(String projectSlug) {
        StringBuilder query = new StringBuilder();
        query.append("select count(doc) from HDocument doc ")
                .append("where doc.projectIteration.project.slug=:projectSlug ")
                .append("and doc.projectIteration.status <> :obsolete ")
                .append("and doc.obsolete = false");

        Query q = getSession().createQuery(query.toString())
                .setParameter("projectSlug", projectSlug)
                .setParameter("obsolete", EntityStatus.OBSOLETE);
        return ((Long) q.uniqueResult()).intValue();
    }

}
