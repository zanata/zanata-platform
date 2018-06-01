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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.EntityTag;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.StatusCount;
import org.zanata.rest.service.DateRange;
import org.zanata.ui.model.statistic.MessageStatistic;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.HashUtil;
import org.zanata.util.StatisticsUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@RequestScoped
public class ProjectIterationDAO extends
        AbstractDAOImpl<HProjectIteration, Long> {

    private static final long serialVersionUID = 5643663448633778779L;

    public ProjectIterationDAO() {
        super(HProjectIteration.class);
    }

    public ProjectIterationDAO(Session session) {
        super(HProjectIteration.class, session);
    }

    @Nullable
    public HProjectIteration getBySlug(@Nonnull String projectSlug,
            @Nonnull String iterationSlug) {
        HProject project =
                (HProject) getSession().byNaturalId(HProject.class)
                        .using("slug", projectSlug).load();
        return getBySlug(project, iterationSlug);
    }

    @Nullable
    public HProjectIteration getBySlug(@Nullable HProject project,
            @Nullable String iterationSlug) {
        if (project == null || StringUtils.isEmpty(iterationSlug)) {
            return null;
        }
        return getSession()
                .byNaturalId(HProjectIteration.class)
                .using("slug", iterationSlug).using("project", project).load();
    }

    public List<HProjectIteration> getByProjectSlug(
            @Nonnull String projectSlug, EntityStatus... includeStatus) {
        if (StringUtils.isEmpty(projectSlug)) {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("from HProjectIteration version where ").
                append("version.project.slug = :projectSlug ");

        if (includeStatus != null) {
            sb.append("and version.status in :includesStatus");
        }

        Query q = getSession().createQuery(sb.toString());
        q.setParameter("projectSlug", projectSlug);

        if (includeStatus != null) {
            q.setParameterList("includesStatus",
                    Lists.newArrayList(includeStatus));
        }
        q.setComment("ProjectIterationDAO.getByProjectSlug");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> results = q.list();
        return results;

    }

    public List<HProjectIteration> getByGroup(HIterationGroup group) {
        Query q =
                getSession()
                        .createQuery(
                                "from HProjectIteration as iter where iter.status<>:OBSOLETE AND :group in elements(iter.groups)");
        q.setParameter("OBSOLETE", EntityStatus.OBSOLETE);
        q.setParameter("group", group);
        q.setComment("ProjectIterationDAO.getByGroup");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> results = q.list();
        return results;
    }

    /**
     * @see DocumentDAO#getStatistics(long, LocaleId)
     * @param iterationId
     * @param localeId
     * @return
     */
    public TransUnitCount getStatisticsForContainer(Long iterationId,
            LocaleId localeId) {
        List<StatusCount> stats = getMessageStatusCount(iterationId, localeId);

        TransUnitCount stat = new TransUnitCount();

        for (StatusCount count : stats) {
            stat.set(count.status, count.count.intValue());
        }

        Long totalCount = getTotalWordCountForIteration(iterationId);

        stat.set(ContentState.New, totalCount.intValue()
                - (stat.getApproved() + stat.getNeedReview()));

        return stat;
    }

    /**
     * @see DocumentDAO#getStatistics(long, LocaleId)
     * @param iterationId
     * @param localeId
     * @return
     */
    public TransUnitWords getWordStatsForContainer(Long iterationId,
            LocaleId localeId) {

        List<StatusCount> stats = getWordStatusCount(iterationId, localeId);

        TransUnitWords stat = new TransUnitWords();

        for (StatusCount count : stats) {
            stat.set(count.status, count.count.intValue());
        }

        Long totalCount = getTotalWordCountForIteration(iterationId);

        stat.set(ContentState.New, totalCount.intValue()
                - (stat.getApproved() + stat.getNeedReview()));

        return stat;
    }

    public WordStatistic getWordStatistics(Long iterationId, LocaleId localeId) {

        List<StatusCount> stats = getWordStatusCount(iterationId, localeId);

        WordStatistic wordStatistic = new WordStatistic();

        for (StatusCount count : stats) {
            wordStatistic.set(count.status, count.count.intValue());
        }

        Long totalCount = getTotalWordCountForIteration(iterationId);

        wordStatistic.set(
                ContentState.New,
                totalCount.intValue()
                        - (wordStatistic.getTranslated()
                                + wordStatistic.getNeedReview() + wordStatistic
                                    .getRejected()));
        return wordStatistic;
    }

    public MessageStatistic getMessageStatistics(Long iterationId,
            LocaleId localeId) {

        List<StatusCount> stats = getMessageStatusCount(iterationId, localeId);

        MessageStatistic messageStatistic = new MessageStatistic();

        for (StatusCount count : stats) {
            messageStatistic.set(count.status, count.count.intValue());
        }

        Long totalCount = getTotalMessageCountForIteration(iterationId);

        messageStatistic
                .set(ContentState.New,
                        totalCount.intValue()
                                - (messageStatistic.getApproved()
                                        + messageStatistic.getTranslated()
                                        + messageStatistic.getNeedReview() + messageStatistic
                                            .getRejected()));
        return messageStatistic;
    }

    public List<StatusCount> getWordStatusCount(Long iterationId,
            LocaleId localeId) {
        Query q =
                getSession()
                        .createQuery(
                                "select new org.zanata.model.StatusCount(tft.state, "
                                        + "sum(tft.textFlow.wordCount)) "
                                        + "from HTextFlowTarget tft "
                                        + "where tft.textFlow.document.projectIteration.id = :id "
                                        + "and tft.locale.localeId = :locale "
                                        + "and tft.textFlow.obsolete = false "
                                        + "and tft.textFlow.document.obsolete = false "
                                        + "group by tft.state ");
        q.setParameter("id", iterationId).setParameter("locale", localeId);
        q.setCacheable(true)
                .setComment("ProjectIterationDAO.getWordStatistics");
        @SuppressWarnings("unchecked")
        List<StatusCount> stats = q.list();

        return stats;
    }

    public List<StatusCount> getMessageStatusCount(Long iterationId,
            LocaleId localeId) {
        Query q =
                getSession()
                        .createQuery(
                                "select new org.zanata.model.StatusCount(tft.state, count(tft)) "
                                        + "from HTextFlowTarget tft "
                                        + "where tft.textFlow.document.projectIteration.id = :id "
                                        + "and tft.locale.localeId = :locale "
                                        + "and tft.textFlow.obsolete = false "
                                        + "and tft.textFlow.document.obsolete = false "
                                        + "group by tft.state");
        q.setParameter("id", iterationId).setParameter("locale", localeId);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getMessageStatusCount");
        @SuppressWarnings("unchecked")
        List<StatusCount> stats = q.list();

        return stats;
    }

    public EntityTag getResourcesETag(HProjectIteration projectIteration) {
        Query q =
                getSession().createQuery(
                        "select d.revision from HDocument d "
                                + "where d.projectIteration =:iteration "
                                + "and d.obsolete = false").setParameter(
                        "iteration", projectIteration);
        q.setCacheable(true).setComment("ProjectIterationDAO.getResourcesETag");
        @SuppressWarnings("unchecked")
        List<Integer> revisions = q.list();

        int hashCode = 1;
        for (int revision : revisions) {
            hashCode = 31 * hashCode + revision;
        }

        String hash = HashUtil.generateHash(String.valueOf(hashCode));

        return EntityTag.valueOf(hash);
    }

    /**
     * @param versionId
     * @return
     */
    public Map<String, TransUnitWords> getAllWordStatsStatistics(
        Long versionId) {
        Query q =
                getSession()
                        .createQuery(
                                "select tft.state, "
                                        + "sum(tft.textFlow.wordCount), "
                                        + "tft.locale.localeId "
                                        + "from HTextFlowTarget tft "
                                        + "where tft.textFlow.document.projectIteration.id = :id "
                                        + "and tft.textFlow.obsolete = false "
                                        + "and tft.textFlow.document.obsolete = false "
                                        + "group by tft.state, tft.locale.localeId");
        q.setParameter("id", versionId);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getAllWordStatsStatistics");
        @SuppressWarnings("unchecked")
        List<Object[]> stats = q.list();

        Map<String, TransUnitWords> result = new HashMap<>();

        for (Object[] count : stats) {
            TransUnitWords stat;
            ContentState state = (ContentState) count[0];
            Long word = (Long) count[1];
            LocaleId locale = (LocaleId) count[2];
            stat = result
                .computeIfAbsent(locale.getId(), key -> new TransUnitWords());
            stat.set(state, word.intValue());
        }

        Long totalCount = getTotalWordCountForIteration(versionId);
        for (TransUnitWords count : result.values()) {
            count.set(ContentState.New,
                    StatisticsUtil.calculateUntranslated(totalCount, count));
        }
        return result;
    }

    /**
     * Retreives translation unit level statistics for a project iteration.
     *
     * @param versionId
     *            The numeric id for a Project iteration.
     * @return A map of translation unit counts indexed by a locale id string.
     */
    public Map<String, TransUnitCount> getAllStatisticsForContainer(
            Long versionId) {
        Query q =
                getSession()
                        .createQuery(
                                "select new map(tft.state as state, "
                                        + "count(tft) as count, "
                                        + "tft.locale.localeId as locale) "
                                        + "from HTextFlowTarget tft "
                                        + "where tft.textFlow.document.projectIteration.id = :id "
                                        + " and tft.textFlow.obsolete = false"
                                        + " and tft.textFlow.document.obsolete = false"
                                        + " group by tft.state, tft.locale");
        q.setParameter("id", versionId);
        q.setComment("ProjectIterationDAO.getAllStatisticsForContainer");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stats = q.list();
        Map<String, TransUnitCount> retVal = new HashMap<>();

        for (Map<String, Object> row : stats) {
            ContentState state = (ContentState) row.get("state");
            Long count = (Long) row.get("count");
            LocaleId localeId = (LocaleId) row.get("locale");

            TransUnitCount transUnitCount = retVal
                .computeIfAbsent(localeId.getId(), key -> new TransUnitCount());

            transUnitCount.set(state, count.intValue());
        }

        Long versionTotalCount = getTotalMessageCountForIteration(versionId);
        for (TransUnitCount stat : retVal.values()) {
            stat.set(ContentState.New,
                    StatisticsUtil.calculateUntranslated(versionTotalCount, stat));
        }
        return retVal;
    }

    public Long getTotalMessageCountForIteration(Long iterationId) {
        Query q =
                getSession().createQuery(
                        "select count(tf) from HTextFlow tf "
                                + "where tf.document.projectIteration.id = :id"
                                + " and tf.obsolete = false"
                                + " and tf.document.obsolete = false");
        q.setParameter("id", iterationId);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalMessageCountForIteration");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0L : totalCount;
    }

    public Long getTotalWordCountForIteration(Long iterationId) {
        Query q =
                getSession().createQuery(
                        "select sum(tf.wordCount) from HTextFlow tf "
                                + "where tf.document.projectIteration.id = :id"
                                + " and tf.obsolete = false"
                                + " and tf.document.obsolete = false");
        q.setParameter("id", iterationId);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalWordCountForGroup");
        Long totalCount = (Long) q.uniqueResult();
        return totalCount == null ? 0L : totalCount;
    }

    public int getTotalProjectIterCount() {
        String query = "select count(*) from HProjectIteration";
        Query q = getSession().createQuery(query);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalProjectIterCount");
        Long totalCount = (Long) q.uniqueResult();

        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalActiveProjectIterCount() {
        Query q =
                getSession().createQuery(
                        "select count(*) from HProjectIteration t "
                                + "where t.status = :status");
        q.setParameter("status", EntityStatus.ACTIVE);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalActiveProjectIterCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalReadOnlyProjectIterCount() {
        Query q =
                getSession().createQuery(
                        "select count(*) from HProjectIteration t "
                                + "where t.status = :status");
        q.setParameter("status", EntityStatus.READONLY);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalReadOnlyProjectIterCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public int getTotalObsoleteProjectIterCount() {
        Query q =
                getSession().createQuery(
                        "select count(*) from HProjectIteration t "
                                + "where t.status = :status");
        q.setParameter("status", EntityStatus.OBSOLETE);
        q.setCacheable(true).setComment(
                "ProjectIterationDAO.getTotalObsoleteProjectIterCount");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null)
            return 0;
        return totalCount.intValue();
    }

    public List<HProjectIteration>
            searchLikeSlugOrProjectSlug(String searchTerm) {
        if (StringUtils.isEmpty(searchTerm)) {
            return new ArrayList<HProjectIteration>();
        }
        Query q =
                getSession().createQuery(
                        "from HProjectIteration t "
                                + "where (lower(t.slug) LIKE :searchTerm "
                                + "OR lower(t.project.slug) LIKE :searchTerm "
                                + "OR lower(t.project.name) LIKE :searchTerm) "
                                + "AND t.status<>:OBSOLETE");
        q.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        q.setParameter("OBSOLETE", EntityStatus.OBSOLETE);
        q.setCacheable(false).setComment(
                "ProjectIterationDAO.searchLikeSlugOrProjectSlug");

        @SuppressWarnings("unchecked")
        List<HProjectIteration> result = q.list();
        return result;
    }

    public List<HProjectIteration> searchByProjectId(Long projectId) {
        Query q =
                getSession().createQuery(
                        "from HProjectIteration t "
                                + "where t.project.id = :projectId "
                                + "order by t.creationDate DESC");
        q.setParameter("projectId", projectId);
        q.setCacheable(false).setComment("ProjectIterationDAO.findByProjectId");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> result = q.list();
        return result;
    }

    public List<HProjectIteration> searchByProjectsExcludeObsolete(
            List<HProject> projects) {
        Preconditions.checkArgument(
                !projects.isEmpty()
                        && projects.size() < MAX_PARAMS_IN_IN_CLAUSE,
                "invalid projectIds list");
        String query =
                "FROM HProjectIteration t WHERE t.project.id in (:projectIds) "
                        + "AND t.status != :status"
                        + " order by t.project.id, t.creationDate DESC";
        List<Long> projectIds = projects.stream().map(HProject::getId).collect(
                Collectors.toList());
        Query q = getSession().createQuery(query)
                .setParameterList("projectIds", projectIds)
                .setParameter("status", EntityStatus.OBSOLETE)
                .setCacheable(false).setComment(
                        "ProjectIterationDAO.searchByProjectIdsExcludeObsolete");
        @SuppressWarnings("unchecked")
        List<HProjectIteration> result = q.list();
        return result;
    }

    public List<HAccount> getContributors(String projectSlug, String versionSlug,
        DateRange dataRange) {
            String query =
                "select account from HAccount account where account.id in " +
                    "(select tft.translator.account.id from HTextFlowTarget tft " +
                    "where tft.textFlow.document.projectIteration.slug = :versionSlug " +
                    "and tft.textFlow.document.projectIteration.project.slug =:projectSlug and tft.lastChanged between :fromDate and :toDate) " +
                    "or account.id in (select tft.reviewer.account.id from HTextFlowTarget tft " +
                    "where tft.textFlow.document.projectIteration.slug = :versionSlug " +
                    "and tft.textFlow.document.projectIteration.project.slug =:projectSlug and tft.lastChanged between :fromDate and :toDate) " +
                    "or account.id in (select tfth.translator.account.id from HTextFlowTargetHistory tfth " +
                    "where tfth.textFlowTarget.textFlow.document.projectIteration.slug = :versionSlug " +
                    "and tfth.textFlowTarget.textFlow.document.projectIteration.project.slug =:projectSlug and tfth.lastChanged between :fromDate and :toDate) " +
                    "or account.id in (select tfth.reviewer.account.id from HTextFlowTargetHistory tfth " +
                    "where tfth.textFlowTarget.textFlow.document.projectIteration.slug = :versionSlug " +
                    "and tfth.textFlowTarget.textFlow.document.projectIteration.project.slug =:projectSlug and tfth.lastChanged between :fromDate and :toDate) group by account.username ";

        Query q = getSession().createQuery(query);
        q.setParameter("versionSlug", versionSlug);
        q.setParameter("projectSlug", projectSlug);
        q.setTimestamp("fromDate", dataRange.getFromDate().toDate());
        q.setTimestamp("toDate", dataRange.getToDate().toDate());
        q.setCacheable(true).setComment(
            "ProjectIterationDAO.getContributors");
        @SuppressWarnings("unchecked")
        List<HAccount> result = q.list();
        return result;
    }

    public int getTotalDocCount(String projectSlug, String iterationSlug) {
        String query = "select count(doc) from HDocument doc " +
                "where doc.projectIteration.slug=:iterationSlug" +
                "and doc.projectIteration.project.slug=:projectSlug " +
                "and doc.projectIteration.status <> :obsolete " +
                "and doc.obsolete = false";

        Query q = getSession().createQuery(query)
                .setParameter("iterationSlug", iterationSlug)
                .setParameter("projectSlug", projectSlug)
                .setParameter("obsolete", EntityStatus.OBSOLETE);
        return ((Long) q.uniqueResult()).intValue();
    }
}
