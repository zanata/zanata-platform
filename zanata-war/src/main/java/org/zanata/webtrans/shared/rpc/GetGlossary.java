package org.zanata.webtrans.shared.rpc;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.ProjectIterationId;

public class GetGlossary implements DispatchAction<GetGlossaryResult>,
        HasSearchType {
    private static final long serialVersionUID = 1L;
    private LocaleId localeId;
    private String query;
    private SearchType searchType;
    private LocaleId srcLocaleId;
    private ProjectIterationId projectIterationId;

    @SuppressWarnings("unused")
    private GetGlossary() {
    }

    public GetGlossary(String query, ProjectIterationId projectIterationId,
            LocaleId localeId, LocaleId srcLocaleId, SearchType searchType) {
        this.query = query;
        this.localeId = localeId;
        this.projectIterationId = projectIterationId;
        this.searchType = searchType;
        this.srcLocaleId = srcLocaleId;
    }

    @Override
    public SearchType getSearchType() {
        return searchType;
    }

    public void setLocaleId(LocaleId localeId) {
        this.localeId = localeId;
    }

    public LocaleId getLocaleId() {
        return localeId;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public LocaleId getSrcLocaleId() {
        return srcLocaleId;
    }

    public ProjectIterationId getProjectIterationId() {
        return projectIterationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GetGlossary)) return false;

        GetGlossary that = (GetGlossary) o;

        if (localeId != null ? !localeId.equals(that.localeId) :
            that.localeId != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null)
            return false;
        if (searchType != that.searchType) return false;
        if (srcLocaleId != null ? !srcLocaleId.equals(that.srcLocaleId) :
            that.srcLocaleId != null) return false;
        return projectIterationId != null ?
            projectIterationId.equals(that.projectIterationId) :
            that.projectIterationId == null;

    }

    @Override
    public int hashCode() {
        int result = localeId != null ? localeId.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (searchType != null ? searchType.hashCode() : 0);
        result =
            31 * result + (srcLocaleId != null ? srcLocaleId.hashCode() : 0);
        result = 31 * result +
            (projectIterationId != null ? projectIterationId.hashCode() : 0);
        return result;
    }
}
