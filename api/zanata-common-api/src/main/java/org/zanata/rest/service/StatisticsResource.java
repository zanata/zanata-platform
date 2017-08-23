/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.rest.dto.ProjectStatisticsMatrix;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Fetch different translation statistics
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path(StatisticsResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Statistics")
public interface StatisticsResource extends Serializable {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String SERVICE_PATH = "/stats";

    /**
     * Get translation statistics for a Project iteration and (optionally) it's
     * underlying documents.
     *
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param includeDetails
     *            Indicates whether to include detailed statistics for the
     *            project iteration's documents.
     * @param includeWordStats
     *            Indicates whether to include word-level statistics. Default is
     *            only message level stats.
     * @param locales
     *            Locale statistics to be fetched. If this is empty, all locale
     *            statistics will be returned. This parameter may be specified
     *            multiple times if multiple locales are to be fetched.
     */
    @GET
    @Path("/proj/{projectSlug}/iter/{iterationSlug}")
    @TypeHint(ContainerTranslationStatistics.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Contains translation statistics" +
                    " for the specified parameters"),
            @ResponseCode(code = 404, condition = "A project iteration could " +
                    "not be found for the given parameters"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
            ContainerTranslationStatistics
            getStatistics(
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("iterationSlug") String iterationSlug,
                    @QueryParam("detail") @DefaultValue("false") boolean includeDetails,
                    @QueryParam("word") @DefaultValue("false") boolean includeWordStats,
                    @QueryParam("locale") String[] locales);

    /**
     * Get translation statistics for a Document.
     *
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param docId
     *            Document identifier.
     * @param includeWordStats
     *            Indicates whether to include word-level statistics. Default is
     *            only message level stats.
     * @param locales
     *            Locale statistics to be fetched. If this is empty, all locale
     *            statistics will be returned. This parameter may be specified
     *            multiple times if multiple locales are to be fetched.
     * Deprecated. Use {@link #getStatisticsWithDocId}
     */
    @Deprecated
    @GET
    @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId:.*}")
    @TypeHint(ContainerTranslationStatistics.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Contains translation statistics" +
                    " for the specified parameters"),
            @ResponseCode(code = 404, condition = "A document could " +
                    "not be found for the given parameters"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
            ContainerTranslationStatistics
            getStatistics(
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("iterationSlug") String iterationSlug,
                    @PathParam("docId") String docId,
                    @QueryParam("word") @DefaultValue("false") boolean includeWordStats,
                    @QueryParam("locale") String[] locales);

    /**
     * Get translation statistics for a Document.
     *
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param docId
     *            Document identifier.
     * @param includeWordStats
     *            Indicates whether to include word-level statistics. Default is
     *            only message level stats.
     * @param locales
     *            Locale statistics to be fetched. If this is empty, all locale
     *            statistics will be returned. This parameter may be specified
     *            multiple times if multiple locales are to be fetched.
     */
    @GET
    @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc")
    @TypeHint(ContainerTranslationStatistics.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Contains translation statistics" +
                    " for the specified parameters"),
            @ResponseCode(code = 404, condition = "A document could " +
                    "not be found for the given parameters"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public ContainerTranslationStatistics getStatisticsWithDocId(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @QueryParam("docId") String docId,
            @QueryParam("word") @DefaultValue("false") boolean includeWordStats,
            @QueryParam("locale") String[] locales);

    /**
     * Get contribution statistic from project-version within given date
     * range.
     *
     * @param projectSlug
     *            project identifier
     * @param versionSlug
     *            version identifier
     * @param username
     *            username of contributor
     * @param dateRange
     *            date range from..to (yyyy-mm-dd..yyyy-mm-dd)
     * @param includeAutomatedEntry
     *            whether to include automatic entries of translation into statistic
     */
    @GET
    @Path("/project/{projectSlug}/version/{versionSlug}/contributor/{username}/{dateRange}")
    @TypeHint(ContributionStatistics.class)
    @Produces({ MediaType.APPLICATION_JSON })
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Contains contribution statistics" +
                    " for the specified parameters"),
            @ResponseCode(code = 404, condition = "A project version could " +
                    "not be found for the given parameters"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
            ContributionStatistics getContributionStatistics(
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("versionSlug") String versionSlug,
                    @PathParam("username") String username,
                    @PathParam("dateRange") String dateRange,
                    @QueryParam("includeAutomatedEntry")
                    @DefaultValue("false") boolean includeAutomatedEntry
            );

    /**
     * Return accumulated daily translation statistics including history for a
     * project version in given date range.
     *
     * @param projectSlug
     *          Project identifier
     * @param versionSlug
     *          Version identifier
     * @param dateRangeParam
     *          from..to (yyyy-mm-dd..yyyy-mm-dd), date range maximum: 365
     *            days
     * @param timeZoneID
     *          optional user time zone ID. Will use system default in absence
     *            or GMT zone if provided time zone ID can not be understood.
     */
    @Path("project/{projectSlug}/version/{versionSlug}/{dateRangeParam}")
    @GET
    @Produces({ "application/json" })
    @TypeHint(ProjectStatisticsMatrix[].class)
    Response getProjectStatisticsMatrix(
            @PathParam("projectSlug") final String projectSlug,
            @PathParam("versionSlug") final String versionSlug,
            @PathParam("dateRangeParam") String dateRangeParam,
            @QueryParam("timeZoneID") String timeZoneID);

}
