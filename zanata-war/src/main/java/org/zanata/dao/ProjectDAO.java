package org.zanata.dao;

import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
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
                    boolean filterActive, boolean filterReadOnly,
                    boolean filterObsolete) {
        String condition =
                constructFilterCondition(filterActive, filterReadOnly,
                        filterObsolete);
        Query q =
                getSession().createQuery(
                        "from HProject p " + condition
                                + "order by UPPER(p.name)");
        q.setMaxResults(count).setFirstResult(offset);
        q.setCacheable(true).setComment("ProjectDAO.getOffsetListOrderByName");
        return q.list();
    }

    public int getFilterProjectSize(boolean filterActive,
            boolean filterReadOnly, boolean filterObsolete) {
        String query =
                "select count(*) from HProject p "
                        + constructFilterCondition(filterActive,
                                filterReadOnly, filterObsolete);
        Query q = getSession().createQuery(query.toString());
        q.setCacheable(true).setComment("ProjectDAO.getFilterProjectSize");
        Long totalCount = (Long) q.uniqueResult();

        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    private String constructFilterCondition(boolean filterActive,
            boolean filterReadOnly, boolean filterObsolete) {
        StringBuilder condition = new StringBuilder();
        if (filterActive || filterReadOnly || filterObsolete) {
            condition.append("where ");
        }

        if (filterActive) {
            // TODO bind this as a parameter
            condition.append("p.status <> '" + EntityStatus.ACTIVE.getInitial()
                    + "' ");
        }

        if (filterReadOnly) {
            if (filterActive) {
                condition.append("and ");
            }

            // TODO bind this as a parameter
            condition.append("p.status <> '"
                    + EntityStatus.READONLY.getInitial() + "' ");
        }

        if (filterObsolete) {
            if (filterActive || filterReadOnly) {
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
                                "from HProjectIteration t where t.project.slug = :projectSlug");
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
        FullTextQuery query = getTextQuery(searchQuery, includeObsolete);
        return query.setMaxResults(maxResult).setFirstResult(firstResult)
                .getResultList();
    }

    public int getQueryProjectSize(@Nonnull String searchQuery,
            boolean includeObsolete) throws ParseException {
        FullTextQuery query = getTextQuery(searchQuery, includeObsolete);
        return query.getResultSize();
    }

    private FullTextQuery getTextQuery(@Nonnull String searchQuery,
            boolean includeObsolete) {
        searchQuery = QueryParser.escape(searchQuery.toLowerCase());

        PrefixQuery slugQuery = new PrefixQuery(new Term("slug", searchQuery));
        PrefixQuery nameQuery = new PrefixQuery(new Term("name", searchQuery));
        PrefixQuery descQuery =
                new PrefixQuery(new Term("description", searchQuery));

        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(slugQuery, BooleanClause.Occur.SHOULD);
        booleanQuery.add(nameQuery, BooleanClause.Occur.SHOULD);
        booleanQuery.add(descQuery, BooleanClause.Occur.SHOULD);

        if (!includeObsolete) {
            TermQuery obsoleteStateQuery =
                    new TermQuery(new Term(IndexFieldLabels.ENTITY_STATUS,
                            EntityStatus.OBSOLETE.toString().toLowerCase()));
            booleanQuery.add(obsoleteStateQuery, BooleanClause.Occur.MUST_NOT);
        }

        return entityManager.createFullTextQuery(booleanQuery, HProject.class);
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
        return ((Long)q.uniqueResult()).intValue();
    }



    /**
     * @param project A project
     * @param account The user for which the last translated date.
     * @return A date indicating the last time in which account's user
     * translated it.
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
                        .setParameter("project", project);
        return (Date)q.uniqueResult();
    }

    public List<HProject> getProjectsForMaintainer(HPerson maintainer,
            int firstResult, int maxResults) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProject p " +
                                "where :maintainer in elements(p.maintainers) " +
                                "order by p.name")
                        .setParameter("maintainer", maintainer)
                        .setFirstResult(firstResult)
                        .setMaxResults(maxResults);
        return q.list();
    }

    public int getMaintainedProjectCount(HPerson maintainer) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(p) from HProject p " +
                                "where :maintainer in elements(p.maintainers)"
                        )
                        .setParameter("maintainer", maintainer);
        return ((Long)q.uniqueResult()).intValue();
    }
}
