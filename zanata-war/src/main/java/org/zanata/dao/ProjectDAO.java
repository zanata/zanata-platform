package org.zanata.dao;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Version;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.hibernate.search.CaseInsensitiveWhitespaceAnalyzer;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

@Name("projectDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectDAO extends AbstractDAOImpl<HProject, Long> {
    @In
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
    public List<HPerson> getProjectMaintainerBySlug(String slug) {
        Query q =
                getSession()
                        .createQuery(
                                "select p.maintainers from HProject as p where p.slug = :slug");
        q.setParameter("slug", slug);
        // http://stackoverflow.com/questions/9060403/hibernate-query-cache-issue
        // q.setCacheable(true)
        q.setComment("ProjectDAO.getProjectMaintainerBySlug");
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<HProject>
            getOffsetListOrderByName(int offset, int count,
                    boolean filterOutActive, boolean filterOutReadOnly,
                    boolean filterOutObsolete) {
        String condition =
                constructFilterCondition(filterOutActive, filterOutReadOnly,
                    filterOutObsolete);
        Query q =
                getSession().createQuery(
                        "from HProject p " + condition
                                + "order by UPPER(p.name)");
        q.setMaxResults(count).setFirstResult(offset);
        q.setCacheable(true).setComment("ProjectDAO.getOffsetListOrderByName");
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

        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(buildSearchFieldQuery(searchQuery, "slug"), BooleanClause.Occur.SHOULD);
        booleanQuery.add(buildSearchFieldQuery(searchQuery, "name"), BooleanClause.Occur.SHOULD);
        booleanQuery.add(buildSearchFieldQuery(searchQuery, "description"), BooleanClause.Occur.SHOULD);

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
            QueryParser parser = new QueryParser(Version.LUCENE_29, field,
                    new CaseInsensitiveWhitespaceAnalyzer(Version.LUCENE_29));

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
        final String sqlFilter = filter == null ? "" : filter;
        Query q =
                getSession()
                        .createQuery(
                                "from HProject p " +
                                "where :maintainer in elements(p.maintainers) " +
                                "and p.status <> " +
                                "'" + EntityStatus.OBSOLETE.getInitial() + "' " +
                                "and (p.name like :filter " +
                                    "or p.slug like :filter) " +
                                "order by p.name")
                        .setParameter("maintainer", maintainer)
                        .setParameter("filter", "%" + sqlFilter + "%")
                        .setFirstResult(firstResult)
                        .setMaxResults(maxResults);
        return q.list();
    }

    public int getMaintainedProjectCount(HPerson maintainer, String filter) {
        final String sqlFilter = filter == null ? "" : filter;
        Query q =
                getSession()
                        .createQuery(
                                "select count(p) from HProject p " +
                                "where :maintainer in elements(p.maintainers) " +
                                "and p.status <> " +
                                "'" + EntityStatus.OBSOLETE.getInitial() + "' " +
                                "and (p.name like :filter " +
                                    "or p.slug like :filter) "
                        )
                        .setParameter("maintainer", maintainer)
                        .setParameter("filter", "%" + sqlFilter + "%");
        return ((Long) q.uniqueResult()).intValue();
    }
}
