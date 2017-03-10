/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.search.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.io.Serializable;
import java.util.List;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SearchResults implements Serializable {
    public int totalCount;
    public List<SearchResult> results;
    private SearchResult.SearchResultType type;

    @java.beans.ConstructorProperties({ "totalCount", "results", "type" })
    public SearchResults(final int totalCount, final List<SearchResult> results,
            final SearchResult.SearchResultType type) {
        this.totalCount = totalCount;
        this.results = results;
        this.type = type;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public List<SearchResult> getResults() {
        return this.results;
    }

    public SearchResult.SearchResultType getType() {
        return this.type;
    }
}
