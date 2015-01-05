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
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.common.EntityStatus;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.Activity;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.DateUtil;
import org.zanata.util.ServiceLocator;
import org.zanata.util.StatisticsUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;

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
    private LocaleMemberDAO localeMemberDAO;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private ZanataIdentity identity;

    @In
    private CopyVersionManager copyVersionManager;

    @In
    private Messages msgs;

    @Setter
    @Getter
    private String slug;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private ConversationScopeMessages conversationScopeMessages;

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
    private AbstractListFilter<HProjectIteration> versionFilter =
            new InMemoryListFilter<HProjectIteration>() {
                @Override
                protected List<HProjectIteration> fetchAll() {
                    return getProjectVersions();
                }

                @Override
                protected boolean include(HProjectIteration elem,
                        String filter) {
                    return StringUtils.containsIgnoreCase(
                            elem.getSlug(), filter);
                }
            };

    private List<HProjectIteration> projectVersions;

    private Map<String, WordStatistic> statisticMap = Maps.newHashMap();

    private final VersionComparator versionComparator = new VersionComparator(
            getVersionSortingList());

    @Getter(lazy = true)
    private final List<Activity> projectLastActivity =
            fetchProjectLastActivity();

    // for storing last activity date for the version
    private Map<Long, Date> versionLatestActivityDate = Maps.newHashMap();

    public boolean isVersionCopying(String projectSlug, String versionSlug) {
        return copyVersionManager
                .isCopyVersionRunning(projectSlug, versionSlug);
    }

    public String
            getCopiedDocumentCount(String projectSlug, String versionSlug) {
        CopyVersionTaskHandle handler =
                copyVersionManager.getCopyVersionProcessHandle(projectSlug,
                        versionSlug);

        if (handler == null) {
            return "0";
        } else {
            return String.valueOf(handler.getDocumentCopied());
        }
    }

    public void cancelCopyVersion(String projectSlug, String versionSlug) {
        copyVersionManager.cancelCopyVersion(projectSlug, versionSlug);
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.copyVersion.Cancelled", versionSlug));
    }

    public String getCopyVersionCompletePercent(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handler =
                copyVersionManager.getCopyVersionProcessHandle(projectSlug,
                        versionSlug);

        if (handler != null) {
            double completedPercent =
                    (double) handler.getCurrentProgress() / (double) handler
                            .getMaxProgress() * 100;
            if (Double.compare(completedPercent, 100) == 0) {
                conversationScopeMessages.setMessage(
                        FacesMessage.SEVERITY_INFO,
                        msgs.format("jsf.copyVersion.Completed", versionSlug));
            }
            return String.format("%1$,.2f", completedPercent);
        } else {
            return "0";
        }
    }

    public String getCopyVersionTotalDocuments(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handler =
                copyVersionManager.getCopyVersionProcessHandle(projectSlug,
                        versionSlug);

        if (handler == null) {
            return "0";
        } else {
            return String.valueOf(handler.getTotalDoc());
        }
    }

    private List<Activity> fetchProjectLastActivity() {
        if (StringUtils.isEmpty(slug) || !identity.isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }

        Collection<Long> versionIds =
                Collections2.transform(getProjectVersions(),
                        new Function<HProjectIteration, Long>() {
                            @Override
                            public Long
                                    apply(@Nullable HProjectIteration input) {
                                return input.getId();
                            }
                        });

        return activityServiceImpl.findLatestVersionActivitiesByUser(
                authenticatedAccount.getPerson().getId(),
                Lists.newArrayList(versionIds), 0, 1);
    }

    public DisplayUnit getStatisticFigureForVersion(
            SortingType.SortOption sortOption, HProjectIteration version) {
        WordStatistic statistic = getStatisticForVersion(version.getSlug());

        return getDisplayUnit(sortOption, statistic, version.getLastChanged());
    }

    /**
     * Sort version list
     */
    public void sortVersionList() {
        Collections.sort(projectVersions, versionComparator);
        versionFilter.reset();
    }

    private class VersionComparator implements Comparator<HProjectIteration> {
        private SortingType sortingType;

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
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)
                    && !selectedSortOption
                            .equals(SortingType.SortOption.LAST_ACTIVITY)) {

                WordStatistic wordStatistic1 =
                        getStatisticForVersion(o1.getSlug());
                WordStatistic wordStatistic2 =
                        getStatisticForVersion(o2.getSlug());

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
                return o1.getSlug()
                        .compareToIgnoreCase(o2.getSlug());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_ACTIVITY)) {

                Date date1 = getVersionLastActivityDate(o1.getId());
                Date date2 = getVersionLastActivityDate(o2.getId());
                return DateUtil.compareDate(date1, date2);
            }
            return 0;
        }
    }

    private Date getVersionLastActivityDate(Long versionId) {
        if (!versionLatestActivityDate.containsKey(versionId)) {
            List<Activity> activities =
                    activityServiceImpl.findLatestVersionActivities(versionId,
                            0, 1);
            if (!activities.isEmpty()) {
                versionLatestActivityDate.put(versionId, activities.get(0)
                        .getLastChanged());
            } else {
                versionLatestActivityDate.put(versionId, null);
            }
        }
        return versionLatestActivityDate.get(versionId);
    }

    public void clearVersionStats(String versionSlug) {
        statisticMap.remove(versionSlug);
    }

    public WordStatistic getStatisticForVersion(String versionSlug) {
        WordStatistic statistic;
        if (statisticMap.containsKey(versionSlug)) {
            statistic = statisticMap.get(versionSlug);
        } else {
            HProjectIteration version =
                    projectIterationDAO.getBySlug(slug, versionSlug);
            statistic = getAllLocaleStatisticForVersion(version);
            statisticMap.put(versionSlug, statistic);
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    @Override
    protected void loadStatistics() {
        statisticMap.clear();

        for (HProjectIteration version : getProjectVersions()) {
            statisticMap.put(version.getSlug(),
                    getAllLocaleStatisticForVersion(version));
        }
    }

    private WordStatistic getAllLocaleStatisticForVersion(
            HProjectIteration version) {
        WordStatistic versionStats = new WordStatistic();
        List<HLocale> locales = getSupportedLocale(version);
        for (HLocale locale : locales) {
            versionStats.add(versionStateCacheImpl.getVersionStatistics(
                    version.getId(), locale.getLocaleId()));
        }
        return versionStats;
    }

    public List<HLocale> getSupportedLocale(HProjectIteration version) {
        if (version != null) {
            return localeServiceImpl.getSupportedLanguageByProjectIteration(
                    slug, version.getSlug());
        }
        return Collections.EMPTY_LIST;
    }

    public List<HLocale> getUserJoinedLocales(HProjectIteration version) {
        if (authenticatedAccount == null) {
            return Collections.EMPTY_LIST;
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
    public List<HProjectIteration> getProjectVersions() {
        // Local DAO reference as this method is used from a dependent object
        // that may be out of bean scope.
        ProjectDAO projectDAO =
                ServiceLocator.instance().getInstance(ProjectDAO.class);
        if (projectVersions == null) {
            if (isUserAllowViewObsolete()) {
                projectVersions = projectDAO.getAllIterations(slug);
            } else {
                projectVersions = projectDAO.getActiveIterations(slug);
                projectVersions.addAll(projectDAO.getReadOnlyIterations(slug));
            }

            Collections.sort(projectVersions,
                    ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR);
        }
        return projectVersions;
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
        versionFilter.reset();
        loadStatistics();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return msgs.format(key, args);
    }
}
