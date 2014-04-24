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

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.FilterUtil;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.ZanataMessages;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHomeAction")
@Scope(ScopeType.PAGE)
public class VersionGroupHomeAction extends AbstractSortAction implements
        Serializable {
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

    @In
    private LocaleDAO localeDAO;

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
    private WordStatistic overallStatistic;

    private List<HLocale> activeLocales;

    private List<HProjectIteration> projectIterations;

    private Map<VersionLocaleKey, WordStatistic> statisticMap;

    private Map<LocaleId, List<HProjectIteration>> missingLocaleVersionMap;

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

    @Getter
    private final AbstractListFilter<HLocale> projectTabLanguageFilter =
            new AbstractListFilter<HLocale>() {
                @Override
                public int getFilteredListSize() {
                    if (getSelectedVersion() == null) {
                        return 0;
                    }
                    return getFilteredList().size();
                }

                @Override
                protected List<HLocale> getFilteredList() {
                    return FilterUtil.filterLanguageList(getQuery(),
                            getActiveLocales());
                }
            };

    @Getter
    private final AbstractListFilter<HLocale> languageTabLanguageFilter =
            new AbstractListFilter<HLocale>() {
                @Override
                protected List<HLocale> getFilteredList() {
                    return FilterUtil.filterLanguageList(getQuery(),
                            getActiveLocales());
                }
            };
    @Getter
    private final AbstractListFilter<HProjectIteration> languageTabVersionFilter =
            new AbstractListFilter<HProjectIteration>() {
                @Override
                public int getFilteredListSize() {
                    if (getSelectedLocale() == null) {
                        return 0;
                    }
                    return getFilteredList().size();
                }

                @Override
                protected List<HProjectIteration> getFilteredList() {
                    return FilterUtil.filterVersionListWithProjectName(
                            getQuery(), getProjectIterations());
                }
            };

    @Getter
    private final AbstractListFilter<HProjectIteration> projectTabVersionFilter =
            new AbstractListFilter<HProjectIteration>() {
                @Override
                protected List<HProjectIteration> getFilteredList() {
                    return FilterUtil.filterVersionListWithProjectName(
                            getQuery(), getProjectIterations());
                }
            };

    public void setPageRendered(boolean pageRendered) {
        if (pageRendered) {
            loadStatistics();
        }
        this.pageRendered = pageRendered;
    }

    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;

        @Setter
        private Long selectedVersionId;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale o1, HLocale o2) {
            if (!sortingType.isDescending()) {
                HLocale temp = o1;
                o1 = o2;
                o2 = temp;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;

                if (selectedVersionId == null) {
                    wordStatistic1 = getStatisticsForLocale(o1.getLocaleId());
                    wordStatistic2 = getStatisticsForLocale(o2.getLocaleId());
                } else {
                    wordStatistic1 =
                            statisticMap.get(new VersionLocaleKey(
                                    selectedVersionId, o1.getLocaleId()));
                    wordStatistic2 =
                            statisticMap.get(new VersionLocaleKey(
                                    selectedVersionId, o2.getLocaleId()));
                }
                switch (selectedSortOption) {
                case PERCENTAGE:
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                case HOURS:
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                case WORDS:
                    return Double.compare(wordStatistic1.getUntranslated(),
                            wordStatistic2.getUntranslated());
                }
            } else {
                return o1.retrieveDisplayName().compareTo(
                        o2.retrieveDisplayName());
            }
            return 0;
        }
    }

    private class VersionComparator implements Comparator<HProjectIteration> {
        private SortingType sortingType;

        @Setter
        private LocaleId selectedLocaleId;

        public VersionComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HProjectIteration o1, HProjectIteration o2) {
            if (!sortingType.isDescending()) {
                HProjectIteration temp = o1;
                o1 = o2;
                o2 = temp;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedLocaleId != null) {
                    wordStatistic1 =
                            statisticMap.get(new VersionLocaleKey(o1.getId(),
                                    selectedLocaleId));
                    wordStatistic2 =
                            statisticMap.get(new VersionLocaleKey(o2.getId(),
                                    selectedLocaleId));
                } else {
                    wordStatistic1 = getStatisticForProject(o1.getId());
                    wordStatistic2 = getStatisticForProject(o2.getId());
                }

                switch (selectedSortOption) {
                case PERCENTAGE:
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                case HOURS:
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                case WORDS:
                    return Double.compare(wordStatistic1.getUntranslated(),
                            wordStatistic2.getUntranslated());
                }
            } else {
                return o1.getProject().getName().toLowerCase()
                        .compareTo(o2.getProject().getName().toLowerCase());
            }
            return 0;
        }
    }

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null
                && authenticatedAccount.getPerson().isMaintainerOfProjects();
    }

    /**
     * Sort language list based on overall locale statistic for the group
     */
    public void sortLanguageList() {
        languageComparator.setSelectedVersionId(null);
        Collections.sort(activeLocales, languageComparator);
    }

    /**
     * Sort language list based on statistics of the version on selected locale
     */
    public void sortLanguageList(Long versionId) {
        languageComparator.setSelectedVersionId(versionId);
        Collections.sort(activeLocales, languageComparator);
    }

    /**
     * Sort project list based on selected locale - language tab
     * 
     * @pa localeId
     */
    public void sortProjectList(LocaleId localeId) {
        versionComparator.setSelectedLocaleId(localeId);
        Collections.sort(projectIterations, versionComparator);
    }

    /**
     * Sort project list based on version's overall statistics
     */
    public void sortProjectList() {
        versionComparator.setSelectedLocaleId(null);
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
    public DisplayUnit getStatisticFigureForProjectWithLocale(
            SortingType.SortOption sortOption, LocaleId localeId,
            Long projectIterationId) {
        WordStatistic statistic =
                statisticMap.get(new VersionLocaleKey(projectIterationId,
                        localeId));
        return getDisplayUnit(sortOption, statistic, null);
    }

    @CachedMethodResult
    public DisplayUnit getStatisticFigureForLocale(
            SortingType.SortOption sortOption, LocaleId localeId) {
        WordStatistic statistic = getStatisticsForLocale(localeId);
        return getDisplayUnit(sortOption, statistic, null);
    }

    @CachedMethodResult
    public DisplayUnit getStatisticFigureForProject(
            SortingType.SortOption sortOption, Long projectIterationId) {
        WordStatistic statistic = getStatisticForProject(projectIterationId);
        return getDisplayUnit(sortOption, statistic, null);
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
        List<HPerson> list = versionGroupDAO.getMaintainersBySlug(getSlug());

        Collections.sort(list, ComparatorUtil.PERSON_NAME_COMPARATOR);
        return list;
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
    @Override
    protected void loadStatistics() {
        statisticMap = Maps.newHashMap();

        for (HLocale locale : getActiveLocales()) {
            statisticMap.putAll(versionGroupServiceImpl.getLocaleStatistic(
                    getSlug(), locale.getLocaleId()));
        }
        overallStatistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            overallStatistic.add(entry.getValue());
        }
        overallStatistic.setRemainingHours(StatisticsUtil
                .getRemainingHours(overallStatistic));
    }

    public List<HProjectIteration> getProjectIterations() {
        if (projectIterations == null) {
            projectIterations =
                    versionGroupServiceImpl
                            .getNonObsoleteProjectIterationsBySlug(slug);
        }

        Collections.sort(projectIterations, versionComparator);
        return projectIterations;
    }

    @Override
    public void resetPageData() {
        projectIterations = null;
        activeLocales = null;
        selectedLocale = null;
        selectedVersion = null;
        missingLocaleVersionMap = null;
        projectTabLanguageFilter.resetQueryAndPage();
        projectTabVersionFilter.resetQueryAndPage();
        languageTabLanguageFilter.resetQueryAndPage();
        languageTabVersionFilter.resetQueryAndPage();
        loadStatistics();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return zanataMessages.getMessage(key, args);
    }

    public void setSelectedLocaleId(String localeId) {
        this.selectedLocale = localeDAO.findByLocaleId(new LocaleId(localeId));
    }

    public void setSelectedVersionSlug(String projectSlug, String versionSlug) {
        selectedVersion =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
    }
}
