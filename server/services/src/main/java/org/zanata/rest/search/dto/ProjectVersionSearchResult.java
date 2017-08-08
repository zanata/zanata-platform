package org.zanata.rest.search.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.EntityStatus;
import org.zanata.rest.dto.SearchResult;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ProjectVersionSearchResult extends SearchResult {
    private static final long serialVersionUID = 1L;
    private EntityStatus status;

    public ProjectVersionSearchResult() {
        super.setType(SearchResultType.ProjectVersion);
    }

    public ProjectVersionSearchResult(String slug, EntityStatus status) {
        super();
        setId(slug);
        this.status = status;
    }

    public EntityStatus getStatus() {
        return status;
    }
}
