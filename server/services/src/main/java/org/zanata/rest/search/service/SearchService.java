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
package org.zanata.rest.search.service;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.lucene.queryparser.classic.ParseException;
import org.zanata.common.EntityStatus;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.SearchResult;
import org.zanata.rest.search.dto.GroupSearchResult;
import org.zanata.rest.search.dto.PersonSearchResult;
import org.zanata.rest.search.dto.ProjectSearchResult;
import org.zanata.rest.search.dto.ProjectVersionSearchResult;
import org.zanata.rest.search.dto.SearchResults;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
@Path("/search")
@Produces(APPLICATION_JSON)
@Transactional(readOnly = true)
public class SearchService implements Serializable {

    private static final long serialVersionUID = 8924030399679653241L;
    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private GravatarService gravatarService;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private VersionGroupDAO versionGroupDAO;

    @Inject
    private ZanataIdentity identity;

    @Inject @Authenticated
    private HAccount authenticatedAccount;

    private static final int MAX_RESULT = 20;

    @GET
    @Path("/projects")
    public Response searchProjects(
            @QueryParam("q") @DefaultValue("") String query,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("sizePerPage") int sizePerPage,
            @DefaultValue("false") @QueryParam("includeVersion") boolean includeVersion) {

        int offset = (validatePage(page) - 1) * validatePageSize(sizePerPage);

        try {
            int totalCount;
            List<HProject> projects;
            if (StringUtils.isEmpty(query)) {
                totalCount = projectDAO.getFilterProjectSize(false, false, true);
                projects = projectDAO.getOffsetList(offset,
                        validatePageSize(sizePerPage), false, false, true);
            } else {
                totalCount = projectDAO.getQueryProjectSize(query, false);
                projects =
                        projectDAO.searchProjects(query,
                                validatePageSize(sizePerPage), offset,
                                false);
            }

            Map<String, List<HProjectIteration>> projectSlugToVersions =
                    Maps.newHashMap();
            if (includeVersion && !projects.isEmpty()) {
                List<HProjectIteration> versions = projectIterationDAO
                        .searchByProjectsExcludeObsolete(projects);
                versions.forEach(ver -> {
                    String projectSlug = ver.getProject().getSlug();
                    List<HProjectIteration> iterations = projectSlugToVersions
                            .getOrDefault(projectSlug,
                                    Lists.newLinkedList());
                    iterations.add(ver);
                    projectSlugToVersions.put(projectSlug, iterations);
                });
            }
            List<SearchResult> results = projects.stream().map(p -> {
                ProjectSearchResult result = new ProjectSearchResult();
                result.setId(p.getSlug());
                result.setStatus(p.getStatus());
                result.setTitle(p.getName());
                result.setDescription(p.getDescription());
                if (includeVersion) {
                    List<HProjectIteration> iterations =
                            projectSlugToVersions.get(p.getSlug());
                    result.setVersions(iterations == null ? null : iterations
                            .stream()
                            .map(iteration -> new ProjectVersionSearchResult(
                                    iteration.getSlug(), iteration.getStatus()))
                            .collect(Collectors.toList()));
                }
                // TODO: include contributor count when data is available
                return result;
            }).collect(Collectors.toList());
            SearchResults searchResults = new SearchResults(totalCount, results,
                SearchResult.SearchResultType.Project);
            return Response.ok(searchResults).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GET
    @Path("/groups")
    public Response searchGroups(
            @QueryParam("q") @DefaultValue("") String query,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("sizePerPage") int sizePerPage) {

        int offset = (validatePage(page) - 1) * validatePageSize(sizePerPage);
        int totalCount;
        List<HIterationGroup> groups;
        boolean includeObsolete =
            identity.hasPermission("HIterationGroup", "view-obsolete");

        final EntityStatus[] status =
                includeObsolete ? new EntityStatus[] { EntityStatus.ACTIVE,
                        EntityStatus.READONLY, EntityStatus.OBSOLETE }
                        : new EntityStatus[] { EntityStatus.ACTIVE,
                                EntityStatus.READONLY };

        if (StringUtils.isEmpty(query)) {
            totalCount = versionGroupDAO.getAllGroupsCount(status);
            groups = versionGroupDAO.getAllGroups(validatePageSize(sizePerPage),
                    offset, status);
        } else {
            totalCount = versionGroupDAO.searchGroupBySlugAndNameCount(query,
                    status);
            groups = versionGroupDAO
                    .searchGroupBySlugAndName(query,
                            validatePageSize(sizePerPage), offset, status);
        }
        List<SearchResult> results = groups.stream().map(g -> {
                GroupSearchResult result = new GroupSearchResult();
                result.setId(g.getSlug());
                result.setStatus(g.getStatus());
                result.setTitle(g.getName());
                result.setDescription(g.getDescription());
                return result;
            }).collect(Collectors.toList());

        SearchResults searchResults = new SearchResults(totalCount, results,
                SearchResult.SearchResultType.Group);
        return Response.ok(searchResults).build();
    }

    @GET
    @Path("/people")
    public SearchResults searchPeople(
        @QueryParam("q") @DefaultValue("") String query,
        @DefaultValue("1") @QueryParam("page") int page,
        @DefaultValue("20") @QueryParam("sizePerPage") int sizePerPage) {

        int offset = (validatePage(page) - 1) * validatePageSize(sizePerPage);

        int totalCount = personDAO.findAllEnabledContainingNameSize(query);
        List<SearchResult> results = personDAO.findAllEnabledContainingName(query,
                validatePageSize(sizePerPage), offset)
            .stream().map(p -> {
                PersonSearchResult result = new PersonSearchResult();
                result.setId(p.getAccount().getUsername());
                result.setDescription(p.getName());
                result.setAvatarUrl(
                    gravatarService.getUserImageUrl(50, p.getEmail()));
                return result;
            }).collect(Collectors.toList());
        return new SearchResults(totalCount, results, SearchResult.SearchResultType.Person);
    }

    @GET
    @Path("/teams/language")
    public SearchResults searchLanguageTeams(
            @QueryParam("q") @DefaultValue("") String query,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("sizePerPage") int sizePerPage) {

        int offset = (validatePage(page) - 1) * validatePageSize(sizePerPage);
        int totalCount = localeDAO.countByFind(query, false);
        List<SearchResult> results = localeDAO
                .find(offset, validatePageSize(sizePerPage), query, null, false)
                .stream()
                .map(l -> LocaleService.convertHLocaleToSearchResultDTO(l))
                .collect(Collectors.toList());
        return new SearchResults(totalCount, results,
                SearchResult.SearchResultType.LanguageTeam);
    }

    private int validatePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int validatePageSize(int sizePerPage) {
        return (sizePerPage > MAX_RESULT) ? MAX_RESULT
                : ((sizePerPage < 1) ? 1 : sizePerPage);
    }
}
