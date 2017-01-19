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
import org.apache.commons.lang.StringUtils;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ProjectMemberDAO;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.StatisticsUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionGroupHomeAction")
@ViewScoped
@Model
@Transactional
public class VersionGroupHomeAction extends AbstractSortAction
        implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private ProjectMemberDAO projectMemberDAO;
    @Inject
    private VersionGroupService versionGroupServiceImpl;
    @Inject
    private Messages msgs;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private VersionGroupDAO versionGroupDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private LocaleDAO localeDAO;
    private String slug;
    private boolean pageRendered = false;
    private HLocale selectedLocale;
    private HProjectIteration selectedVersion;
    private WordStatistic overallStatistic;
    private List<HLocale> activeLocales;
    private List<HProjectIteration> projectIterations;
    private Map<VersionLocaleKey, WordStatistic> statisticMap =
            Maps.newHashMap();
    private Map<LocaleId, List<HProjectIteration>> missingLocaleVersionMap;
    private final Map<LocaleId, WordStatistic> localeStats = Maps.newHashMap();
    private final Map<Long, WordStatistic> projectStats = Maps.newHashMap();
    private SortingType projectSortingList = new SortingType(Lists.newArrayList(
            SortingType.SortOption.ALPHABETICAL, SortingType.SortOption.HOURS,
            SortingType.SortOption.PERCENTAGE, SortingType.SortOption.WORDS));
    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());
    private final VersionComparator versionComparator =
            new VersionComparator(getProjectSortingList());
    private final AbstractListFilter<HLocale> projectTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {

                @Override
                protected List<HLocale> fetchAll() {
                    return getActiveLocales();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(
                            elem.getLocaleId().getId(), filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.retrieveDisplayName(), filter);
                }
            };
    private final AbstractListFilter<HLocale> languageTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {

                @Override
                protected List<HLocale> fetchAll() {
                    return getActiveLocales();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(
                            elem.getLocaleId().getId(), filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.retrieveDisplayName(), filter);
                }
            };
    private final AbstractListFilter<HProjectIteration> languageTabVersionFilter =
            new InMemoryListFilter<HProjectIteration>() {

                @Override
                protected List<HProjectIteration> fetchAll() {
                    return getProjectIterations();
                }

                @Override
                protected boolean include(HProjectIteration elem,
                        String filter) {
                    HProject project = elem.getProject();
                    return StringUtils.containsIgnoreCase(project.getName(),
                            filter);
                }
            };
    private final AbstractListFilter<HProjectIteration> projectTabVersionFilter =
            new InMemoryListFilter<HProjectIteration>() {

                @Override
                protected List<HProjectIteration> fetchAll() {
                    return getProjectIterations();
                }

                @Override
                protected boolean include(HProjectIteration elem,
                        String filter) {
                    HProject project = elem.getProject();
                    return StringUtils.containsIgnoreCase(project.getName(),
                            filter);
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
        private Long selectedVersionId;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale o1, HLocale o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                HLocale temp = o1;
                o1 = o2;
                o2 = temp;
            }
            // Need to get statistic for comparison
            if (!selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedVersionId == null) {
                    wordStatistic1 = getStatisticsForLocale(o1.getLocaleId());
                    wordStatistic2 = getStatisticsForLocale(o2.getLocaleId());
                } else {
                    wordStatistic1 = statisticMap.get(new VersionLocaleKey(
                            selectedVersionId, o1.getLocaleId()));
                    wordStatistic2 = statisticMap.get(new VersionLocaleKey(
                            selectedVersionId, o2.getLocaleId()));
                }
                switch (selectedSortOption) {
                case PERCENTAGE:
                    return Double.compare(wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());

                case HOURS:
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());

                case WORDS:
                    return Double.compare(wordStatistic1.getUntranslated(),
                            wordStatistic2.getUntranslated());

                }
            } else {
                return o1.retrieveDisplayName()
                        .compareTo(o2.retrieveDisplayName());
            }
            return 0;
        }

        public void setSelectedVersionId(final Long selectedVersionId) {
            this.selectedVersionId = selectedVersionId;
        }
    }

    private class VersionComparator implements Comparator<HProjectIteration> {
        private SortingType sortingType;
        private LocaleId selectedLocaleId;

        public VersionComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HProjectIteration o1, HProjectIteration o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                HProjectIteration temp = o1;
                o1 = o2;
                o2 = temp;
            }
            // Need to get statistic for comparison
            if (!selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedLocaleId != null) {
                    wordStatistic1 = statisticMap.get(
                            new VersionLocaleKey(o1.getId(), selectedLocaleId));
                    wordStatistic2 = statisticMap.get(
                            new VersionLocaleKey(o2.getId(), selectedLocaleId));
                } else {
                    wordStatistic1 = getStatisticForProject(o1.getId());
                    wordStatistic2 = getStatisticForProject(o2.getId());
                }
                switch (selectedSortOption) {
                case PERCENTAGE:
                    return Double.compare(wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());

                case HOURS:
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());

                case WORDS:
                    return Double.compare(wordStatistic1.getUntranslated(),
                            wordStatistic2.getUntranslated());

                }
            } else {
                return ComparatorUtil.compareStringIgnoreCase(
                        o1.getProject().getName(), o2.getProject().getName());
            }
            return 0;
        }

        public void setSelectedLocaleId(final LocaleId selectedLocaleId) {
            this.selectedLocaleId = selectedLocaleId;
        }
    }

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null && projectMemberDAO
                .isMaintainerOfAnyProject(authenticatedAccount.getPerson());
    }

    /**
     * Sort language list based on overall locale statistic for the group
     */
    public void sortLanguageList() {
        languageComparator.setSelectedVersionId(null);
        Collections.sort(activeLocales, languageComparator);
        languageTabLanguageFilter.reset();
    }

    /**
     * Sort language list based on statistics of the version on selected locale
     */
    public void sortLanguageList(Long versionId) {
        languageComparator.setSelectedVersionId(versionId);
        Collections.sort(activeLocales, languageComparator);
        projectTabLanguageFilter.reset();
    }

    /**
     * Sort project list based on selected locale - language tab
     *
     * @pa localeId
     */
    public void sortProjectList(LocaleId localeId) {
        versionComparator.setSelectedLocaleId(localeId);
        Collections.sort(projectIterations, versionComparator);
        languageTabVersionFilter.reset();
    }

    /**
     * Sort project list based on version's overall statistics
     */
    public void sortProjectList() {
        versionComparator.setSelectedLocaleId(null);
        Collections.sort(projectIterations, versionComparator);
        projectTabVersionFilter.reset();
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

    public DisplayUnit getStatisticFigureForProjectWithLocale(
            SortingType.SortOption sortOption, LocaleId localeId,
            Long projectIterationId) {
        WordStatistic statistic = statisticMap.getOrDefault(
                new VersionLocaleKey(projectIterationId, localeId),
                new WordStatistic());
        return getDisplayUnit(sortOption, statistic, null);
    }

    public DisplayUnit getStatisticFigureForLocale(
            SortingType.SortOption sortOption, LocaleId localeId) {
        WordStatistic statistic = getStatisticsForLocale(localeId);
        return getDisplayUnit(sortOption, statistic, null);
    }

    public DisplayUnit getStatisticFigureForProject(
            SortingType.SortOption sortOption, Long projectIterationId) {
        WordStatistic statistic = getStatisticForProject(projectIterationId);
        return getDisplayUnit(sortOption, statistic, null);
    }

    public WordStatistic getStatisticsForLocale(LocaleId localeId) {
        if (!localeStats.containsKey(localeId)) {
            WordStatistic statistic = new WordStatistic();
            for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                    .entrySet()) {
                if (entry.getKey().getLocaleId().equals(localeId)) {
                    statistic.add(entry.getValue());
                }
            }
            statistic.setRemainingHours(
                    StatisticsUtil.getRemainingHours(statistic));
            localeStats.put(localeId, statistic);
        }
        return localeStats.get(localeId);
    }

    public WordStatistic getStatisticForProject(Long projectIterationId) {
        if (!projectStats.containsKey(projectIterationId)) {
            WordStatistic statistic = new WordStatistic();
            for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                    .entrySet()) {
                if (entry.getKey().getProjectIterationId()
                        .equals(projectIterationId)) {
                    statistic.add(entry.getValue());
                }
            }
            statistic.setRemainingHours(
                    StatisticsUtil.getRemainingHours(statistic));
            projectStats.put(projectIterationId, statistic);
        }
        return projectStats.get(projectIterationId);
    }

    public WordStatistic getSelectedLocaleStatistic(Long projectIterationId) {
        return statisticMap.get(new VersionLocaleKey(projectIterationId,
                selectedLocale.getLocaleId()));
    }

    public WordStatistic getSelectedVersionStatistic(LocaleId localeId) {
        return statisticMap
                .get(new VersionLocaleKey(selectedVersion.getId(), localeId));
    }

    public List<HPerson> getMaintainers() {
        List<HPerson> list = versionGroupDAO.getMaintainersBySlug(getSlug());
        Collections.sort(list, ComparatorUtil.PERSON_NAME_COMPARATOR);
        return list;
    }

    private Map<LocaleId, List<HProjectIteration>>
            getMissingLocaleVersionMap() {
        if (missingLocaleVersionMap == null) {
            missingLocaleVersionMap = versionGroupServiceImpl
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
            return msgs.format("jsf.LanguagesMissingProject", size);
        }
        return msgs.format("jsf.LanguageMissingProject", size);
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
        return Collections.EMPTY_LIST;
    }

    public String getMissingVersionTitle(LocaleId localeId) {
        int size = getMissingVersion(localeId).size();
        if (size > 1) {
            return msgs.format("jsf.ProjectsMissingLanguage", size);
        }
        return msgs.format("jsf.ProjectMissingLanguage", size);
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
            statisticMap.putAll(versionGroupServiceImpl
                    .getLocaleStatistic(getSlug(), locale.getLocaleId()));
        }
        overallStatistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            overallStatistic.add(entry.getValue());
        }
        overallStatistic.setRemainingHours(
                StatisticsUtil.getRemainingHours(overallStatistic));
    }

    public List<HProjectIteration> getProjectIterations() {
        if (projectIterations == null) {
            projectIterations = versionGroupServiceImpl
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
        projectTabLanguageFilter.reset();
        projectTabVersionFilter.reset();
        languageTabLanguageFilter.reset();
        languageTabVersionFilter.reset();
        localeStats.clear();
        projectStats.clear();
        loadStatistics();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return msgs.formatWithAnyArgs(key, args);
    }

    public void setSelectedLocaleId(String localeId) {
        this.selectedLocale = localeDAO.findByLocaleId(new LocaleId(localeId));
    }

    public void setSelectedVersionSlug(String projectSlug, String versionSlug) {
        selectedVersion =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
    }

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public boolean isPageRendered() {
        return this.pageRendered;
    }

    public HLocale getSelectedLocale() {
        return this.selectedLocale;
    }

    public HProjectIteration getSelectedVersion() {
        return this.selectedVersion;
    }

    public WordStatistic getOverallStatistic() {
        return this.overallStatistic;
    }

    public SortingType getProjectSortingList() {
        return this.projectSortingList;
    }

    public AbstractListFilter<HLocale> getProjectTabLanguageFilter() {
        return this.projectTabLanguageFilter;
    }

    public AbstractListFilter<HLocale> getLanguageTabLanguageFilter() {
        return this.languageTabLanguageFilter;
    }

    public AbstractListFilter<HProjectIteration> getLanguageTabVersionFilter() {
        return this.languageTabVersionFilter;
    }

    public AbstractListFilter<HProjectIteration> getProjectTabVersionFilter() {
        return this.projectTabVersionFilter;
    }
}
