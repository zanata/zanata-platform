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
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.exception.InvalidDateParamException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.dto.stats.contribution.BaseContributionStatistic;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.util.DateUtil;
import org.zanata.util.StatisticsUtil;
import org.zanata.webtrans.shared.model.DocumentStatus;

/**
 * Default implementation for the
 * {@link org.zanata.rest.service.StatisticsResource} interface. This is a
 * business/REST service.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("statisticsServiceImpl")
@Path(StatisticsResource.SERVICE_PATH)
@Scope(ScopeType.STATELESS)
@Slf4j
public class StatisticsServiceImpl implements StatisticsResource {
    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private DocumentDAO documentDAO;

    @In
    private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

    @In
    private LocaleServiceImpl localeServiceImpl;

    @In
    private ZPathService zPathService;

    @In
    private PersonDAO personDAO;

    @In
    private TranslationStateCache translationStateCacheImpl;

    private static final int MAX_STATS_DAYS = 365;

    // TODO Need to refactor this method to get Message statistic by default.
    // This is to be consistance with UI which uses message stats, and for
    // calculating remaining hours.
    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, boolean includeDetails,
            boolean includeWordStats, String[] locales) {
        LocaleId[] localeIds;

        // if no locales are specified, search in all locales
        if (locales.length == 0) {
            List<HLocale> iterationLocales =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, iterationSlug);
            localeIds = new LocaleId[iterationLocales.size()];
            for (int i = 0, iterationLocalesSize = iterationLocales.size(); i < iterationLocalesSize; i++) {
                HLocale loc = iterationLocales.get(i);
                localeIds[i] = loc.getLocaleId();
            }
        } else {
            localeIds = new LocaleId[locales.length];
            for (int i = 0; i < locales.length; i++) {
                localeIds[i] = new LocaleId(locales[i]);
            }
        }

        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);

        if (iteration == null) {
            throw new NoSuchEntityException(projectSlug + "/" + iterationSlug);
        }

        Map<String, TransUnitCount> transUnitIterationStats =
                projectIterationDAO.getAllStatisticsForContainer(iteration
                        .getId());
        Map<String, TransUnitWords> wordIterationStats =
                projectIterationDAO
                        .getAllWordStatsStatistics(iteration.getId());

        ContainerTranslationStatistics iterationStats =
                new ContainerTranslationStatistics();
        iterationStats.setId(iterationSlug);
        iterationStats.addRef(new Link(URI.create(zPathService
                .generatePathForProjectIteration(iteration)), "statSource",
                "PROJ_ITER"));
        long iterationTotalMssgs =
                projectIterationDAO
                        .getTotalMessageCountForIteration(iteration.getId());
        long iterationTotalWords =
                projectIterationDAO.getTotalWordCountForIteration(iteration
                        .getId());

        for (LocaleId locId : localeIds) {
            // trans unit level stats
            TransUnitCount count = transUnitIterationStats.get(locId.getId());
            // Stats might not return anything if nothing is translated
            if (count == null) {
                count = new TransUnitCount(0, 0, (int) iterationTotalMssgs);
            }

            HTextFlowTarget target =
                    localeServiceImpl.getLastTranslated(projectSlug,
                            iterationSlug, locId);

            String lastModifiedBy = "";
            Date lastModifiedDate = null;

            if (target != null) {
                lastModifiedDate = target.getLastChanged();
                if (target.getLastModifiedBy() != null) {
                    lastModifiedBy =
                            target.getLastModifiedBy().getAccount()
                                    .getUsername();
                }
            }

            TransUnitWords wordCount = wordIterationStats.get(locId.getId());
            if (wordCount == null) {
                wordCount = new TransUnitWords(0, 0, (int) iterationTotalWords);
            }

            TranslationStatistics transUnitStats =
                    getMessageStats(count, locId, lastModifiedDate,
                            lastModifiedBy);
            transUnitStats.setRemainingHours(StatisticsUtil
                    .getRemainingHours(wordCount));
            iterationStats.addStats(transUnitStats);

            // word level stats
            if (includeWordStats) {
                TranslationStatistics wordsStats =
                        getWordsStats(wordCount, locId, lastModifiedDate,
                                lastModifiedBy);
                wordsStats.setRemainingHours(StatisticsUtil
                        .getRemainingHours(wordCount));
                iterationStats.addStats(wordsStats);
            }
        }

        // TODO Do in a single query
        if (includeDetails) {
            for (String docId : iteration.getDocuments().keySet()) {
                iterationStats.addDetailedStats(this.getStatistics(projectSlug,
                        iterationSlug, docId, includeWordStats, locales));
            }
        }

        return iterationStats;
    }

    @Override
    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String docId, boolean includeWordStats,
            String[] locales) {
        LocaleId[] localeIds;

        // if no locales are specified, search in all locales
        if (locales.length == 0) {
            List<HLocale> iterationLocales =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, iterationSlug);
            localeIds = new LocaleId[iterationLocales.size()];
            for (int i = 0, iterationLocalesSize = iterationLocales.size(); i < iterationLocalesSize; i++) {
                HLocale loc = iterationLocales.get(i);
                localeIds[i] = loc.getLocaleId();
            }
        } else {
            localeIds = new LocaleId[locales.length];
            for (int i = 0; i < locales.length; i++) {
                localeIds[i] = new LocaleId(locales[i]);
            }
        }

        HDocument document =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        iterationSlug, docId);

        if (document == null) {
            throw new NoSuchEntityException(projectSlug + "/" + iterationSlug
                    + "/" + docId);
        }

        ContainerTranslationStatistics docStatistics =
                new ContainerTranslationStatistics();
        docStatistics.setId(docId);
        docStatistics.addRef(new Link(URI.create(zPathService
                .generatePathForDocument(document)), "statSource", "DOC"));

        for (LocaleId localeId : localeIds) {
            ContainerTranslationStatistics docStats =
                    getDocStatistics(document.getId(), localeId);

            DocumentStatus docStatus =
                    translationStateCacheImpl.getDocumentStatus(
                            document.getId(), localeId);

            TranslationStatistics docWordStatistic =
                    docStats.getStats(localeId.getId(), StatUnit.WORD);
            TranslationStatistics docMsgStatistic =
                    docStats.getStats(localeId.getId(), StatUnit.MESSAGE);

            docMsgStatistic
                    .setLastTranslatedBy(docStatus.getLastTranslatedBy());
            docMsgStatistic.setLastTranslatedDate(docStatus
                    .getLastTranslatedDate());
            docMsgStatistic.setLastTranslated(getLastTranslated(
                    docStatus.getLastTranslatedDate(),
                    docStatus.getLastTranslatedBy()));
            docStatistics.addStats(docMsgStatistic);

            // word level stats
            if (includeWordStats) {
                docWordStatistic.setLastTranslatedBy(docStatus
                        .getLastTranslatedBy());
                docWordStatistic.setLastTranslatedDate(docStatus
                        .getLastTranslatedDate());
                docWordStatistic.setLastTranslated(getLastTranslated(
                        docStatus.getLastTranslatedDate(),
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
     * Throws NoSuchEntityException if:
     * - project/version not found or is obsolete,
     * - user not found
     *
     * Throws InvalidDateParamException if:
     * - dateRangeParam is in wrong format,
     * - date range is over MAX_STATS_DAYS
     *
     * @param projectSlug
     *            project identifier
     * @param versionSlug
     *            version identifier
     * @param username
     *            username of contributor
     * @param dateRangeParam
     *            from..to (yyyy-mm-dd..yyyy-mm-dd),
     *            date range maximum: 365 days
     */
    @Override
    public ContributionStatistics getContributionStatistics(String projectSlug,
            String versionSlug, String username, String dateRangeParam) {

        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (version == null || version.getStatus() == EntityStatus.OBSOLETE ||
                version.getProject().getStatus() == EntityStatus.OBSOLETE) {
            throw new NoSuchEntityException(projectSlug + "/" + versionSlug);
        }

        HPerson person = personDAO.findByUsername(username);
        if (person == null) {
            throw new NoSuchEntityException(username);
        }

        String[] dateRange = dateRangeParam.split("\\.\\.");
        if (dateRange.length != 2) {
            throw new InvalidDateParamException(dateRangeParam);
        }

        Date fromDate, toDate;

        try {
            fromDate = DateUtil.getDate(dateRange[0], DATE_FORMAT);
            toDate = DateUtil.getDate(dateRange[1], DATE_FORMAT);

            fromDate = DateUtil.getStartOfDay(fromDate);
            toDate = DateUtil.getEndOfTheDay(toDate);

            if (fromDate.after(toDate) || !DateUtil.isDatesInRange(fromDate,
                    toDate, MAX_STATS_DAYS)) {
                throw new InvalidDateParamException(dateRangeParam);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidDateParamException(dateRangeParam);
        }

        LocaleStatistics localeStatistics = new LocaleStatistics();

        List<Object[]> data =
                textFlowTargetHistoryDAO.getUserContributionStatisticInVersion(
                        version.getId(), person.getId(), fromDate, toDate);

        for (Object[] entry : data) {
            int count = ((BigDecimal) entry[0]).intValue();
            ContentState state = ContentState.values()[(int) entry[1]];
            LocaleId localeId = new LocaleId(entry[2].toString());

            BaseContributionStatistic stats;
            if (localeStatistics.containsKey(localeId)) {
                stats = localeStatistics.get(localeId);
            } else {
                stats = new BaseContributionStatistic(0, 0, 0, 0);
            }
            stats.set(state, count);
            localeStatistics.put(localeId, stats);
        }

        ContributionStatistics result = new ContributionStatistics();
        result.put(username, localeStatistics);

        return result;
    }

    private TranslationStatistics getWordsStats(TransUnitWords wordCount,
            LocaleId locale, Date lastChanged, String lastModifiedBy) {
        TranslationStatistics stats =
                new TranslationStatistics(wordCount, locale.getId());
        stats.setLastTranslatedBy(lastModifiedBy);
        stats.setLastTranslatedDate(lastChanged);
        stats.setLastTranslated(getLastTranslated(lastChanged, lastModifiedBy));

        return stats;
    }

    private TranslationStatistics getMessageStats(TransUnitCount unitCount,
            LocaleId locale, Date lastChanged, String lastModifiedBy) {
        TranslationStatistics stats =
                new TranslationStatistics(unitCount, locale.getId());
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

        TranslationStatistics wordStatistics =
                result.getStats(localeId.getId(), StatUnit.WORD);
        wordStatistics.setRemainingHours(StatisticsUtil
                .getRemainingHours(wordStatistics));

        TranslationStatistics msgStatistics =
                result.getStats(localeId.getId(), StatUnit.MESSAGE);
        msgStatistics.setRemainingHours(StatisticsUtil
                .getRemainingHours(wordStatistics));

        return result;
    }
}
