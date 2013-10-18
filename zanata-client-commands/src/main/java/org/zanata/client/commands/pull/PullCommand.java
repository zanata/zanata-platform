package org.zanata.client.commands.pull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.etag.ETagCacheEntry;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.RestUtil;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.HashUtil;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PullCommand extends PushPullCommand<PullOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PullCommand.class);

    private static final Map<String, Class<? extends PullStrategy>> strategies =
            new HashMap<String, Class<? extends PullStrategy>>();

    {
        strategies.put(PROJECT_TYPE_UTF8_PROPERTIES,
                UTF8PropertiesStrategy.class);
        strategies.put(PROJECT_TYPE_PROPERTIES, PropertiesStrategy.class);
        strategies.put(PROJECT_TYPE_GETTEXT, GettextPullStrategy.class);
        strategies.put(PROJECT_TYPE_PUBLICAN, GettextDirStrategy.class);
        strategies.put(PROJECT_TYPE_XLIFF, XliffStrategy.class);
        strategies.put(PROJECT_TYPE_XML, XmlStrategy.class);
        strategies.put(PROJECT_TYPE_OFFLINE_PO, OfflinePoStrategy.class);
    }

    public PullCommand(PullOptions opts) {
        super(opts);
    }

    public PullCommand(PullOptions opts, ZanataProxyFactory factory,
            ISourceDocResource sourceDocResource,
            ITranslatedDocResource translationResources, URI uri) {
        super(opts, factory, sourceDocResource, translationResources, uri);
    }

    private PullStrategy createStrategy(String strategyType)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Class<? extends PullStrategy> clazz = strategies.get(strategyType);
        if (clazz == null) {
            throw new RuntimeException("unknown project type: "
                    + getOpts().getProjectType());
        }
        Constructor<? extends PullStrategy> ctor =
                clazz.getDeclaredConstructor(PullOptions.class);
        assert ctor != null : "strategy must have constructor which accepts PullOptions";
        return ctor.newInstance(getOpts());
    }

    private void logOptions() {
        logOptions(log, getOpts());
        log.info("Create skeletons for untranslated messages/files: {}",
                getOpts().getCreateSkeletons());
        if (getOpts().getFromDoc() != null) {
            log.info("From document: {}", getOpts().getFromDoc());
        }
        if (getOpts().isDryRun()) {
            log.info("DRY RUN: no permanent changes will be made");
        }
    }

    /**
     * @param logger
     * @param opts
     */
    public static void logOptions(Logger logger, PullOptions opts) {
        logger.info("Server: {}", opts.getUrl());
        logger.info("Project: {}", opts.getProj());
        logger.info("Version: {}", opts.getProjectVersion());
        logger.info("Username: {}", opts.getUsername());
        logger.info("Project type: {}", opts.getProjectType());
        logger.info("Enable modules: {}", opts.getEnableModules());
        logger.info("Using ETag cache: {}", opts.getUseCache());
        logger.info("Purging ETag cache beforehand: {}", opts.getPurgeCache());
        if (opts.getEnableModules()) {
            logger.info("Current Module: {}", opts.getCurrentModule());
            if (opts.isRootModule()) {
                logger.info("Root module: YES");
                if (logger.isDebugEnabled()) {
                    logger.debug("Modules: {}",
                            StringUtils.join(opts.getAllModules(), ", "));
                }
            }
        }
        logger.info("Locales to pull: {}", opts.getLocaleMapList());
        logger.info("Encode tab as \\t: {}", opts.getEncodeTabs());
        logger.info("Current directory: {}", System.getProperty("user.dir"));
        if (opts.getPullType() == PushPullType.Source) {
            logger.info("Pulling source documents only");
            logger.info("Source-language directory (originals): {}",
                    opts.getSrcDir());
        } else if (opts.getPullType() == PushPullType.Trans) {
            logger.info("Pulling target documents (translations) only");
            logger.info("Target-language base directory (translations): {}",
                    opts.getTransDir());
        } else {
            logger.info("Pulling source and target (translation) documents");
            logger.info("Source-language directory (originals): {}",
                    opts.getSrcDir());
            logger.info("Target-language base directory (translations): {}",
                    opts.getTransDir());
        }
    }

    @Override
    public void performWork() throws Exception {
        logOptions();

        LocaleList locales = getOpts().getLocaleMapList();
        if (locales == null && (getOpts().getPullType() != PushPullType.Source))
            throw new ConfigException("no locales specified");
        PullStrategy strat = createStrategy(getOpts().getProjectType());

        if (strat.isTransOnly()
                && getOpts().getPullType() == PushPullType.Source) {
            log.error("You are trying to pull source only, but source is not available for this project type.\n");
            log.info("Nothing to do. Aborting.\n");
            return;
        }

        List<String> unsortedDocNamesForModule =
                getQualifiedDocNamesForCurrentModuleFromServer();
        SortedSet<String> docNamesForModule =
                new TreeSet<String>(unsortedDocNamesForModule);

        SortedSet<String> docsToPull = docNamesForModule;
        if (getOpts().getFromDoc() != null) {
            if (getOpts().getEnableModules()) {
                if (belongsToCurrentModule(getOpts().getFromDoc())) {
                    docsToPull =
                            getDocsAfterFromDoc(getOpts().getFromDoc(),
                                    docsToPull);
                }
                // else fromDoc does not apply to this module
            } else {
                docsToPull =
                        getDocsAfterFromDoc(getOpts().getFromDoc(), docsToPull);
            }
        }

        // TODO compare docNamesForModule with localDocNames, offer to delete
        // obsolete translations from filesystem
        if (docNamesForModule.isEmpty()) {
            log.info("No documents in remote module: {}; nothing to do",
                    getOpts().getCurrentModule());
            return;
        }
        log.info("Pulling {} of {} docs for this module from the server",
                docsToPull.size(), docNamesForModule.size());
        log.debug("Doc names: {}", docsToPull);

        PushPullType pullType = getOpts().getPullType();
        boolean pullSrc =
                pullType == PushPullType.Both
                        || pullType == PushPullType.Source;
        boolean pullTarget =
                pullType == PushPullType.Both || pullType == PushPullType.Trans;

        if (pullSrc && strat.isTransOnly()) {
            log.warn("Source is not available for this project type. Source will not be pulled.\n");
            pullSrc = false;
        }

        if (pullSrc) {
            log.warn("Pull Type set to '"
                    + pullType
                    + "': existing source-language files may be overwritten/deleted");
            confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
        } else {
            confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
        }

        if (getOpts().getPurgeCache()) {
            eTagCache.clear();
        }

        for (String qualifiedDocName : docsToPull) {
            try {
                Resource doc = null;
                String localDocName = unqualifiedDocName(qualifiedDocName);
                // TODO follow a Link instead of generating the URI
                String docUri =
                        RestUtil.convertToDocumentURIId(qualifiedDocName);
                boolean createSkeletons = getOpts().getCreateSkeletons();
                if (strat.needsDocToWriteTrans() || pullSrc || createSkeletons) {
                    ClientResponse<Resource> resourceResponse =
                            sourceDocResource.getResource(docUri,
                                    strat.getExtensions());
                    ClientUtility.checkResult(resourceResponse, uri);
                    doc = resourceResponse.getEntity();
                    doc.setName(localDocName);
                }
                if (pullSrc) {
                    writeSrcDoc(strat, doc);
                }

                if (pullTarget) {
                    for (LocaleMapping locMapping : locales) {
                        LocaleId locale = new LocaleId(locMapping.getLocale());
                        String eTag = null;
                        File transFile =
                                strat.getTransFileToWrite(localDocName,
                                        locMapping);
                        ETagCacheEntry eTagCacheEntry =
                                eTagCache.findEntry(localDocName,
                                        locale.getId());

                        if (getOpts().getUseCache() && eTagCacheEntry != null) {
                            // Check the last updated date on the file matches
                            // what's in the cache
                            // only then use the cached ETag
                            if (transFile.exists()
                                    && Long.toString(transFile.lastModified())
                                            .equals(eTagCacheEntry
                                                    .getLocalFileTime())) {
                                eTag = eTagCacheEntry.getServerETag();
                            }
                        }

                        ClientResponse<TranslationsResource> transResponse =
                                translationResources.getTranslations(docUri,
                                        locale, strat.getExtensions(),
                                        createSkeletons, eTag);
                        TranslationsResource targetDoc;

                        // ignore 404 (no translation yet for specified
                        // document)
                        if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND) {
                            targetDoc = null;
                            if (!createSkeletons) {
                                log.info(
                                        "No translations found in locale {} for document {}",
                                        locale, localDocName);
                                // We need to release connection.
                                // see
                                // http://stackoverflow.com/questions/4612573/exception-using-httprequest-execute-invalid-use-of-singleclientconnmanager-c
                                transResponse.releaseConnection();
                                continue;
                            }
                        }
                        // 304 NOT MODIFIED (the document can stay the same)
                        else if (transResponse.getResponseStatus() == Response.Status.NOT_MODIFIED) {
                            targetDoc = null;
                            log.info(
                                    "No changes in translations for locale {} and document {}",
                                    locale, localDocName);

                            // Check the file's MD5 matches what's stored in the
                            // cache. If not, it needs to be fetched again (with
                            // no etag)
                            String fileChecksum =
                                    HashUtil.getMD5Checksum(transFile);
                            if (!fileChecksum.equals(eTagCacheEntry
                                    .getLocalFileMD5())) {
                                transResponse =
                                        translationResources.getTranslations(
                                                docUri, locale,
                                                strat.getExtensions(),
                                                createSkeletons, null);
                                ClientUtility.checkResult(transResponse, uri);
                                targetDoc = transResponse.getEntity();
                            }
                        } else {
                            ClientUtility.checkResult(transResponse, uri);
                            targetDoc = transResponse.getEntity();
                        }
                        if (targetDoc != null || createSkeletons) {
                            writeTargetDoc(strat, localDocName, locMapping,
                                    doc, targetDoc,
                                    transResponse.getResponseHeaders()
                                            .getFirst(HttpHeaders.ETAG));
                        }
                    }

                    // write the cache
                    super.storeETagCache();
                }

            } catch (RuntimeException e) {
                String message =
                        "Operation failed.\n\n"
                                + "    To retry from the last document, please set the following option(s):\n\n"
                                + "        ";
                if (getOpts().getEnableModules()) {
                    message +=
                            "--resume-from " + getOpts().getCurrentModule(true)
                                    + " ";
                }
                // Note: '.' is included after trailing newlines to prevent them
                // being stripped,
                // since stripping newlines can cause extra text to be appended
                // to the options.
                message +=
                        getOpts().buildFromDocArgument(qualifiedDocName)
                                + "\n\n.";
                throw new RuntimeException(message, e);
            }
        }

    }

    /**
     * Returns a list with all documents before fromDoc removed.
     *
     * @param fromDoc
     * @param docNames
     * @return a set with only the documents after fromDoc, inclusive
     * @throws RuntimeException
     *             if no document with the specified name exists
     */
    private SortedSet<String> getDocsAfterFromDoc(String fromDoc,
            SortedSet<String> docNames) {
        SortedSet<String> docsToPull;
        if (!docNames.contains(fromDoc)) {
            throw new RuntimeException(
                    "Document with id "
                            + fromDoc
                            + " not found, unable to start pull from unknown document. Aborting.");
        }
        docsToPull = docNames.tailSet(fromDoc);
        int numSkippedDocs = docNames.size() - docsToPull.size();
        log.info("Skipping {} document(s) before {}.", numSkippedDocs, fromDoc);
        return docsToPull;
    }

    private void writeSrcDoc(PullStrategy strat, Resource doc)
            throws IOException {
        if (!getOpts().isDryRun()) {
            log.info("Writing source file for document {}", doc.getName());
            strat.writeSrcFile(doc);
        } else {
            log.info(
                    "Writing source file for document {} (skipped due to dry run)",
                    doc.getName());
        }
    }

    /**
     *
     * @param strat
     * @param localDocName
     * @param locMapping
     * @param docWithLocalName
     *            may be null if needsDocToWriteTrans() returns false
     * @param targetDoc
     * @throws IOException
     */
    private void writeTargetDoc(PullStrategy strat, String localDocName,
            LocaleMapping locMapping, Resource docWithLocalName,
            TranslationsResource targetDoc, String serverETag)
            throws IOException {
        if (!getOpts().isDryRun()) {
            log.info("Writing translation file in locale {} for document {}",
                    locMapping.getLocalLocale(), localDocName);
            FileDetails fileDetails =
                    strat.writeTransFile(docWithLocalName, localDocName,
                            locMapping, targetDoc);

            // Insert to cache if the strategy returned file details and we are
            // using the cache
            if (getOpts().getUseCache() && fileDetails != null) {
                eTagCache.addEntry(new ETagCacheEntry(localDocName, locMapping
                        .getLocale(), Long.toString(fileDetails.getFile()
                        .lastModified()), fileDetails.getMd5(), serverETag));
            }
        } else {
            log.info(
                    "Writing translation file in locale {} for document {} (skipped due to dry run)",
                    locMapping.getLocalLocale(), localDocName);
        }
    }

}
