/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.ZanataMessages;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHomeAction")
@Scope(ScopeType.PAGE)
public class VersionGroupHomeAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In
    private ZanataMessages zanataMessages;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private VersionGroupDAO versionGroupDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @Getter
    @Setter
    private String slug;

    @Getter
    private boolean pageRendered = false;

    @Getter
    @Setter
    private HLocale selectedLocale;

    @Getter
    @Setter
    private HProjectIteration selectedVersion;

    @Getter
    private OverallStatistic overallStatistic;

    private List<HLocale> activeLocales;

    private List<HProjectIteration> projectIterations;

    private Map<VersionLocaleKey, WordStatistic> statisticMap;

    private Map<LocaleId, List<HProjectIteration>> missingLocaleVersionMap;

    @Getter
    private final SortingType languageSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE));

    @Getter
    private SortingType projectSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS));

    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());

    private final VersionComparator versionComparator = new VersionComparator(
            getProjectSortingList());

    public void setPageRendered(boolean pageRendered) {
        if (pageRendered) {
            loadStatistic();
        }
        this.pageRendered = pageRendered;
    }

    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale locale, HLocale locale2) {
            final HLocale item1, item2;

            if (sortingType.isDescending()) {
                item1 = locale;
                item2 = locale2;
            } else {
                item1 = locale2;
                item2 = locale;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1 =
                        getStatisticsForLocale(item1.getLocaleId());
                WordStatistic wordStatistic2 =
                        getStatisticsForLocale(item2.getLocaleId());

                if (selectedSortOption
                        .equals(SortingType.SortOption.PERCENTAGE)) {
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                } else if (selectedSortOption
                        .equals(SortingType.SortOption.HOURS)) {
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                }
            } else {
                return item1.retrieveDisplayName().compareTo(
                        item2.retrieveDisplayName());
            }
            return 0;
        }
    }

    private class VersionComparator implements Comparator<HProjectIteration> {
        private SortingType sortingType;

        public VersionComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int
                compare(HProjectIteration version, HProjectIteration version2) {
            final HProjectIteration item1, item2;

            if (sortingType.isDescending()) {
                item1 = version;
                item2 = version2;
            } else {
                item1 = version2;
                item2 = version;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1 = new WordStatistic();
                WordStatistic wordStatistic2 = new WordStatistic();
                if (selectedLocale != null) {
                    wordStatistic1 =
                            statisticMap
                                    .get(new VersionLocaleKey(item1.getId(),
                                            selectedLocale.getLocaleId()));
                    wordStatistic2 =
                            statisticMap
                                    .get(new VersionLocaleKey(item2.getId(),
                                            selectedLocale.getLocaleId()));
                }

                if (selectedSortOption
                        .equals(SortingType.SortOption.PERCENTAGE)) {
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                } else if (selectedSortOption
                        .equals(SortingType.SortOption.HOURS)) {
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                } else if (selectedSortOption
                        .equals(SortingType.SortOption.WORDS)) {
                    if (wordStatistic1.getTotal() == wordStatistic2.getTotal()) {
                        return 0;
                    }
                    return wordStatistic1.getTotal() > wordStatistic2
                            .getTotal() ? 1 : -1;
                }
            } else {
                return item1.getProject().getName()
                        .compareTo(item2.getProject().getName());
            }
            return 0;
        }
    }

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null
                && authenticatedAccount.getPerson().isMaintainerOfProjects();
    }

    public void sortLanguageList() {
        Collections.sort(activeLocales, languageComparator);
    }

    public void sortProjectList() {
        Collections.sort(projectIterations, versionComparator);
    }

    public List<HLocale> getActiveLocales() {
        if (activeLocales == null) {
            Set<HLocale> groupActiveLocales =
                    versionGroupServiceImpl.getGroupActiveLocales(getSlug());
            activeLocales = Lists.newArrayList(groupActiveLocales);
        }
        Collections.sort(activeLocales, languageComparator);
        return activeLocales;
    }

    @CachedMethodResult
    public String getStatisticFigureForLocale(
            SortingType.SortOption sortOption, LocaleId localeId) {
        WordStatistic statistic = getStatisticsForLocale(localeId);

        return getStatisticFigure(sortOption, statistic);
    }

    @CachedMethodResult
    public String getStatisticFigureForProjectWithLocale(
            SortingType.SortOption sortOption, LocaleId localeId,
            Long projectIterationId) {
        WordStatistic statistic =
                statisticMap.get(new VersionLocaleKey(projectIterationId,
                        localeId));

        return getStatisticFigure(sortOption, statistic);
    }

    @CachedMethodResult
    public String getStatisticFigureForProject(
            SortingType.SortOption sortOption, Long projectIterationId) {
        WordStatistic statistic = getStatisticForProject(projectIterationId);

        return getStatisticFigure(sortOption, statistic);
    }

    private String getStatisticFigure(SortingType.SortOption sortOption,
            WordStatistic statistic) {
        if (sortOption.equals(SortingType.SortOption.HOURS)) {
            return StatisticsUtil.formatHours(statistic.getRemainingHours());
        } else if (sortOption.equals(SortingType.SortOption.WORDS)) {
            return String.valueOf(statistic.getTotal());
        } else {
            return StatisticsUtil.formatPercentage(statistic
                    .getPercentTranslated()) + "%";
        }
    }

    public String getStatisticUnit(SortingType.SortOption sortOption) {
        if (sortOption.equals(SortingType.SortOption.HOURS)) {
            return zanataMessages.getMessage("jsf.stats.HoursRemaining");
        } else if (sortOption.equals(SortingType.SortOption.WORDS)) {
            return zanataMessages.getMessage("jsf.Words");
        } else {
            return zanataMessages.getMessage("jsf.Translated");
        }
    }

    @CachedMethodResult
    public WordStatistic getStatisticsForLocale(LocaleId localeId) {
        WordStatistic statistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            if (entry.getKey().getLocaleId().equals(localeId)) {
                statistic.add(entry.getValue());
            }
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    @CachedMethodResult
    public WordStatistic getStatisticForProject(Long projectIterationId) {
        WordStatistic statistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            if (entry.getKey().getProjectIterationId()
                    .equals(projectIterationId)) {
                statistic.add(entry.getValue());
            }
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    @CachedMethodResult
    public WordStatistic getSelectedLocaleStatistic(Long projectIterationId) {
        return statisticMap.get(new VersionLocaleKey(projectIterationId,
                selectedLocale.getLocaleId()));
    }

    @CachedMethodResult
    public WordStatistic getSelectedVersionStatistic(LocaleId localeId) {
        return statisticMap.get(new VersionLocaleKey(selectedVersion.getId(),
                localeId));
    }

    public List<HPerson> getMaintainers() {
        return versionGroupDAO.getMaintainersBySlug(getSlug());
    }

    private Map<LocaleId, List<HProjectIteration>> getMissingLocaleVersionMap() {
        if (missingLocaleVersionMap == null) {
            missingLocaleVersionMap =
                    versionGroupServiceImpl
                            .getMissingLocaleVersionMap(getSlug());
        }
        return missingLocaleVersionMap;
    }

    /**
     * Search for locale that is not activated in given version
     *
     * @param version
     */
    public List<LocaleId> getMissingLocale(HProjectIteration version) {
        List<LocaleId> result = Lists.newArrayList();
        for (Map.Entry<LocaleId, List<HProjectIteration>> entry : getMissingLocaleVersionMap()
                .entrySet()) {
            if (entry.getValue().contains(version)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public String getMissingLocaleTitle(HProjectIteration version) {
        int size = getMissingLocale(version).size();
        if (size > 1) {
            return zanataMessages.getMessage("jsf.LanguagesMissingProject",
                    size);
        }
        return zanataMessages.getMessage("jsf.LanguageMissingProject", size);
    }

    /**
     * Search for version that doesn't activate given locale
     *
     * @param localeId
     */
    public List<HProjectIteration> getMissingVersion(LocaleId localeId) {
        if (getMissingLocaleVersionMap().containsKey(localeId)) {
            return getMissingLocaleVersionMap().get(localeId);
        }
        return Lists.newArrayList();
    }

    public String getMissingVersionTitle(LocaleId localeId) {
        int size = getMissingVersion(localeId).size();
        if (size > 1) {
            return zanataMessages.getMessage("jsf.ProjectsMissingLanguage",
                    size);
        }
        return zanataMessages.getMessage("jsf.ProjectMissingLanguage", size);
    }

    public boolean isLocaleActivatedInVersion(HProjectIteration version,
            LocaleId localeId) {
        List<HProjectIteration> versionList =
                getMissingLocaleVersionMap().get(localeId);
        return !versionList.contains(version);
    }

    /**
     * Load up statistics for all project versions in all active locales in the
     * group.
     */
    private void loadStatistic() {
        statisticMap = Maps.newHashMap();

        for (HLocale locale : getActiveLocales()) {
            statisticMap.putAll(versionGroupServiceImpl.getLocaleStatistic(
                    getSlug(), locale.getLocaleId()));
        }
        WordStatistic overallWordStatistic = new WordStatistic();
        int totalWordCount = 0;
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            overallWordStatistic.add(entry.getValue());
            totalWordCount += entry.getValue().getTotal();
        }
        overallWordStatistic.setRemainingHours(StatisticsUtil
                .getRemainingHours(overallWordStatistic));

        int totalMessageCount =
                versionGroupServiceImpl.getTotalMessageCount(getSlug());

        overallStatistic =
                new OverallStatistic(totalWordCount, totalMessageCount,
                        overallWordStatistic);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public final class OverallStatistic {
        private int totalWordCount;
        private int totalMessageCount;
        private WordStatistic statistic;
    }

    public List<HProjectIteration> getProjectIterations() {
        if (projectIterations == null) {
            projectIterations = projectIterationDAO.getByGroupSlug(slug);
        }
        Collections.sort(projectIterations, versionComparator);
        return projectIterations;
    }

    // reset all page cached statistics
    public void resetPageData() {
        projectIterations = null;
        activeLocales = null;
        missingLocaleVersionMap = null;
        loadStatistic();
    }
}
