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
package org.zanata.rest.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.transform.ResultTransformer;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.ProjectStatisticsMatrix;
import org.zanata.rest.dto.TranslationMatrix;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.dto.stats.contribution.BaseContributionStatistic;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.util.DateUtil;
import org.zanata.webtrans.shared.model.DocumentStatus;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * Default implementation for the
 * {@link org.zanata.rest.service.StatisticsResource} interface. This is a
 * business/REST service.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("statisticsServiceImpl")
@Path(StatisticsResource.SERVICE_PATH)
@RequestScoped
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsResource {

    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;
    @Inject
    private LocaleServiceImpl localeServiceImpl;
    @Inject
    private ZPathService zPathService;
    @Inject
    private PersonDAO personDAO;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject
    private EntityManager entityManager;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    @Inject
    private ZanataIdentity identity;
    // TODO Need to refactor this method to get Message statistic by default.
    // This is to be consistent with the UI which uses message stats, and for
    // calculating remaining hours.

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, boolean includeDetails,
            boolean includeWordStats, String[] locales) {
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        if (iteration == null || !identity.hasPermission(iteration, "read")) {
            throw new NoSuchEntityException(projectSlug + "/" + iterationSlug);
        }

        List<LocaleId> localeIds;
        // if no locales are specified, search in all locales
        if (locales == null || locales.length == 0) {
            localeIds =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                    projectSlug, iterationSlug).stream()
                    .map(HLocale::getLocaleId)
                    .collect(Collectors.toList());
        } else {
            localeIds =
                Arrays.stream(locales).map(locale -> new LocaleId(locale))
                    .collect(Collectors.toList());
        }

        Map<String, TransUnitCount> transUnitIterationStats =
                projectIterationDAO
                        .getAllStatisticsForContainer(iteration.getId());
        Map<String, TransUnitWords> wordIterationStats = projectIterationDAO
                .getAllWordStatsStatistics(iteration.getId());

        ContainerTranslationStatistics versionStats =
                new ContainerTranslationStatistics();
        versionStats.setId(iterationSlug);
        versionStats.addRef(new Link(
                URI.create(zPathService
                        .generatePathForProjectIteration(iteration)),
                "statSource", "PROJ_ITER"));
        long versionTotalMsgs = projectIterationDAO
                .getTotalMessageCountForIteration(iteration.getId());
        long versionTotalWords = projectIterationDAO
                .getTotalWordCountForIteration(iteration.getId());
        for (LocaleId locId : localeIds) {
            // trans unit level stats
            // Stats might not return anything if nothing is translated
            TransUnitCount count = transUnitIterationStats
                .computeIfAbsent(locId.getId(),
                    key -> new TransUnitCount(0, 0, (int) versionTotalMsgs));

            boolean isTranslated = isTranslated(count);

            String lastModifiedBy = "";
            Date lastModifiedDate = null;
            if (isTranslated) {
                HTextFlowTarget target = localeServiceImpl
                    .getLastTranslated(projectSlug, iterationSlug, locId);
                if (target != null) {
                    lastModifiedDate = target.getLastChanged();
                    if (target.getLastModifiedBy() != null) {
                        lastModifiedBy = target.getLastModifiedBy().getAccount()
                            .getUsername();
                    }
                }
            }

            TransUnitWords wordCount = wordIterationStats
                .computeIfAbsent(locId.getId(),
                    key -> new TransUnitWords(0, 0, (int) versionTotalWords));

            TranslationStatistics transUnitStats = getMessageStats(count, locId,
                    lastModifiedDate, lastModifiedBy);
            versionStats.addStats(transUnitStats);
            // word level stats
            if (includeWordStats) {
                TranslationStatistics wordsStats = getWordsStats(wordCount,
                        locId, lastModifiedDate, lastModifiedBy);
                versionStats.addStats(wordsStats);
            }
        }
        // TODO Do in a single query
        if (includeDetails) {
            for (String docId : iteration.getDocuments().keySet()) {
                versionStats
                    .addDetailedStats(getStatisticsWithDocId(projectSlug,
                        iterationSlug, docId, includeWordStats, locales));
            }
        }
        return versionStats;
    }

    private boolean isTranslated(TransUnitCount count) {
        return count.getTranslated() != 0 || count.getNeedReview() != 0 ||
            count.getRejected() != 0;
    }

    @Deprecated
    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String docId, boolean includeWordStats,
            String[] locales) {
        return getStatisticsWithDocId(projectSlug, iterationSlug, docId,
                includeWordStats, locales);
    }

    @Override
    public ContainerTranslationStatistics getStatisticsWithDocId(
            String projectSlug, String iterationSlug, String docId,
            boolean includeWordStats, String[] locales) {
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        if (iteration == null || !identity.hasPermission(iteration, "read")) {
            throw new NoSuchEntityException(projectSlug + "/" + iterationSlug);
        }
        HDocument document = documentDAO.getByProjectIterationAndDocId(
            projectSlug, iterationSlug, docId);
        if (document == null) {
            throw new NoSuchEntityException(
                projectSlug + "/" + iterationSlug + "/" + docId);
        }

        List<LocaleId> localeIds;
        // if no locales are specified, search in all locales
        if (locales == null || locales.length == 0) {
            List<HLocale> iterationLocales =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                            projectSlug, iterationSlug);
            localeIds =
                    iterationLocales.stream().map(HLocale::getLocaleId).collect(
                            Collectors.toList());
        } else {
            localeIds = Arrays.stream(locales).map(s -> new LocaleId(s))
                    .collect(Collectors.toList());
        }

        ContainerTranslationStatistics docStatistics =
                new ContainerTranslationStatistics();
        docStatistics.setId(docId);
        docStatistics.addRef(new Link(
                URI.create(zPathService.generatePathForDocument(document)),
                "statSource", "DOC"));
        for (LocaleId localeId : localeIds) {
            ContainerTranslationStatistics docStats =
                    getDocStatistics(document.getId(), localeId);
            DocumentStatus docStatus = translationStateCacheImpl
                    .getDocumentStatus(document.getId(), localeId);
            TranslationStatistics docWordStatistic =
                    docStats.getStats(localeId.getId(), StatUnit.WORD);
            TranslationStatistics docMsgStatistic =
                    docStats.getStats(localeId.getId(), StatUnit.MESSAGE);
            docMsgStatistic
                    .setLastTranslatedBy(docStatus.getLastTranslatedBy());
            docMsgStatistic
                    .setLastTranslatedDate(docStatus.getLastTranslatedDate());
            docMsgStatistic.setLastTranslated(
                    getLastTranslated(docStatus.getLastTranslatedDate(),
                            docStatus.getLastTranslatedBy()));
            docStatistics.addStats(docMsgStatistic);
            // word level stats
            if (includeWordStats) {
                docWordStatistic
                        .setLastTranslatedBy(docStatus.getLastTranslatedBy());
                docWordStatistic.setLastTranslatedDate(
                        docStatus.getLastTranslatedDate());
                docWordStatistic.setLastTranslated(
                        getLastTranslated(docStatus.getLastTranslatedDate(),
                                docStatus.getLastTranslatedBy()));
                docStatistics.addStats(docWordStatistic);
            }
        }
        return docStatistics;
    }

    /**
     * Get contribution statistic (translations) from project-version within
     * given date range.
     *
     * Throws NoSuchEntityException if: - project/version not found or is
     * obsolete, - user not found
     *
     * Throws InvalidDateParamException if: - dateRangeParam is in wrong format,
     * - date range is over MAX_STATS_DAYS
     *
     * @param projectSlug
     *            project identifier
     * @param versionSlug
     *            version identifier
     * @param username
     *            username of contributor
     * @param dateRangeParam
     *            from..to (yyyy-mm-dd..yyyy-mm-dd), date range maximum: 365
     *            days
     */
    @Override
    public ContributionStatistics getContributionStatistics(String projectSlug,
            String versionSlug, String username, String dateRangeParam,
            boolean automatedEntry) {
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (version == null || version.getStatus() == EntityStatus.OBSOLETE
                || version.getProject().getStatus() == EntityStatus.OBSOLETE ||
                !identity.hasPermission(version, "read")) {
            throw new NoSuchEntityException(projectSlug + "/" + versionSlug);
        }
        HPerson person = findPersonOrExceptionOnNotFound(username);
        DateRange dateRange = DateRange.from(dateRangeParam);
        List<Object[]> translationData = textFlowTargetHistoryDAO
                .getUserTranslationStatisticInVersion(version.getId(),
                        person.getId(), dateRange.getFromDate().toDate(),
                        dateRange.getToDate().toDate(), automatedEntry);
        List<Object[]> reviewData = textFlowTargetHistoryDAO
                .getUserReviewStatisticInVersion(version.getId(),
                        person.getId(), dateRange.getFromDate().toDate(),
                        dateRange.getToDate().toDate(), automatedEntry);
        Map<LocaleId, LocaleStatistics> localeStatsMap = Maps.newHashMap();
        for (Object[] entry : translationData) {
            int count = ((BigDecimal) entry[0]).intValue();
            ContentState state = ContentState.values()[(int) entry[1]];
            LocaleId localeId = new LocaleId(entry[2].toString());
            BaseContributionStatistic translationStats = null;
            LocaleStatistics localeStatistics = null;
            if (localeStatsMap.containsKey(localeId)) {
                localeStatistics = localeStatsMap.get(localeId);
                translationStats = localeStatistics.getTranslationStats();
            }
            if (localeStatistics == null) {
                localeStatistics = new LocaleStatistics(localeId);
            }
            if (translationStats == null) {
                translationStats = new BaseContributionStatistic(0, 0, 0, 0);
            }
            translationStats.set(state, count);
            localeStatistics.setTranslationStats(translationStats);
            localeStatsMap.put(localeId, localeStatistics);
        }
        for (Object[] entry : reviewData) {
            int count = ((BigDecimal) entry[0]).intValue();
            ContentState state = ContentState.values()[(int) entry[1]];
            LocaleId localeId = new LocaleId(entry[2].toString());
            BaseContributionStatistic reviewStats = null;
            LocaleStatistics localeStatistics = null;
            if (localeStatsMap.containsKey(localeId)) {
                localeStatistics = localeStatsMap.get(localeId);
                reviewStats = localeStatistics.getReviewStats();
            }
            if (localeStatistics == null) {
                localeStatistics = new LocaleStatistics(localeId);
            }
            if (reviewStats == null) {
                reviewStats = new BaseContributionStatistic(0, null, null, 0);
            }
            reviewStats.set(state, count);
            localeStatistics.setReviewStats(reviewStats);
            localeStatsMap.put(localeId, localeStatistics);
        }
        return new ContributionStatistics(username,
                new ArrayList<>(localeStatsMap.values()));
    }

    private HPerson findPersonOrExceptionOnNotFound(String username) {
        HPerson person = personDAO.findByUsername(username);
        if (person == null) {
            throw new NoSuchEntityException(abbreviate(username, 23));
        }
        return person;
    }

    private TranslationStatistics getWordsStats(TransUnitWords wordCount,
            LocaleId locale, Date lastChanged, String lastModifiedBy) {
        TranslationStatistics stats =
                new TranslationStatistics(wordCount, locale.getId());
        stats = setLastTranslated(stats, lastChanged, lastModifiedBy);
        return stats;
    }

    private TranslationStatistics getMessageStats(TransUnitCount unitCount,
            LocaleId locale, Date lastChanged, String lastModifiedBy) {
        TranslationStatistics stats =
                new TranslationStatistics(unitCount, locale.getId());
        stats = setLastTranslated(stats, lastChanged, lastModifiedBy);
        return stats;
    }

    private TranslationStatistics setLastTranslated(TranslationStatistics stats,
            Date lastChanged, String lastModifiedBy) {
        stats.setLastTranslatedBy(lastModifiedBy);
        stats.setLastTranslatedDate(lastChanged);
        stats.setLastTranslated(getLastTranslated(lastChanged, lastModifiedBy));
        return stats;
    }

    private String getLastTranslated(Date lastChanged, String lastModifiedBy) {
        StringBuilder result = new StringBuilder();
        if (lastChanged != null) {
            result.append(DateUtil.formatShortDate(lastChanged));
            if (!StringUtils.isEmpty(lastModifiedBy)) {
                result.append(" by ");
                result.append(lastModifiedBy);
            }
        }
        return result.toString();
    }

    public ContainerTranslationStatistics getDocStatistics(Long documentId,
            LocaleId localeId) {
        ContainerTranslationStatistics result =
                documentDAO.getStatistics(documentId, localeId);
        return result;
    }

    /**
     * Get translation work statistics for a user in given date range.
     *
     * Throws NoSuchEntityException if: - user not found
     *
     * Throws InvalidDateParamException if: - dateRangeParam is in wrong format,
     * - date range is over MAX_STATS_DAYS
     *
     * @param username
     *            username of contributor
     * @param dateRangeParam
     *            from..to (yyyy-mm-dd..yyyy-mm-dd), date range maximum: 365
     *            days
     * @param userTimeZoneID
     *            optional user time zone ID. Will use system default in absence
     *            or GMT zone if provided time zone ID can not be understood.
     */
    @Path("user/{username}/{dateRangeParam}")
    @GET
    @Produces({ "application/json" })
    public List<TranslationMatrix> getUserWorkMatrix(
            @PathParam("username") final String username,
            @PathParam("dateRangeParam") String dateRangeParam,
            @QueryParam("userTimeZone") String userTimeZoneID) {
        HPerson person = findPersonOrExceptionOnNotFound(username);
        DateRange dateRange = DateRange.from(dateRangeParam, userTimeZoneID);
        DateTime fromDate = dateRange.getFromDate();
        DateTime toDate = dateRange.getToDate();
        DateTimeZone userZone = dateRange.getTimeZone();
        DateTimeFormatter dateFormatter =
                DateTimeFormat.forPattern(DATE_FORMAT).withZone(userZone);
        // TODO system time zone should be persisted in database
        DateTimeZone systemZone = DateTimeZone.getDefault();
        Optional<DateTimeZone> userZoneOpt;
        if (userZone.getStandardOffset(0) != systemZone.getStandardOffset(0)) {
            userZoneOpt = Optional.of(userZone);
        } else {
            userZoneOpt = Optional.absent();
        }
        List<TranslationMatrix> translationMatrixList =
                textFlowTargetHistoryDAO.getUserTranslationMatrix(person,
                        fromDate, toDate, userZoneOpt, systemZone,
                        new UserMatrixResultTransformer(entityManager,
                                identity, dateFormatter));
        return translationMatrixList;
    }


    @Override
    public Response getProjectStatisticsMatrix(final String projectSlug,
            final String versionSlug, String dateRangeParam,
            String timeZoneID) {
        if (StringUtils.isBlank(projectSlug) ||
                StringUtils.isBlank(versionSlug)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("parameter project and version are required").build();
        }
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (version == null || !identity.hasPermission(version, "read")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Project version not found:" + projectSlug + "-" + versionSlug).build();
        }
        DateRange dateRange = DateRange.from(dateRangeParam, timeZoneID);
        DateTime fromDate = dateRange.getFromDate();
        DateTime toDate = dateRange.getToDate();
        DateTimeZone timezone = dateRange.getTimeZone();
        DateTimeFormatter dateFormatter =
                DateTimeFormat.forPattern(DATE_FORMAT).withZone(timezone);
        DateTimeZone systemZone = DateTimeZone.getDefault();
        Optional<DateTimeZone> timezoneOpt;
        if (timezone.getStandardOffset(0) != systemZone.getStandardOffset(0)) {
            timezoneOpt = Optional.of(timezone);
        } else {
            timezoneOpt = Optional.absent();
        }
        List<ProjectStatisticsMatrix> translationMatrixList =
                textFlowTargetHistoryDAO.getProjectTranslationMatrix(version,
                        fromDate, toDate, timezoneOpt, systemZone,
                        new ProjectMatrixResultTransformer(entityManager,
                                dateFormatter));
        return Response.ok(translationMatrixList).build();
    }

    public static class ProjectMatrixResultTransformer
            implements ResultTransformer {
        private static final long serialVersionUID = 1L;
        @SuppressFBWarnings("SE_BAD_FIELD")
        private final EntityManager entityManager;
        private transient final DateTimeFormatter dateFormatter;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            String savedDate = dateFormatter
                    .print(new DateTime(tuple[0]).toDate().getTime());
            HLocale locale = entityManager.find(HLocale.class,
                    ((BigInteger) tuple[1]).longValue());
            String localeDisplayName = locale.retrieveDisplayName();
            LocaleId localeId = locale.getLocaleId();
            ContentState savedState = ContentState.values()[(int) tuple[2]];
            long wordCount = ((BigDecimal) tuple[3]).toBigInteger().longValue();
            return new ProjectStatisticsMatrix(savedDate, localeId,
                    localeDisplayName, savedState, wordCount);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public List transformList(List collection) {
            return collection;
        }

        @java.beans.ConstructorProperties({ "entityManager", "dateFormatter" })
        public ProjectMatrixResultTransformer(final EntityManager entityManager,
                final DateTimeFormatter dateFormatter) {
            this.entityManager = entityManager;
            this.dateFormatter = dateFormatter;
        }
    }

    public static class UserMatrixResultTransformer
            implements ResultTransformer {
        private static final long serialVersionUID = 1L;
        @SuppressFBWarnings("SE_BAD_FIELD")
        private final EntityManager entityManager;
        @SuppressFBWarnings("SE_BAD_FIELD")
        private final DateTimeFormatter dateFormatter;
        private final ZanataIdentity identity;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            String savedDate = dateFormatter
                    .print(new DateTime(tuple[0]).toDate().getTime());
            HProjectIteration iteration =
                    entityManager.find(HProjectIteration.class,
                            ((BigInteger) tuple[1]).longValue());
            String projectSlug = iteration.getProject().getSlug();
            String projectName = iteration.getProject().getName();
            String versionSlug = iteration.getSlug();
            if (!identity.hasPermission(iteration.getProject(), "read")) {
                projectSlug = null;
                projectName = null;
                versionSlug = null;
            }
            HLocale locale = entityManager.find(HLocale.class,
                    ((BigInteger) tuple[2]).longValue());
            String localeDisplayName = locale.retrieveDisplayName();
            LocaleId localeId = locale.getLocaleId();
            ContentState savedState = ContentState.values()[(int) tuple[3]];
            long wordCount = ((BigDecimal) tuple[4]).toBigInteger().longValue();
            return new TranslationMatrix(savedDate, projectSlug, projectName,
                    versionSlug, localeId, localeDisplayName, savedState,
                    wordCount);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public List transformList(List collection) {
            return collection;
        }

        @java.beans.ConstructorProperties({ "entityManager", "dateFormatter" })
        public UserMatrixResultTransformer(final EntityManager entityManager,
                @Nullable final ZanataIdentity identity,
                final DateTimeFormatter dateFormatter) {
            this.entityManager = entityManager;
            this.identity = identity;
            this.dateFormatter = dateFormatter;
        }
    }
}
