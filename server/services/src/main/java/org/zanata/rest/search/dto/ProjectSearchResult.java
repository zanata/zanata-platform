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

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.EntityStatus;
import org.zanata.rest.dto.SearchResult;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ProjectSearchResult extends SearchResult {
    private static final long serialVersionUID = -3409572991798139508L;
    private String title;
    private long contributorCount;
    private EntityStatus status;
    private List<ProjectVersionSearchResult> versions;

    public ProjectSearchResult() {
        this.setType(SearchResultType.Project);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public long getContributorCount() {
        return this.contributorCount;
    }

    public void setContributorCount(final long contributorCount) {
        this.contributorCount = contributorCount;
    }

    public EntityStatus getStatus() {
        return this.status;
    }

    public void setStatus(final EntityStatus status) {
        this.status = status;
    }

    public List<ProjectVersionSearchResult> getVersions() {
        return versions;
    }

    public void setVersions(List<ProjectVersionSearchResult> versions) {
        this.versions = versions;
    }
}
