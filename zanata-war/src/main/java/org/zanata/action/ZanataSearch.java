package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;

import com.google.common.collect.Lists;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.DateUtil;
import org.zanata.util.ServiceLocator;

/**
 * This will search both projects and people.
 */
@Name("zanataSearch")
@Scope(ScopeType.PAGE)
@AutoCreate
public class ZanataSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean includeObsolete = false;

    @In
    private ProjectDAO projectDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private AccountDAO accountDAO;

    @Getter
    private ProjectUserAutocomplete autocomplete = new ProjectUserAutocomplete();

    @Getter
    private SortingType ProjectSortingList = new SortingType(
        Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
            SortingType.SortOption.CREATED_DATE));

    @Getter
    private SortingType UserSortingList = new SortingType(
        Lists.newArrayList(SortingType.SortOption.ALPHABETICAL));

    // Count of project to be return as part of autocomplete
    private final static int INITIAL_RESULT_COUNT = 10;

    // Count of person to be return as part of autocomplete
    private final static int INITIAL_PERSON_RESULT_COUNT = 20;

    private final ProjectComparator projectComparator =
        new ProjectComparator(getProjectSortingList());

    private final UserComparator userComparator =
        new UserComparator(getUserSortingList());

    private List<HProject> projects;

    private List<HAccount> accounts;

    @AllArgsConstructor
    @NoArgsConstructor
    public class SearchResult {
        @Getter
        private HProject project;

        @Getter
        private HAccount account;

        public boolean isProjectNull() {
            return project == null;
        }
        public boolean isUserNull() {
            return account == null;
        }
    }

    private class ProjectUserAutocomplete extends
            AbstractAutocomplete<SearchResult> {

        private ProjectDAO projectDAO =
                ServiceLocator.instance().getInstance(ProjectDAO.class);

        private AccountDAO accountDAO = ServiceLocator.instance().getInstance(
                AccountDAO.class);

        /**
         * Return results on search
         */
        @Override
        public List<SearchResult> suggest() {
            List<SearchResult> result = Lists.newArrayList();
            if (StringUtils.isBlank(getQuery())) {
                return result;
            }
            try {
                String searchQuery = getQuery().trim();
                List<HProject> searchResult =
                        projectDAO.searchProjects(searchQuery,
                                INITIAL_RESULT_COUNT, 0, includeObsolete);

                for (HProject project : searchResult) {
                    result.add(new SearchResult(project, null));
                }
                List<HAccount> hAccounts =
                        accountDAO.searchQuery(searchQuery,
                                INITIAL_PERSON_RESULT_COUNT, 0);
                for (HAccount hAccount : hAccounts) {
                    result.add(new SearchResult(null, hAccount));
                }
                result.add(new SearchResult());
                return result;
            } catch (ParseException pe) {
                return result;
            }
        }

        /**
         * Action when an item is selected
         */
        @Override
        public void onSelectItemAction() {
            // nothing here
        }

        @Override
        public void setQuery(String query) {
            super.setQuery(query);
        }
    }

    @Getter
    private final AbstractListFilter<HProject> projectTabProjectFilter =
        new InMemoryListFilter<HProject>() {
            /**
             * Fetches all records.
             *
             * @return A list of all records to be managed by the filter.
             */
            @Override
            protected List<HProject> fetchAll() {
                if (StringUtils.isBlank(getAutocomplete().getQuery())) {
                    return Collections.emptyList();
                }
                return getProjects();
            }

            /**
             * Indicates whether the element should be included in the results.
             *
             * @param elem   The element to analyze
             * @param filter The filter string being used.
             * @return True if the element passes the filter. False otherwise.
             */
            @Override
            protected boolean include(HProject elem, String filter) {
                return true; //no internal filter
            }
        };

    @Getter
    private final AbstractListFilter<HAccount> userTabUserFilter =
        new InMemoryListFilter<HAccount>() {
            /**
             * Fetches all records.
             *
             * @return A list of all records to be managed by the filter.
             */
            @Override
            protected List<HAccount> fetchAll() {
                if (StringUtils.isEmpty(getAutocomplete().getQuery())) {
                    return Collections.emptyList();
                }
                return getAccounts();
            }

            /**
             * Indicates whether the element should be included in the results.
             *
             * @param elem   The element to analyze
             * @param filter The filter string being used.
             * @return True if the element passes the filter. False otherwise.
             */
            @Override
            protected boolean include(HAccount elem, String filter) {
                return true; //no internal filter
            }
        };

    public List<HProject> getProjects()  {
        if (projects == null) {
            ProjectDAO projectDAO =
                ServiceLocator.instance().getInstance(ProjectDAO.class);
            try {
                projects = projectDAO.searchProjects(getAutocomplete()
                    .getQuery(), -1, 0, includeObsolete);
                Collections.sort(projects,
                        ComparatorUtil.PROJECT_NAME_COMPARATOR);
            } catch (ParseException e) {
                return Collections.emptyList();
            }
        }
        return projects;
    }

    public List<HAccount> getAccounts() {
        if (accounts == null) {
            AccountDAO accountDAO =
                ServiceLocator.instance().getInstance(AccountDAO.class);

            accounts = accountDAO.searchQuery(getAutocomplete().getQuery(),
                    -1, 0);
            Collections.sort(accounts,
                ComparatorUtil.ACCOUNT_NAME_COMPARATOR);
        }
        return accounts;
    }

    public int getVersionSize(String projectSlug) {
        return projectIterationDAO.getByProjectSlug(projectSlug,
                EntityStatus.ACTIVE, EntityStatus.READONLY).size();
    }

    public int getTotalProjectCount() {
        if(StringUtils.isEmpty(getAutocomplete().getQuery())) {
            return 0;
        }
        try {
            return projectDAO.getQueryProjectSize(getAutocomplete().getQuery(),
                    includeObsolete);
        } catch (ParseException pe) {
            return 0;
        }
    }

    public int getTotalUserCount() {
        if(StringUtils.isEmpty(getAutocomplete().getQuery())) {
            return 0;
        }
        return accountDAO.searchQuery(getAutocomplete().getQuery(), -1, 0)
                .size();
    }

    public String getHowLongAgoDescription(Date date) {
        return DateUtil.getHowLongAgoDescription(date);
    }

    public String formatDate(Date date) {
        return DateUtil.formatShortDate(date);
    }

    /**
     * Sort project list
     */
    public void sortProjectList() {
        Collections.sort(projects, projectComparator);
        projectTabProjectFilter.reset();
    }

    public void sortUserList() {
        Collections.sort(accounts, userComparator);
        userTabUserFilter.reset();
    }

    private class ProjectComparator implements Comparator<HProject> {
        private SortingType sortingType;

        public ProjectComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HProject o1, HProject o2) {
            SortingType.SortOption selectedSortOption =
                sortingType.getSelectedSortOption();

            if (!selectedSortOption.isAscending()) {
                HProject temp = o1;
                o1 = o2;
                o2 = temp;
            }

            if (selectedSortOption.equals(SortingType.SortOption.CREATED_DATE)) {
                return ComparatorUtil.compareDate(o1.getCreationDate(),
                        o2.getCreationDate());
            } else {
                return ComparatorUtil.compareStringIgnoreCase(o1.getName(),
                        o2.getName());
            }
        }
    }

    private class UserComparator implements Comparator<HAccount> {
        private SortingType sortingType;

        public UserComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HAccount o1, HAccount o2) {
            SortingType.SortOption selectedSortOption =
                sortingType.getSelectedSortOption();

            if (!selectedSortOption.isAscending()) {
                HAccount temp = o1;
                o1 = o2;
                o2 = temp;
            }
            return ComparatorUtil.compareStringIgnoreCase(
                    o1.getPerson().getName(), o2.getPerson().getName());
        }
    }
}
