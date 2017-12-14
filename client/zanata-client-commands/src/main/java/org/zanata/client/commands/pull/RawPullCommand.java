/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.client.commands.pull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtil;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.service.FileResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPullCommand extends PushPullCommand<PullOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(RawPullCommand.class);

    private FileResourceClient fileResourceClient;

    public RawPullCommand(PullOptions opts) {
        super(opts);
        fileResourceClient = getClientFactory().getFileResourceClient();
    }

    @VisibleForTesting
    public RawPullCommand(PullOptions opts,
            FileResourceClient fileResourceClient,
            RestClientFactory clientFactory) {
        super(opts, clientFactory);
        this.fileResourceClient = fileResourceClient;
    }

    @SuppressFBWarnings({"SLF4J_SIGN_ONLY_FORMAT"})
    @Override
    public void run() throws IOException {
        PullCommand.logOptions(log, getOpts());
        if (getOpts().isDryRun()) {
            log.info("DRY RUN: no permanent changes will be made");
        }

        log.warn("Using EXPERIMENTAL project type 'file'.");

        LocaleList locales = getOpts().getLocaleMapList();
        if (locales == null) {
            throw new ConfigException("no locales specified");
        }
        RawPullStrategy strat = new RawPullStrategy();
        strat.setPullOptions(getOpts());

        List<String> docNamesForModule =
                getQualifiedDocNamesForCurrentModuleFromServer();
        SortedSet<String> localDocNames =
                new TreeSet<String>(docNamesForModule);

        SortedSet<String> docsToPull = localDocNames;
        if (getOpts().getFromDoc() != null) {
            if (!localDocNames.contains(getOpts().getFromDoc())) {
                log.error(
                        "Document with id {} not found, unable to start pull from unknown document. Aborting.",
                        getOpts().getFromDoc());
                // FIXME should this be throwing an exception to properly abort?
                // need to see behaviour with modules
                return;
            }
            docsToPull = localDocNames.tailSet(getOpts().getFromDoc());
            int numSkippedDocs = localDocNames.size() - docsToPull.size();
            log.info("Skipping {} document(s) before {}.", numSkippedDocs,
                    getOpts().getFromDoc());
        }

        // TODO compare docNamesForModule with localDocNames, offer to delete
        // obsolete translations from filesystem
        if (docsToPull.isEmpty()) {
            log.info("No documents in remote module: {}; nothing to do",
                    getOpts().getCurrentModule());
            return;
        } else {
            log.info("Source documents on server:");
            for (String docName : localDocNames) {
                if (docsToPull.contains(docName)) {
                    log.info("           {}", docName);
                } else {
                    log.info("(to skip)  {}", docName);
                }
            }
        }

        log.info("Pulling {} of {} docs for this module from the server",
                docsToPull.size(), localDocNames.size());
        log.debug("Doc names: {}", localDocNames);

        PushPullType pullType = getOpts().getPullType();
        boolean pullSrc =
                pullType == PushPullType.Both
                        || pullType == PushPullType.Source;
        boolean pullTarget =
                pullType == PushPullType.Both || pullType == PushPullType.Trans;

        if (needToGetStatistics(pullTarget)) {
            log.info("Setting minimum document completion percentage may potentially increase the processing time.");
        }

        if (pullSrc) {
            log.warn("Pull Type set to '{}': existing source-language files may be overwritten/deleted", pullType);
            confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
        } else {
            confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
        }

        Optional<Map<String, Map<LocaleId, TranslatedPercent>>> optionalStats =
                prepareStatsIfApplicable(pullTarget, locales);

        for (String qualifiedDocName : docsToPull) {
            // TODO add filtering by file type? e.g. pull all dtd documents
            // only.

            try {
                String localDocName = unqualifiedDocName(qualifiedDocName);

                if (pullSrc) {
                    Response response;
                    try {
                        response = fileResourceClient.downloadSourceFile(
                                getOpts().getProj(), getOpts()
                                        .getProjectVersion(),
                                FileResource.FILETYPE_RAW_SOURCE_DOCUMENT,
                                qualifiedDocName);
                        InputStream srcDoc = response
                                .readEntity(InputStream.class);
                        if (srcDoc != null) {
                            try {
                                strat.writeSrcFile(localDocName, srcDoc);
                            } finally {
                                srcDoc.close();
                            }
                        }
                    } catch (ResponseProcessingException e) {
                        if (e.getResponse().getStatus() == 404) {
                            log.warn(
                                    "No source document file is available for [{}]. Skipping.",
                                    qualifiedDocName);
                        } else {
                            throw e;
                        }
                    }
                }

                if (pullTarget) {
                    String fileExtension;
                    if (getOpts().getIncludeFuzzy()) {
                        fileExtension =
                                FileResource.FILETYPE_TRANSLATED_APPROVED_AND_FUZZY;
                    } else {
                        fileExtension =
                                FileResource.FILETYPE_TRANSLATED_APPROVED;
                    }

                    List<LocaleId> skippedLocales = Lists.newArrayList();
                    for (LocaleMapping locMapping : locales) {
                        LocaleId locale = new LocaleId(locMapping.getLocale());

                        if (shouldPullThisLocale(optionalStats, localDocName, locale)) {
                            pullDocForLocale(strat, qualifiedDocName, localDocName,
                                    fileExtension,
                                    locMapping, locale);
                        } else {
                            skippedLocales.add(locale);
                        }

                    }
                    if (!skippedLocales.isEmpty()) {
                        log.info(
                                "Translation file for document {} for locales {} are skipped due to insufficient completed percentage",
                                localDocName, skippedLocales);
                    }
                }
            } catch (IOException | RuntimeException e) {
                log.error(
                        "Operation failed: {}\n\n"
                                + "    To retry from the last document, please add the option: {}\n",
                        e.getMessage(),
                        getOpts().buildFromDocArgument(qualifiedDocName));
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void pullDocForLocale(RawPullStrategy strat,
            String qualifiedDocName, String localDocName, String fileExtension,
            LocaleMapping locMapping, LocaleId locale) throws IOException {
        Response response = null;
        try {
            response = fileResourceClient.downloadTranslationFile(getOpts()
                                    .getProj(), getOpts()
                                    .getProjectVersion(), locale.getId(),
                            fileExtension, qualifiedDocName);
            InputStream transDoc = response.readEntity(InputStream.class);
            if (transDoc != null) {
                try {
                    String fileName =
                            ClientUtil.getFileNameFromHeader(
                                    response.getStringHeaders());
                    String targetFileExt = FilenameUtils
                            .getExtension(fileName);

                    Optional<String> translationFileExtension =
                            Optional.fromNullable(targetFileExt);

                    strat.writeTransFile(localDocName,
                            locMapping, transDoc, translationFileExtension);
                } finally {
                    transDoc.close();
                }
            }
        } catch (ResponseProcessingException e) {
            if (e.getResponse().getStatus() == 404) {
                log.info(
                        "No translation document file found in locale {} for document [{}]",
                        locale, qualifiedDocName);
            } else {
                throw e;
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
