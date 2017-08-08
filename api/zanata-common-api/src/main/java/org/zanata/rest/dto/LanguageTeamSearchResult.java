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
package org.zanata.rest.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LanguageTeamSearchResult extends SearchResult {
    private static final long serialVersionUID = 3410996397191527671L;
    private LocaleDetails localeDetails;
    private long memberCount;
    private long requestCount;

    public LanguageTeamSearchResult() {
        this.setType(SearchResultType.LanguageTeam);
    }

    @JsonProperty("localeDetails")
    public LocaleDetails getLocaleDetails() {
        return this.localeDetails;
    }

    public void setLocaleDetails(final LocaleDetails localeDetails) {
        this.localeDetails = localeDetails;
    }

    @JsonProperty("memberCount")
    public long getMemberCount() {
        return this.memberCount;
    }

    public void setMemberCount(final long memberCount) {
        this.memberCount = memberCount;
    }

    @JsonProperty("requestCount")
    public long getRequestCount() {
        return this.requestCount;
    }

    public void setRequestCount(final long requestCount) {
        this.requestCount = requestCount;
    }
}
