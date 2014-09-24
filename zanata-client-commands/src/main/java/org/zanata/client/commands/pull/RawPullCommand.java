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
import java.net.URI;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IFileResource;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.service.FileResource;
import com.google.common.annotations.VisibleForTesting;

/**
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPullCommand extends PushPullCommand<PullOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(RawPullCommand.class);

    private IFileResource fileResource;

    public RawPullCommand(PullOptions opts) {
        super(opts);
        this.fileResource = getRequestFactory().getFileResource();
    }

    @VisibleForTesting
    public RawPullCommand(PullOptions opts,
            ZanataProxyFactory factory,
            ISourceDocResource sourceDocResource,
            ITranslatedDocResource translationResources,
            URI uri, IFileResource fileResource) {
        super(opts, factory, sourceDocResource, translationResources, uri);
        this.fileResource = fileResource;
    }

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

        if (pullSrc) {
            log.warn("Pull Type set to '"
                    + pullType
                    + "': existing source-language files may be overwritten/deleted");
            confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
        } else {
            confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
        }

        for (String qualifiedDocName : docsToPull) {
            // TODO add filtering by file type? e.g. pull all dtd documents
            // only.

            try {
                String localDocName = unqualifiedDocName(qualifiedDocName);

                if (pullSrc) {
                    ClientResponse response =
                            fileResource.downloadSourceFile(
                                    getOpts().getProj(), getOpts()
                                            .getProjectVersion(),
                                    FileResource.FILETYPE_RAW_SOURCE_DOCUMENT,
                                    qualifiedDocName);
                    if (response.getResponseStatus() == Status.NOT_FOUND) {
                        log.warn(
                                "No source document file is available for [{}]. Skipping.",
                                qualifiedDocName);
                    } else {
                        ClientUtility.checkResult(response, uri);
                        InputStream srcDoc =
                                (InputStream) response
                                        .getEntity(InputStream.class);
                        if (srcDoc != null) {
                            try {
                                strat.writeSrcFile(localDocName, srcDoc);
                            } finally {
                                srcDoc.close();
                            }
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

                    for (LocaleMapping locMapping : locales) {
                        LocaleId locale = new LocaleId(locMapping.getLocale());
                        ClientResponse response =
                                fileResource.downloadTranslationFile(getOpts()
                                        .getProj(), getOpts()
                                        .getProjectVersion(), locale.getId(),
                                        fileExtension, qualifiedDocName);
                        if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                            log.info(
                                    "No translation document file found in locale {} for document [{}]",
                                    locale, qualifiedDocName);
                        } else {
                            ClientUtility.checkResult(response, uri);
                            InputStream transDoc =
                                    (InputStream) response
                                            .getEntity(InputStream.class);
                            if (transDoc != null) {
                                try {
                                    strat.writeTransFile(localDocName, locMapping,
                                            transDoc);
                                } finally {
                                    transDoc.close();
                                }
                            }
                        }
                    }
                }
            } catch (IOException | RuntimeException e) {
                log.error(
                        "Operation failed: "+e.getMessage()+"\n\n"
                        + "    To retry from the last document, please add the option: {}\n",
                        getOpts().buildFromDocArgument(qualifiedDocName));
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
