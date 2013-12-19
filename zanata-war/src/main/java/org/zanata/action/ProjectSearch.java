package org.zanata.action;

import com.google.common.collect.Lists;
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
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;

import javax.faces.model.DataModel;
import java.io.Serializable;
import java.util.List;

@Name("projectSearch")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProjectSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int scrollerPage = 1;

    @Setter
    @Getter
    // project slug
    private String selectedItem;

    @Setter
    @Getter
    private String suggestQuery;

    @In
    private ProjectDAO projectDAO;

    @In
    private ZanataIdentity identity;

    private QueryProjectPagedListDataModel queryProjectPagedListDataModel =
            new QueryProjectPagedListDataModel();

    // Count of result to be return as part of autocomplete
    private final static int INITIAL_RESULT_COUNT = 5;

    /**
     * Return results on project search
     */
    public List<SearchResult> suggestProjects() {
        List<SearchResult> result = Lists.newArrayList();
        if (StringUtils.isEmpty(suggestQuery)) {
            return result;
        }
        try {
            List<HProject> searchResult =
                    projectDAO
                            .searchProjects(suggestQuery, INITIAL_RESULT_COUNT,
                                0,
                                identity.hasPermission("HProject",
                                    "view-obsolete"));

            for (HProject project : searchResult) {
                result.add(new SearchResult(project));
            }
            result.add(new SearchResult());
            return result;
        } catch (ParseException pe) {
            return result;
        }
    }

    public int getPageSize() {
        return queryProjectPagedListDataModel.getPageSize();
    }

    public DataModel getProjectPagedListDataModel() {
        queryProjectPagedListDataModel.setIncludeObsolete(identity
                .hasPermission("HProject", "view-obsolete"));
        return queryProjectPagedListDataModel;
    }

    public void setSearchQuery(String searchQuery) {
        queryProjectPagedListDataModel.setQuery(searchQuery);
    }

    public String getSearchQuery() {
        return queryProjectPagedListDataModel.getQuery();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public class SearchResult {
        @Getter
        private HProject project;

        public boolean isProjectNull() {
            return project == null;
        }
    }
}
