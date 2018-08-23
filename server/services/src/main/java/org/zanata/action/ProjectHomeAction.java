/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.Activity;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HProjectLocaleMember;
import org.zanata.model.HProjectMember;
import org.zanata.model.LocaleRole;
import org.zanata.model.ProjectRole;
import org.zanata.rest.service.GlossaryService;
import org.zanata.seam.security.CurrentUser;
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
import org.zanata.util.GlossaryUtil;
import org.zanata.util.StatisticsUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.model.ProjectRole.TranslationMaintainer;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("projectHomeAction")
@ViewScoped
@Model
@Transactional
public class ProjectHomeAction extends AbstractSortAction
        implements Serializable {
    public static final Ordering<LocaleRole> LOCALE_ROLE_ORDERING =
            Ordering.explicit(LocaleRole.Translator, LocaleRole.Reviewer,
                    LocaleRole.Coordinator, LocaleRole.Glossarist);
    private static final long serialVersionUID = -5163376385991003306L;
    @Inject
    private ActivityService activityServiceImpl;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    private LocaleMemberDAO localeMemberDAO;
    @Inject
    private CurrentUser currentUser;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private CopyVersionManager copyVersionManager;
    @Inject
    private Messages msgs;
    private String slug;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    @SuppressWarnings("deprecation")
    private org.zanata.seam.scope.ConversationScopeMessages conversationScopeMessages;
    @Inject
    private GlossaryDAO glossaryDAO;
    private SortingType VersionSortingList = new SortingType(Lists.newArrayList(
            SortingType.SortOption.ALPHABETICAL, SortingType.SortOption.HOURS,
            SortingType.SortOption.PERCENTAGE, SortingType.SortOption.WORDS,
            SortingType.SortOption.LAST_ACTIVITY));
    private boolean pageRendered = false;
    private AbstractListFilter<HProjectIteration> versionFilter =
            new InMemoryListFilter<HProjectIteration>() {

                private static final long serialVersionUID =
                        7931445158995457207L;

                @Override
                protected List<HProjectIteration> fetchAll() {
                    return getProjectVersions();
                }

                @Override
                protected boolean include(HProjectIteration elem,
                        String filter) {
                    return containsIgnoreCase(elem.getSlug(),
                            filter);
                }
            };
    private final SortingType PeopleSortingList =
            new SortingType(
                    Lists.newArrayList(SortingType.SortOption.NAME,
                            SortingType.SortOption.ROLE),
                    SortingType.SortOption.NAME);
    private final PeopleFilterComparator peopleFilterComparator =
            new PeopleFilterComparator(getPeopleSortingList());
    private ListMultimap<HPerson, ProjectRole> personRoles;
    private Map<HPerson, ListMultimap<HLocale, LocaleRole>> personLocaleRoles;
    private List<HProjectIteration> projectVersions;
    private Map<String, WordStatistic> statisticMap = new HashMap<>();
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private final VersionComparator versionComparator =
            new VersionComparator(getVersionSortingList());
    private final AtomicReference<Object> projectLastActivity =
            new AtomicReference<>();
    private HProject project;
    // for storing last activity date for the version
    private Map<Long, Date> versionLatestActivityDate = new HashMap<>();

    public boolean isVersionCopying(String projectSlug, String versionSlug) {
        return copyVersionManager.isCopyVersionRunning(projectSlug,
                versionSlug);
    }

    public String getCopiedDocumentCount(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handler = copyVersionManager
                .getCopyVersionProcessHandle(projectSlug, versionSlug);
        if (handler == null) {
            return "0";
        } else {
            return String.valueOf(handler.getDocumentCopied());
        }
    }

    public void cancelCopyVersion(String projectSlug, String versionSlug) {
        copyVersionManager.cancelCopyVersion(projectSlug, versionSlug);
        setMessage(msgs.format("jsf.copyVersion.Cancelled", versionSlug));
    }

    public String getCopyVersionCompletePercent(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handler = copyVersionManager
                .getCopyVersionProcessHandle(projectSlug, versionSlug);
        if (handler != null) {
            double completedPercent = (double) handler.getCurrentProgress()
                    / (double) handler.getMaxProgress() * 100;
            if (Double.compare(completedPercent, 100) == 0) {
                setMessage(msgs.format("jsf.copyVersion.Completed", versionSlug));
            }
            return String.format("%1$,.2f", completedPercent);
        } else {
            return "0";
        }
    }

    @SuppressWarnings("deprecation")
    private void setMessage(String message) {
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                message);
    }

    public String getCopyVersionTotalDocuments(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handler = copyVersionManager
                .getCopyVersionProcessHandle(projectSlug, versionSlug);
        if (handler == null) {
            return "0";
        } else {
            return String.valueOf(handler.getTotalDoc());
        }
    }

    private List<Activity> fetchProjectLastActivity() {
        if (StringUtils.isEmpty(slug) || !identity.isLoggedIn()) {
            return Collections.emptyList();
        }

        List<Long> versionIds = getProjectVersions().stream()
                .map(it -> it != null ? it.getId(): null)
                .collect(toList());
        return activityServiceImpl.findLatestVersionActivitiesByUser(
                currentUser.getPerson().getId(),
                versionIds, 0, 1);
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

    private final class VersionComparator
            implements Comparator<HProjectIteration> {
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
                    return Double.compare(wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                } else if (selectedSortOption
                        .equals(SortingType.SortOption.HOURS)) {
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                } else if (selectedSortOption
                        .equals(SortingType.SortOption.WORDS)) {
                    if (wordStatistic1.getTotal() == wordStatistic2
                            .getTotal()) {
                        return 0;
                    }
                    return wordStatistic1.getTotal() > wordStatistic2.getTotal()
                            ? 1 : -1;
                }
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                return o1.getSlug().compareToIgnoreCase(o2.getSlug());
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
            List<Activity> activities = activityServiceImpl
                    .findLatestVersionActivities(versionId, 0, 1);
            if (!activities.isEmpty()) {
                versionLatestActivityDate.put(versionId,
                        activities.get(0).getLastChanged());
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

    private WordStatistic
            getAllLocaleStatisticForVersion(HProjectIteration version) {
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
        return Collections.emptyList();
    }

    public List<HLocale> getUserJoinedLocales(HProjectIteration version) {
        if (!currentUser.isLoggedIn()) {
            return Collections.emptyList();
        }
        List<HLocale> userJoinedLocales = Lists.newArrayList();
        Long personId = currentUser.getPerson().getId();
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
        if (projectVersions == null) {
            projectVersions = projectDAO.getActiveIterations(slug);
            projectVersions.addAll(projectDAO.getReadOnlyIterations(slug));
            projectVersions
                    .sort(ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR);
        }
        return projectVersions;
    }

    public HProject getProject() {
        if (project == null) {
            project = projectDAO.getBySlug(slug);
        }
        return project;
    }

    public boolean isUserAllowedToTranslateOrReview(HProjectIteration version,
            HLocale localeId) {
        return version != null && localeId != null && isIterationActive(version)
                && identity != null
                && (identity.hasPermissionWithAnyTargets("add-translation",
                        version.getProject(), localeId)
                        || identity.hasPermissionWithAnyTargets(
                                "translation-review", version.getProject(),
                                localeId));
    }

    private boolean isIterationActive(HProjectIteration version) {
        return version.isActive();
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
        return msgs.formatWithAnyArgs(key, args);
    }

    public Map<HPerson, Collection<ProjectRole>> getMemberRoles() {
        return getPersonRoles().asMap();
    }

    private ListMultimap<HPerson, ProjectRole> getPersonRoles() {
        if (personRoles == null) {
            populatePersonRoles();
        }
        return personRoles;
    }

    private void populatePersonRoles() {
        personRoles = ArrayListMultimap.create();
        // This can be run from an ajax call that does not actually need to
        // render
        // personRoles. The null check prevents a NullPointerException in that
        // case, and the unused empty list is adequate
        if (getProject() != null) {
            for (HProjectMember membership : getProject().getMembers()) {
                personRoles.put(membership.getPerson(), membership.getRole());
            }
        }
    }

    private Map<HPerson, ListMultimap<HLocale, LocaleRole>>
            getPersonLocaleRoles() {
        if (personLocaleRoles == null) {
            populatePersonLocaleRoles();
        }
        return personLocaleRoles;
    }

    private void populatePersonLocaleRoles() {
        personLocaleRoles = new HashMap<>();
        // Project may be null if this is triggered from an ajax call that does
        // not actually need to render personLocaleRoles
        if (getProject() != null) {
            for (HProjectLocaleMember membership : getProject()
                    .getLocaleMembers()) {
                final HPerson person = membership.getPerson();
                if (!personLocaleRoles.containsKey(person)) {
                    final ListMultimap<HLocale, LocaleRole> localeRoles =
                            ArrayListMultimap.create();
                    personLocaleRoles.put(person, localeRoles);
                }
                personLocaleRoles.get(person).put(membership.getLocale(),
                        membership.getRole());
            }
        }
    }

    /**
     * Clear data related to memberships so that it will be re-loaded from the
     * database next time it is needed for display.
     *
     * This should be done whenever permissions are changed.
     */
    public void clearCachedMembershipData() {
        // Roles may have changed, so role lists are cleared so they will be
        // regenerated
        personRoles = null;
        personLocaleRoles = null;
        project = null;
        // Person may have no roles left and no longer belong in the list, so
        // ensure the list of people is refreshed.
        peopleFilterComparator.clearCachedData();
        peopleFilterComparator.sortPeopleList();
    }

    public List<HPerson> getAllMembers() {
        final Set<HPerson> people = new HashSet<>(getMemberRoles().keySet());
        people.addAll(getPersonLocaleRoles().keySet());
        return Lists.newArrayList(people);
    }

    public boolean isTranslator(HPerson person) {
        ListMultimap<HLocale, LocaleRole> map =
                getPersonLocaleRoles().get(person);
        return map != null && !map.isEmpty();
    }

    public boolean isMaintainer(HPerson person) {
        List<ProjectRole> roles = getPersonRoles().get(person);
        return (roles == null || roles.isEmpty()) ? false
                : roles.contains(ProjectRole.Maintainer)
                        || roles.contains(TranslationMaintainer);
    }

    /**
     * Check whether a person has any project or locale membership in this
     * project.
     */
    public boolean isMember(HPerson person) {
        return getAllMembers().contains(person);
    }

    /**
     * Get display names for all of a person's project and locale roles
     */
    public List<String> allRoleDisplayNames(HPerson person) {
        List<String> displayNames = Lists.newArrayList();
        displayNames.addAll(projectRoleDisplayNames(person));
        displayNames.addAll(languageRoleDisplayNames(person));
        return displayNames;
    }

    /**
     * Get a list of the display name for every project-related role for a
     * person.
     */
    public Collection<String> projectRoleDisplayNames(HPerson person) {
        final Collection<ProjectRole> rolesForPerson =
                getMemberRoles().get(person);
        Collection<ProjectRole> roles;
        if (rolesForPerson == null) {
            roles = new ArrayList<>();
        } else {
            roles = new ArrayList<>(rolesForPerson);
        }
        // Maintainer role includes TranslationMaintainer privileges, so do not
        // show the lower-permission role.
        if (roles.contains(Maintainer)) {
            roles.remove(TranslationMaintainer);
        }
        return roles.stream().map(this::projectRoleDisplayName).collect(toList());
    }

    /**
     * Get a list of the display name for every language-related role for a
     * person.
     */
    private List<String> languageRoleDisplayNames(HPerson person) {
        final ListMultimap<HLocale, LocaleRole> localeRolesMultimap =
                getPersonLocaleRoles().get(person);
        if (localeRolesMultimap == null) {
            return Collections.emptyList();
        }

        return localeRolesMultimap.asMap().entrySet().stream()
                .map(TO_LOCALE_ROLES_DISPLAY_STRING)
                .sorted(Ordering.natural().onResultOf(
                        s -> isNullOrEmpty(s) ? "" : s.toLowerCase()))
                .collect(toList());
    }

    /**
     * Display string for just the roles for a person within a locale.
     */
    public Collection<String> rolesDisplayForLocale(HPerson person,
            HLocale locale) {
        final ListMultimap<HLocale, LocaleRole> localesWithRoles =
                getPersonLocaleRoles().get(person);
        if (localesWithRoles == null) {
            return new ArrayList<>();
        }
        Collection<LocaleRole> roles = localesWithRoles.asMap().get(locale);
        if (roles == null) {
            return new ArrayList<>();
        }
        final List<LocaleRole> sortedRoles =
                LOCALE_ROLE_ORDERING.sortedCopy(roles);

        final Stream<String> roleNames = sortedRoles.stream()
                .map(this::localeRoleDisplayName);
        return Lists.newArrayList(Joiner.on(", ").join(roleNames.iterator()));
    }

    @SuppressFBWarnings("SE_BAD_FIELD")
    private final Function<Map.Entry<HLocale, Collection<LocaleRole>>, String>
            TO_LOCALE_ROLES_DISPLAY_STRING =
            new Function<Map.Entry<HLocale, Collection<LocaleRole>>, String>() {

                @Nullable
                @Override
                public String apply(
                        @Nullable Map.Entry<HLocale, Collection<LocaleRole>> entry) {
                    if (entry != null) {
                        final String localeName =
                                entry.getKey().retrieveDisplayName();
                        final List<LocaleRole> sortedRoles =
                                LOCALE_ROLE_ORDERING
                                        .sortedCopy(entry.getValue());

                        Stream<String> roleNames = sortedRoles.stream()
                                .map(it -> localeRoleDisplayName(it));
                        return localeName + " " +
                                Joiner.on(", ").join(roleNames.iterator());
                    }
                    return null;
                }
            };

    private String projectRoleDisplayName(ProjectRole role) {
        switch (role) {
        case Maintainer:
            return msgs.get("jsf.Maintainer");

        case TranslationMaintainer:
            return msgs.get("jsf.TranslationMaintainer");

        default:
            return "";

        }
    }

    public String localeRoleDisplayName(LocaleRole role) {
        switch (role) {
        case Translator:
            return msgs.get("jsf.Translator");

        case Reviewer:
            return msgs.get("jsf.Reviewer");

        case Coordinator:
            return msgs.get("jsf.Coordinator");

        case Glossarist:
            return msgs.get("jsf.Glossarist");

        default:
            return "";

        }
    }

    public int getGlossarySize() {
        String qualifiedName = GlossaryUtil.generateQualifiedName(
                GlossaryService.PROJECT_QUALIFIER_PREFIX, getSlug());
        return glossaryDAO.getEntriesCount(LocaleId.EN_US, null, qualifiedName);
    }

    /**
     * Transform to extract the name of the locale from a HLocale (for sorting)
     *
     * Use with {@link java.util.Collections#sort}
     */
    @SuppressFBWarnings("SE_BAD_FIELD")
    public static final Function<HLocale, String> TO_LOCALE_NAME =
            new Function<HLocale, String>() {

                @Nullable
                @Override
                public String apply(HLocale input) {
                    // To lowercase to prevent non-caps values appearing after
                    // all caps values (e.g. a appearing after Z)
                    return input != null ?
                            input.retrieveDisplayName().toLowerCase() : null;
                }
            };
    private static final Ordering<HLocale> LOCALE_NAME_ORDERING =
            Ordering.natural().onResultOf(TO_LOCALE_NAME::apply);

    public final class PeopleFilterComparator
            extends InMemoryListFilter<HPerson> implements Comparator<HPerson> {
        private static final long serialVersionUID = 3905373873256076410L;
        private final ProjectRolePredicate projectRolePredicate =
                new ProjectRolePredicate();
        private final ProjectLocalePredicate projectLocalePredicate =
                new ProjectLocalePredicate();
        private SortingType sortingType;
        private boolean showMembersInGroup;
        private List<HPerson> allMembers;
        private Map<HLocale, List<HPerson>> localePersonMap;

        public PeopleFilterComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HPerson o1, HPerson o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                HPerson temp = o1;
                o1 = o2;
                o2 = temp;
            }
            // this is set here as a workaround to prevent a separate API call
            // for the sort setting (according to aeng).
            setShowMembersInGroup(
                    selectedSortOption.equals(SortingType.SortOption.ROLE));
            return o1.getAccount().getUsername().toLowerCase()
                    .compareTo(o2.getAccount().getUsername().toLowerCase());
        }

        @Override
        protected List<HPerson> fetchAll() {
            if (allMembers == null) {
                allMembers = getAllMembers();
                // allMembers must be sorted or the initial display will be in
                // an undefined ordering. This is a weakness of the parent
                // classes,
                // which do not ensure correct ordering for the initial display.
                allMembers.sort(peopleFilterComparator);
            }
            return allMembers;
        }

        public void clearCachedData() {
            allMembers = null;
            localePersonMap = null;
        }

        @Override
        protected boolean include(HPerson person, final String filter) {
            if (StringUtils.isBlank(filter)) {
                return true;
            }
            projectRolePredicate.setFilter(filter);
            projectLocalePredicate.setFilter(filter);
            return hasMatchingName(person, filter) || hasMatchingRole(person)
                    || hasMatchingLanguage(person);
        }

        public void sortPeopleList() {
            this.reset();
            fetchAll().sort(peopleFilterComparator);
        }

        public Collection<HPerson> getMaintainers() {
            return fetchAll().stream()
                    .filter(p -> include(p, getFilter()) && isMaintainer(p))
                    .collect(toList());
        }

        public List<HLocale> getLocalesWithMembers() {
            final ArrayList<HLocale> locales =
                    new ArrayList<>(getMembersByLocale().keySet());
            locales.sort(LOCALE_NAME_ORDERING);
            return locales;
        }

        public Map<HLocale, List<HPerson>> getMembersByLocale() {
            if (localePersonMap == null) {
                localePersonMap = generateMembersByLocale();
            }
            return localePersonMap;
        }

        private Map<HLocale, List<HPerson>> generateMembersByLocale() {
            Map<HLocale, List<HPerson>> localePersonMap = new HashMap<>();
            for (HPerson person : fetchAll()) {
                if (!include(person, getFilter()) || !isTranslator(person)) {
                    continue;
                }
                ListMultimap<HLocale, LocaleRole> localeRolesForPerson =
                        getPersonLocaleRoles().get(person);
                for (HLocale locale : localeRolesForPerson.keySet()) {
                    List<HPerson> peopleForLocale = localePersonMap
                            .computeIfAbsent(locale, k -> new ArrayList<>());
                    if (!peopleForLocale.contains(person)) {
                        peopleForLocale.add(person);
                    }
                }
            }
            // ensure each list of people is in order
            for (List<HPerson> people : localePersonMap.values()) {
                people.sort(this);
            }
            return localePersonMap;
        }

        private boolean hasMatchingName(HPerson person, String filter) {
            return containsIgnoreCase(person.getName(), filter)
                    || containsIgnoreCase(
                            person.getAccount().getUsername(), filter);
        }

        private boolean hasMatchingRole(HPerson person) {
            return getPersonRoles().get(person).stream().anyMatch(projectRolePredicate);
        }

        private boolean hasMatchingLanguage(HPerson person) {
            ListMultimap<HLocale, LocaleRole> languageRoles =
                    getPersonLocaleRoles().get(person);
            return languageRoles != null &&
                    languageRoles.keySet().stream().anyMatch(projectLocalePredicate);
        }

        public boolean isShowMembersInGroup() {
            return this.showMembersInGroup;
        }

        public void setShowMembersInGroup(final boolean showMembersInGroup) {
            this.showMembersInGroup = showMembersInGroup;
        }
    }

    private static final class ProjectRolePredicate implements Predicate<ProjectRole> {

        private String filter;

        @Override
        public boolean test(ProjectRole projectRole) {
            return projectRole != null &&
                    containsIgnoreCase(projectRole.name(), filter);
        }

        public void setFilter(final String filter) {
            this.filter = filter;
        }
    }

    private static final class ProjectLocalePredicate implements Predicate<HLocale> {

        private String filter;

        @Override
        public boolean test(HLocale locale) {
            return locale != null &&
                    (containsIgnoreCase(locale.getDisplayName(), filter) ||
                            containsIgnoreCase(locale.getLocaleId().toString(),
                                    filter));
        }

        public void setFilter(final String filter) {
            this.filter = filter;
        }
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return this.slug;
    }

    public SortingType getVersionSortingList() {
        return this.VersionSortingList;
    }

    public boolean isPageRendered() {
        return this.pageRendered;
    }

    public AbstractListFilter<HProjectIteration> getVersionFilter() {
        return this.versionFilter;
    }

    public SortingType getPeopleSortingList() {
        return this.PeopleSortingList;
    }

    public PeopleFilterComparator getPeopleFilterComparator() {
        return this.peopleFilterComparator;
    }

    // This ugly code was generated by @lombok.Getter(lazy = true)
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("JLM_JSR166_UTILCONCURRENT_MONITORENTER")
    public List<Activity> getProjectLastActivity() {
        Object value = this.projectLastActivity.get();
        if (value == null) {
            synchronized (this.projectLastActivity) {
                value = this.projectLastActivity.get();
                if (value == null) {
                    final List<Activity> actualValue =
                            fetchProjectLastActivity();
                    value = actualValue == null ? this.projectLastActivity
                            : actualValue;
                    this.projectLastActivity.set(value);
                }
            }
        }
        return (List<Activity>) (value == this.projectLastActivity ? null
                : value);
    }
}
