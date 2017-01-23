/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import com.google.common.base.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.ConfigurationService;
import org.zanata.service.FileSystemService;
import org.zanata.service.TranslationArchiveService;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.zanata.common.ProjectType.*;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("translationArchiveServiceImpl")
@RequestScoped
@Transactional
public class TranslationArchiveServiceImpl
        implements TranslationArchiveService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationArchiveServiceImpl.class);

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private FileSystemService fileSystemServiceImpl;
    @Inject
    private ConfigurationService configurationServiceImpl;

    @Override
    public String buildTranslationFileArchive(String projectSlug,
            String iterationSlug, String localeId, String userName,
            AsyncTaskHandle<String> handle) throws Exception {
        Optional<AsyncTaskHandle<String>> handleOpt =
                Optional.fromNullable(handle);
        if (handleOpt.isPresent()) {
            prepareHandle(handleOpt.get(), projectSlug, iterationSlug);
        }
        boolean isPoProject = isPoProject(projectSlug, iterationSlug);
        final String projectDirectory = projectSlug + "-" + iterationSlug + "/";
        final HLocale hLocale =
                localeDAO.findByLocaleId(new LocaleId(localeId));
        final String mappedLocale = hLocale.getLocaleId().getId();
        final String localeDirectory = projectDirectory + mappedLocale + "/";
        final File downloadFile =
                fileSystemServiceImpl.createDownloadStagingFile("zip");
        final FileOutputStream output = new FileOutputStream(downloadFile);
        final ZipOutputStream zipOutput = new ZipOutputStream(output);
        zipOutput.setMethod(ZipOutputStream.DEFLATED);
        final PoWriter2 poWriter = new PoWriter2(false, !isPoProject);
        final Set<String> extensions = new HashSet<String>();
        extensions.add("gettext");
        extensions.add("comment");
        // Generate the download descriptor file
        String downloadId = fileSystemServiceImpl.createDownloadDescriptorFile(
                downloadFile,
                projectSlug + "_" + iterationSlug + "_" + localeId + ".zip",
                userName);
        // Add the config file at the root of the project directory
        String configFilename = projectDirectory
                + configurationServiceImpl.getConfigurationFileName();
        zipOutput.putNextEntry(new ZipEntry(configFilename));
        zipOutput
                .write(configurationServiceImpl
                        .getConfigForOfflineTranslation(projectSlug,
                                iterationSlug, hLocale)
                        .getBytes(StandardCharsets.UTF_8));
        zipOutput.closeEntry();
        handle.increaseProgress(1);
        final List<HDocument> allIterationDocs = documentDAO
                .getAllByProjectIteration(projectSlug, iterationSlug);
        for (HDocument document : allIterationDocs) {
            // Stop the process if signaled to do so
            if (handleOpt.isPresent() && handleOpt.get().isCancelled()) {
                zipOutput.close();
                downloadFile.delete();
                fileSystemServiceImpl.deleteDownloadDescriptorFile(downloadId);
                return null;
            }
            TranslationsResource translationResource =
                    new TranslationsResource();
            List<HTextFlowTarget> hTargets =
                    textFlowTargetDAO.findTranslations(document, hLocale);
            resourceUtils.transferToTranslationsResource(translationResource,
                    document, hLocale, extensions, hTargets,
                    Optional.<String> absent());
            Resource res = resourceUtils.buildResource(document);
            String filename = localeDirectory + document.getDocId() + ".po";
            zipOutput.putNextEntry(new ZipEntry(filename));
            poWriter.writePo(zipOutput, "UTF-8", res, translationResource);
            zipOutput.closeEntry();
            if (handleOpt.isPresent()) {
                handleOpt.get().increaseProgress(1);
            }
        }
        zipOutput.flush();
        zipOutput.close();
        return downloadId;
    }

    @Override
    @Async
    public Future<String> startBuildingTranslationFileArchive(
            String projectSlug, String iterationSlug, String localeId,
            String userName, AsyncTaskHandle<String> handle) throws Exception {
        String archiveId = buildTranslationFileArchive(projectSlug,
                iterationSlug, localeId, userName, handle);
        return AsyncTaskResult.taskResult(archiveId);
    }

    private void prepareHandle(AsyncTaskHandle<String> handle,
            String projectSlug, String iterationSlug) {
        // Max documents to process
        final List<HDocument> allIterationDocs = documentDAO
                .getAllByProjectIteration(projectSlug, iterationSlug);
        handle.setMaxProgress(allIterationDocs.size() + 1); // all files plus
        // the zanata.xml
        // file
    }

    private boolean isPoProject(String projectSlug, String versionSlug) {
        HProjectIteration projectIteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        ProjectType type = projectIteration.getProjectType();
        if (type == null) {
            type = projectIteration.getProject().getDefaultProjectType();
        }
        return type == Gettext || type == Podir;
    }
}
