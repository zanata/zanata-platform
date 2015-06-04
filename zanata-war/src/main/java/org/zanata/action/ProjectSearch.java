package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.DataModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;

import com.google.common.collect.Lists;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.util.ServiceLocator;

/**
 * This will search both projects and people.
 */
@Name("projectSearch")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProjectSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static int DEFAULT_PAGE_SIZE = 30;

    @Getter
    private ProjectAutocomplete projectAutocomplete = new ProjectAutocomplete();

    private QueryProjectPagedListDataModel queryProjectPagedListDataModel =
            new QueryProjectPagedListDataModel(DEFAULT_PAGE_SIZE);

    // Count of project to be return as part of autocomplete
    private final static int INITIAL_RESULT_COUNT = 10;

    // Count of person to be return as part of autocomplete
    private final static int INITIAL_PERSON_RESULT_COUNT = 20;

    public DataModel getProjectPagedListDataModel() {
        return queryProjectPagedListDataModel;
    }

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

    private class ProjectAutocomplete extends
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
            if (StringUtils.isEmpty(getQuery())) {
                return result;
            }
            try {
                String searchQuery = getQuery().trim();
                boolean includeObsolete = false;
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
            queryProjectPagedListDataModel.setQuery(query);
            super.setQuery(query);
        }
    }
}
