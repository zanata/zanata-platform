/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.action;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.EntityStatus;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.FilterUtil;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.UrlUtil;
import org.zanata.util.ZanataMessages;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("projectHomeAction")
@Scope(ScopeType.PAGE)
public class ProjectHomeAction extends AbstractSortAction implements
        Serializable {

    @In
    private ActivityService activityServiceImpl;

    @In
    private LocaleService localeServiceImpl;

    @In
    private VersionStateCache versionStateCacheImpl;

    @In
    private UrlUtil urlUtil;

    @In
    private ProjectDAO projectDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private LocaleMemberDAO localeMemberDAO;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private ZanataIdentity identity;

    @In
    private ZanataMessages zanataMessages;

    @Setter
    @Getter
    private String slug;

    @Getter
    private SortingType VersionSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS,
                    SortingType.SortOption.LAST_ACTIVITY));

    @Getter
    private boolean pageRendered = false;

    @Getter
    private AbstractListFilter<VersionItem> versionFilter =
            new AbstractListFilter<VersionItem>() {
                @Override
                protected List<VersionItem> getFilteredList() {
                    return FilterUtil.filterVersionItemList(getQuery(),
                            getProjectVersions());
                }
            };

    private List<VersionItem> projectVersions;

    private Map<String, WordStatistic> statisticMap;

    private final VersionItemComparator versionItemComparator =
            new VersionItemComparator(getVersionSortingList());

    @CachedMethodResult
    public List<Activity> getProjectLastActivity() {
        if (StringUtils.isEmpty(slug) || !identity.isLoggedIn()) {
            return Lists.newArrayList();
        }

        Collection<Long> versionIds =
                Collections2.transform(getProjectVersions(),
                        new Function<VersionItem, Long>() {
                            @Override
                            public Long apply(@Nullable VersionItem input) {
                                return input.getVersion().getId();
                            }
                        });

        return activityServiceImpl.findLatestVersionActivitiesByUser(
                authenticatedAccount.getPerson().getId(),
                Lists.newArrayList(versionIds), 0, 1);
    }

    @CachedMethodResult
    public DisplayUnit getStatisticFigureForVersion(
            SortingType.SortOption sortOption, HProjectIteration version) {
        WordStatistic statistic = getStatisticForVersion(version.getSlug());

        return getDisplayUnit(sortOption, statistic, version.getLastChanged());
    }

    @CachedMethodResult
    public WordStatistic getStatisticForVersion(String versionSlug) {
        WordStatistic statistic = statisticMap.get(versionSlug);
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    /**
     * Sort version list
     */
    public void sortVersionList() {
        Collections.sort(projectVersions, versionItemComparator);
        versionFilter.resetQueryAndPage();
    }

    private class VersionItemComparator implements Comparator<VersionItem> {
        private SortingType sortingType;

        public VersionItemComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(VersionItem compareFrom, VersionItem compareTo) {
            final HProjectIteration item1, item2;

            if (sortingType.isDescending()) {
                item1 = compareFrom.getVersion();
                item2 = compareTo.getVersion();
            } else {
                item1 = compareTo.getVersion();
                item2 = compareFrom.getVersion();
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)
                    && !selectedSortOption
                            .equals(SortingType.SortOption.LAST_ACTIVITY)) {

                WordStatistic wordStatistic1 =
                        getStatisticForVersion(item1.getSlug());
                WordStatistic wordStatistic2 =
                        getStatisticForVersion(item2.getSlug());

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
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                return item1.getSlug().toLowerCase()
                        .compareTo(item2.getSlug().toLowerCase());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_ACTIVITY)) {

                List<Activity> activity1 =
                        activityServiceImpl.findLatestVersionActivities(
                                item1.getId(), 0, 1);
                List<Activity> activity2 =
                        activityServiceImpl.findLatestVersionActivities(
                                item2.getId(), 0, 1);
                if (activity1.isEmpty() && activity2.isEmpty()) {
                    return 0;
                } else if (activity1.isEmpty()) {
                    return -1;
                } else if (activity2.isEmpty()) {
                    return 1;
                }
                return activity1.get(0).getLastChanged()
                        .compareTo(activity2.get(0).getLastChanged());
            }
            return 0;
        }
    }

    @Override
    protected void loadStatistics() {
        statisticMap = Maps.newHashMap();

        for (VersionItem versionItem : getProjectVersions()) {
            WordStatistic versionStats = new WordStatistic();
            List<HLocale> locales =
                    getSupportedLocale(versionItem.getVersion());
            for (HLocale locale : locales) {
                versionStats
                        .add(versionStateCacheImpl.getVersionStatistics(
                                versionItem.getVersion().getId(),
                                locale.getLocaleId()));
            }
            statisticMap.put(versionItem.getVersion().getSlug(), versionStats);
        }
    }

    public List<HLocale> getSupportedLocale(HProjectIteration version) {
        if (version != null) {
            return localeServiceImpl.getSupportedLanguageByProjectIteration(
                    slug, version.getSlug());
        }
        return Lists.newArrayList();
    }

    public List<HLocale> getUserJoinedLocales(HProjectIteration version) {
        if (authenticatedAccount == null) {
            return Lists.newArrayList();
        }

        List<HLocale> userJoinedLocales = Lists.newArrayList();
        Long personId = authenticatedAccount.getPerson().getId();

        for (HLocale supportedLocale : getSupportedLocale(version)) {
            if (localeMemberDAO.isLocaleMember(personId,
                    supportedLocale.getLocaleId())
                    && isUserAllowedToTranslateOrReview(version,
                            supportedLocale)) {
                userJoinedLocales.add(supportedLocale);
            }
        }
        return userJoinedLocales;
    }

    // return list of versions order by creation date
    public List<VersionItem> getProjectVersions() {
        if (projectVersions == null) {
            List<HProjectIteration> result;
            if (isUserAllowViewObsolete()) {
                result = projectDAO.getAllIterations(slug);
            } else {
                result = projectDAO.getActiveIterations(slug);
                result.addAll(projectDAO.getReadOnlyIterations(slug));
                Collections.sort(result,
                        ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR);
            }

            projectVersions = Lists.newArrayList();
            for (int i = 0; i < result.size(); i++) {
                HProjectIteration version = result.get(i);
                projectVersions.add(new VersionItem(version, i == 0));
            }
        }
        return projectVersions;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class VersionItem {
        private HProjectIteration version;
        private boolean latestVersion;
    }

    public boolean isUserAllowViewObsolete() {
        return identity != null
                && identity.hasPermission("HProject", "view-obsolete");
    }

    public boolean isUserAllowedToTranslateOrReview(HProjectIteration version,
            HLocale localeId) {
        return version != null
                && localeId != null
                && isIterationActive(version)
                && identity != null
                && (identity.hasPermission("add-translation",
                        version.getProject(), localeId) || identity
                        .hasPermission("translation-review",
                                version.getProject(), localeId));
    }

    private boolean isIterationActive(HProjectIteration version) {
        return version.getProject().getStatus() == EntityStatus.ACTIVE
                || version.getStatus() == EntityStatus.ACTIVE;
    }

    public void setPageRendered(boolean pageRendered) {
        if (pageRendered) {
            loadStatistics();
        }
        this.pageRendered = pageRendered;
    }

    @Override
    public void resetPageData() {
        projectVersions = null;
        versionFilter.resetQueryAndPage();
        loadStatistics();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return zanataMessages.getMessage(key, args);
    }

    public String getCreateVersionUrl(String projectSlug) {
        return urlUtil.createNewVersionUrl(projectSlug);
    }
}
