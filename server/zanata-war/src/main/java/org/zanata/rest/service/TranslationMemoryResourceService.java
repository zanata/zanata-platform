/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import java.io.InputStream;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamingDAO;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.dao.TransMemoryStreamingDAO;
import org.zanata.exception.EntityMissingException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.lock.Lock;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.ITextFlow;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.tmx.TMXParser;
import org.zanata.util.CloseableIterator;
import com.google.common.base.Optional;
// TODO this should use transactions (probably too big for one though)
// TODO options to export obsolete docs and textflows to TMX?

@RequestScoped
@Named("translationMemoryResource")
@Path(TranslationMemoryResource.SERVICE_PATH)
@ParametersAreNonnullByDefault
public class TranslationMemoryResourceService
        implements TranslationMemoryResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationMemoryResourceService.class);

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private LockManagerService lockManagerServiceImpl;
    @Inject
    private RestSlugValidator restSlugValidator;
    @Inject
    private TextFlowStreamingDAO textFlowStreamDAO;
    @Inject
    private TransMemoryStreamingDAO transMemoryStreamingDAO;
    @Inject
    private TransMemoryDAO transMemoryDAO;
    @Inject
    private TMXParser tmxParser;
    @Inject
    private ZanataIdentity identity;

    @Override
    @CheckRole("admin")
    public Response getAllTranslationMemory(@Nullable LocaleId locale) {
        log.debug("exporting TMX for all projects, locale {}", locale);
        if (locale != null) {
            localeServiceImpl.validateSourceLocale(locale);
            // TODO findTextFlowsByLocale
        }
        String filename = makeTMXFilename(null, null, locale);
        CloseableIterator<HTextFlow> iter = textFlowStreamDAO.findTextFlows();
        return buildTMX("getAllTranslationMemory", iter, locale, filename);
    }

    @Override
    public Response getProjectTranslationMemory(@Nonnull String projectSlug,
            @Nullable LocaleId locale) {
        identity.checkPermission("", "download-tmx");
        log.debug("exporting TMX for project {}, locale {}", projectSlug,
                locale);
        HProject hProject =
                restSlugValidator.retrieveAndCheckProject(projectSlug, false);
        if (locale != null) {
            restSlugValidator.validateTargetLocale(locale, projectSlug);
            // TODO findTextFlowsByProjectAndLocale
        }
        String filename = makeTMXFilename(projectSlug, null, locale);
        CloseableIterator<HTextFlow> iter =
                textFlowStreamDAO.findTextFlowsByProject(hProject);
        return buildTMX("getProjectTranslationMemory-" + filename, iter, locale,
                filename);
    }

    @Override
    public Response getProjectIterationTranslationMemory(
            @Nonnull String projectSlug, @Nonnull String iterationSlug,
            @Nullable LocaleId locale) {
        identity.checkPermission("", "download-tmx");
        log.debug("exporting TMX for project {}, iteration {}, locale {}",
                projectSlug, iterationSlug, locale);
        HProjectIteration hProjectIteration = restSlugValidator
                .retrieveAndCheckIteration(projectSlug, iterationSlug, false);
        if (locale != null) {
            restSlugValidator.validateTargetLocale(locale, projectSlug,
                    iterationSlug);
            // TODO findTextFlowsByProjectIterationAndLocale
        }
        String filename = makeTMXFilename(projectSlug, iterationSlug, locale);
        CloseableIterator<HTextFlow> iter = textFlowStreamDAO
                .findTextFlowsByProjectIteration(hProjectIteration);
        return buildTMX("getProjectIterationTranslationMemory-" + filename,
                iter, locale, filename);
    }

    @Override
    @CheckRole("admin")
    public Response getTranslationMemory(@Nonnull String slug) {
        log.debug("exporting TMX for translation memory {}", slug);
        TransMemory tm = getTM(transMemoryDAO.getBySlug(slug), slug);
        String filename = makeTMXFilename(slug);
        CloseableIterator<TransMemoryUnit> iter =
                transMemoryStreamingDAO.findTransUnitsByTM(tm);
        return buildTMX("getTranslationMemory-" + filename, tm, iter, filename);
    }

    @Override
    @CheckRole("admin")
    public Response updateTranslationMemory(String slug, InputStream input)
            throws Exception {
        Lock tmLock = lockTM(slug);
        try {
            Optional<TransMemory> tm = transMemoryDAO.getBySlug(slug);
            tmxParser.parseAndSaveTMX(input, getTM(tm, slug));
            return Response.ok().build();
        } finally {
            lockManagerServiceImpl.release(tmLock);
        }
    }

    private TransMemory getTM(Optional<TransMemory> tm, String slug) {
        if (!tm.isPresent()) {
            throw new EntityMissingException(
                    "Translation memory \'" + slug + "\' was not found.");
        }
        return tm.get();
    }

    @CheckRole("admin")
    public Object deleteTranslationMemory(String slug)
            throws EntityMissingException {
        Lock tmLock = lockTM(slug);
        try {
            Optional<TransMemory> transMemory = transMemoryDAO.getBySlug(slug);
            if (transMemory.isPresent()) {
                transMemoryDAO.makeTransient(transMemory.get());
                return "Translation memory \'" + slug + "\' deleted";
            } else {
                throw new EntityMissingException(slug);
            }
        } finally {
            lockManagerServiceImpl.release(tmLock);
        }
    }

    @Override
    @CheckRole("admin")
    public Object deleteTranslationUnits(String slug) {
        return deleteTranslationUnitsUnguarded(slug);
    }

    /**
     * Deletes without checking security permissions (useful for background
     * processes which do the check ahead of time).
     *
     * @param slug
     * @return
     */
    public Object deleteTranslationUnitsUnguarded(String slug) {
        Lock tmLock = lockTM(slug);
        try {
            int numDeleted = transMemoryDAO.deleteTransMemoryContents(slug);
            return numDeleted + " translation units deleted";
        } finally {
            lockManagerServiceImpl.release(tmLock);
        }
    }

    /**
     * Deletes without checking security permissions and asynchronously.
     *
     * @param slug
     * @return
     */
    @Async
    public Future<Object> deleteTranslationUnitsUnguardedAsync(String slug,
            AsyncTaskHandle handle) {
        // TODO the handle is not being used for progress tracking in the
        // current implementation
        return AsyncTaskResult
                .taskResult(deleteTranslationUnitsUnguarded(slug));
    }

    private Lock lockTM(String slug) {
        Lock tmLock = new Lock("tm", slug);
        String owner = lockManagerServiceImpl.attainLockOrReturnOwner(tmLock);
        if (owner != null) {
            throw new ZanataServiceException("Translation Memory \'" + slug
                    + "\' is locked by user: " + owner, 503);
        }
        return tmLock;
    }

    private Response buildTMX(String jobName,
            @Nonnull CloseableIterator<? extends ITextFlow> iter,
            @Nullable LocaleId locale, @Nonnull String filename) {
        TMXStreamingOutput<HTextFlow> output = new TMXStreamingOutput(jobName,
                iter, new TranslationsTMXExportStrategy(locale));
        return okResponse(filename, output);
    }

    private Response buildTMX(String jobName, TransMemory tm,
            CloseableIterator<TransMemoryUnit> iter, String filename) {
        TMXStreamingOutput<TransMemoryUnit> output =
                new TMXStreamingOutput<TransMemoryUnit>(jobName, iter,
                        new TransMemoryTMXExportStrategy(tm));
        return okResponse(filename, output);
    }

    private Response okResponse(String filename, StreamingOutput output) {
        return Response.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + filename + "\"")
                .type(PREFERRED_MEDIA_TYPE).entity(output).build();
    }

    @Nonnull
    private static String makeTMXFilename(@Nullable String projectSlug,
            @Nullable String iterationSlug, @Nullable LocaleId locale) {
        String p = projectSlug != null ? projectSlug : "allProjects";
        String i = iterationSlug != null ? iterationSlug : "allVersions";
        String l = locale != null ? locale.getId() : "allLocales";
        return "zanata-" + p + "-" + i + "-" + l + ".tmx";
    }

    @Nonnull
    private static String makeTMXFilename(@Nullable String tmSlug) {
        return "zanata-" + tmSlug + ".tmx";
    }
}
