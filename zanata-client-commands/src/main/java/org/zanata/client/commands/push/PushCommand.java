package org.zanata.client.commands.push;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.adapter.properties.PropWriter;
import org.zanata.adapter.xliff.XliffCommon.ValidationType;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.client.util.ConsoleUtils;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.AsyncProcessClient;
import org.zanata.rest.client.CopyTransClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.all;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PushCommand extends PushPullCommand<PushOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PushCommand.class);
    private static final int POLL_PERIOD = 250;

    private static final Map<String, AbstractPushStrategy> strategies =
            new HashMap<String, AbstractPushStrategy>();

    private CopyTransClient copyTransClient;
    private AsyncProcessClient asyncProcessClient;

    public interface TranslationResourcesVisitor {
        void visit(LocaleMapping locale, TranslationsResource targetDoc);
    }

    {
        strategies.put(PROJECT_TYPE_UTF8_PROPERTIES, new PropertiesStrategy(
            PropWriter.CHARSET.UTF8));
        strategies.put(PROJECT_TYPE_PROPERTIES, new PropertiesStrategy());
        strategies.put(PROJECT_TYPE_GETTEXT, new GettextPushStrategy());
        strategies.put(PROJECT_TYPE_PUBLICAN, new GettextDirStrategy());
        strategies.put(PROJECT_TYPE_XLIFF, new XliffStrategy());
        strategies.put(PROJECT_TYPE_XML, new XmlStrategy());
        strategies.put(
                PROJECT_TYPE_OFFLINE_PO,
                new OfflinePoStrategy(getClientFactory()
                        .getSourceDocResourceClient(getOpts().getProj(),
                                getOpts()
                .getProjectVersion())));
    }

    public PushCommand(PushOptions opts) {
        super(opts);
        copyTransClient = getClientFactory().getCopyTransClient();
        asyncProcessClient = getClientFactory().getAsyncProcessClient();
    }

    public PushCommand(PushOptions opts,
            CopyTransClient copyTransClient,
            AsyncProcessClient asyncProcessClient,
            RestClientFactory clientFactory) {
        super(opts,
                clientFactory);
        this.copyTransClient = copyTransClient;
        this.asyncProcessClient = asyncProcessClient;
    }

    public AbstractPushStrategy getStrategy(PushOptions pushOptions) {
        AbstractPushStrategy strat =
                strategies.get(pushOptions.getProjectType());
        if (strat == null) {
            throw new RuntimeException("unknown project type: "
                    + pushOptions.getProjectType());
        }
        strat.setPushOptions(pushOptions);
        strat.init();
        return strat;
    }

    public static void logOptions(Logger logger, PushOptions opts) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        logger.info("Server: {}", opts.getUrl());
        logger.info("Project: {}", opts.getProj());
        logger.info("Version: {}", opts.getProjectVersion());
        logger.info("Username: {}", opts.getUsername());
        logger.info("Project type: {}", opts.getProjectType());
        logger.info("Source language: {}", opts.getSourceLang());

        String copyTransMssg = "" + opts.getCopyTrans();
        if (opts.getCopyTrans() && opts.getPushType() == PushPullType.Trans) {
            copyTransMssg = "disabled since pushType=Trans";
        }
        logger.info("Copy previous translations: {}", copyTransMssg);
        if (!opts.getCopyTrans() && opts.getPushType() != PushPullType.Trans) {
            logger.warn("As of Zanata Client 3.8.0, copyTrans is disabled by default.");
        }

        logger.info("Merge type: {}", opts.getMergeType());
        logger.info("Enable modules: {}", opts.getEnableModules());

        if (opts.getEnableModules()) {
            logger.info("Current module: {}", opts.getCurrentModule());
            if (opts.isRootModule()) {
                logger.info("Root module: YES");
                if (logger.isDebugEnabled()) {
                    logger.debug("Modules: {}",
                            StringUtils.join(opts.getAllModules(), ", "));
                }
            }
        }
        logger.info("Include patterns: {}",
                StringUtils.join(opts.getIncludes(), " "));
        logger.info("Exclude patterns: {}",
                StringUtils.join(opts.getExcludes(), " "));
        logger.info("Case sensitive: {}", opts.getCaseSensitive());
        logger.info("Default excludes: {}", opts.getDefaultExcludes());
        log.info("Exclude locale filenames: {}",
                opts.getExcludeLocaleFilenames());

        if (opts.getPushType() == PushPullType.Trans) {
            logger.info("Pushing target documents only");
            logger.info("Locales to push: {}", opts.getLocaleMapList());
        } else if (opts.getPushType() == PushPullType.Source) {
            logger.info("Pushing source documents only");
        } else {
            logger.info("Pushing source and target documents");
            logger.info("Locales to push: {}", opts.getLocaleMapList());
        }

        logger.info("Current directory: {}", System.getProperty("user.dir"));
        logger.info("Source directory (originals): {}", opts.getSrcDir());
        if (opts.getPushType() == PushPullType.Both
                || opts.getPushType() == PushPullType.Trans) {
            logger.info("Target base directory (translations): {}",
                    opts.getTransDir());

            logger.info("Is my translations: {}", opts.isMyTrans());
        }
        if (opts.getFromDoc() != null) {
            logger.info("From document: {}", opts.getFromDoc());
        }
        if (opts.isDryRun()) {
            logger.info("DRY RUN: no permanent changes will be made");
        }
        if (opts.getProjectType().equalsIgnoreCase("xliff")) {
            validateValidation(opts.getValidate());
            log.info("Validate option: {}", opts.getValidate());
        }
    }

    private static void validateValidation(String validate) {
        if (!validate.equalsIgnoreCase(ValidationType.CONTENT.toString())
                && !validate.equalsIgnoreCase(ValidationType.XSD.toString())) {
            throw new RuntimeException("unknown validate option: " + validate);
        }
    }

    private boolean mergeAuto() {
        return getOpts().getMergeType().toUpperCase()
                .equals(MergeType.AUTO.name());
    }

    private boolean pushSource() {
        return getOpts().getPushType() == PushPullType.Both
                || getOpts().getPushType() == PushPullType.Source;
    }

    private boolean pushTrans() {
        return getOpts().getPushType() == PushPullType.Both
                || getOpts().getPushType() == PushPullType.Trans;
    }

    @Override
    public void run() throws Exception {
        logOptions(log, getOpts());
        pushCurrentModule();

        if (pushSource() && getOpts().getEnableModules()
                && getOpts().isRootModule()) {
            List<String> obsoleteDocs =
                    getObsoleteDocNamesForProjectIterationFromServer();
            log.info("found {} docs in obsolete modules (or no module): {}",
                    obsoleteDocs.size(), obsoleteDocs);
            if (getOpts().getDeleteObsoleteModules() && !obsoleteDocs.isEmpty()) {
                // offer to delete obsolete documents
                confirmWithUser("Do you want to delete all documents from the server which don't belong to any module in the Maven reactor?\n");
                deleteSourceDocsFromServer(obsoleteDocs);
            } else {
                log.warn(
                        "found {} docs in obsolete modules (or no module).  use -Dzanata.deleteObsoleteModules to delete them",
                        obsoleteDocs.size());
            }
        }
    }

    /**
     * gets doc list from server, returns a list of qualified doc names from
     * obsolete modules, or from no module.
     */
    protected List<String> getObsoleteDocNamesForProjectIterationFromServer() {
        if (!getOpts().getEnableModules())
            return Collections.emptyList();
        List<ResourceMeta> remoteDocList =
                getDocListForProjectIterationFromServer();

        Pattern p = Pattern.compile(getOpts().getDocNameRegex());
        Set<String> modules = new HashSet<String>(getOpts().getAllModules());

        List<String> obsoleteDocs = new ArrayList<String>();
        for (ResourceMeta doc : remoteDocList) {
            // NB ResourceMeta.name == HDocument.docId
            String docName = doc.getName();

            Matcher matcher = p.matcher(docName);
            if (matcher.matches()) {
                String module = matcher.group(1);
                if (modules.contains(module)) {
                    log.debug("doc {} belongs to non-obsolete module {}",
                            docName, module);
                } else {
                    obsoleteDocs.add(docName);
                    log.info("doc {} belongs to obsolete module {}", docName,
                            module);
                }
            } else {
                obsoleteDocs.add(docName);
                log.warn("doc {} doesn't belong to any module", docName);
            }
        }
        return obsoleteDocs;
    }

    private void pushCurrentModule() throws IOException, RuntimeException {
        AbstractPushStrategy strat = getStrategy(getOpts());
        File sourceDir = getOpts().getSrcDir();

        if (!sourceDir.exists() && !strat.isTransOnly()) {
            if (getOpts().getEnableModules()) {
                log.info("source directory '" + sourceDir
                        + "' not found; skipping docs push for module "
                        + getOpts().getCurrentModule());
                return;
            } else {
                throw new RuntimeException("directory '" + sourceDir
                        + "' does not exist - check "
                        + getOpts().getSrcDirParameterName() + " option");
            }
        }

        final StringSet extensions = strat.getExtensions();

        // to save memory, we don't load all the docs into a HashMap
        Set<String> unsortedDocNames =
                strat.findDocNames(sourceDir, getOpts().getIncludes(),
                        getOpts().getExcludes(),
                        getOpts().getDefaultExcludes(), getOpts()
                                .getCaseSensitive(), getOpts()
                                .getExcludeLocaleFilenames());
        SortedSet<String> localDocNames = new TreeSet<String>(unsortedDocNames);

        SortedSet<String> docsToPush = localDocNames;
        if (getOpts().getFromDoc() != null) {
            if (getOpts().getEnableModules()) {
                if (belongsToCurrentModule(getOpts().getFromDoc())) {
                    docsToPush =
                            getDocsAfterFromDoc(unqualifiedDocName(getOpts()
                                    .getFromDoc()), localDocNames);
                }
                // else fromDoc does not apply to this module
            } else {
                docsToPush =
                        getDocsAfterFromDoc(getOpts().getFromDoc(),
                                localDocNames);
            }
        }

        if (localDocNames.isEmpty()) {
            log.info("No source documents found.");
        } else {
            log.info("Found source documents:");
            for (String docName : localDocNames) {
                if (docsToPush.contains(docName)) {
                    log.info("           {}", docName);
                } else {
                    log.info("(to skip)  {}", docName);
                }
            }
        }

        List<String> obsoleteDocs = Collections.emptyList();
        if (pushSource() && !strat.isTransOnly()) {
            obsoleteDocs = getObsoleteDocsInModuleFromServer(localDocNames);
        }
        if (obsoleteDocs.isEmpty()) {
            if (localDocNames.isEmpty()) {
                log.info("no documents in module: {}; nothing to do", getOpts()
                        .getCurrentModule());
                return;
            } else {
                // nop
            }
        } else {
            log.warn(
                    "Found {} obsolete docs on the server which will be DELETED",
                    obsoleteDocs.size());
            log.info("Obsolete docs: {}", obsoleteDocs);
        }

        if (pushTrans() && getOpts().getLocaleMapList() == null) {
            throw new ConfigException("pushType set to '"
                    + getOpts().getPushType()
                    + "', but project has no locales configured");
        }

        if (pushTrans()) {
            log.warn("pushType set to '"
                    + getOpts().getPushType()
                    + "': existing translations on server may be overwritten/deleted");
        }

        if (strat.isTransOnly()) {
            switch (getOpts().getPushType()) {
            case Source:
                log.error("You are trying to push source only, but source is not available for this project type.\n");
                log.info("Nothing to do. Aborting\n");
                return;
            case Both:
                log.warn("Source is not available for this project type. Source will not be pushed.\n");
                confirmWithUser("This will overwrite existing TRANSLATIONS on the server.\n");
                break;
            case Trans:
                confirmWithUser("This will overwrite existing TRANSLATIONS on the server.\n");
                break;
            }
        } else {
            if (pushTrans()) {
                if (getOpts().getPushType() == PushPullType.Both) {
                    confirmWithUser("This will overwrite existing documents AND TRANSLATIONS on the server, and delete obsolete documents.\n");
                } else if (getOpts().getPushType() == PushPullType.Trans) {
                    confirmWithUser("This will overwrite existing TRANSLATIONS on the server.\n");
                }
            } else {
                confirmWithUser("This will overwrite existing source documents on the server, and delete obsolete documents.\n");
            }
        }

        for (final String localDocName : docsToPush) {
            try {
                final String qualifiedDocName = qualifiedDocName(localDocName);
                final String docUri =
                        RestUtil.convertToDocumentURIId(qualifiedDocName);
                final Resource srcDoc;
                if (strat.isTransOnly()) {
                    srcDoc = null;
                } else {
                    srcDoc = strat.loadSrcDoc(sourceDir, localDocName);
                    srcDoc.setName(qualifiedDocName);
                    debug(srcDoc);

                    if (pushSource()) {
                        pushSrcDocToServer(docUri, srcDoc, extensions);
                    }
                }

                if (pushTrans()) {
                    strat.visitTranslationResources(localDocName, srcDoc,
                            new TranslationResourcesVisitor() {
                                @Override
                                public void visit(LocaleMapping locale,
                                        TranslationsResource targetDoc) {
                                    debug(targetDoc);
                                    stripUntranslatedEntriesIfMergeTypeIsNotImport(getOpts(),
                                            targetDoc);
                                    if (targetDoc.getTextFlowTargets()
                                            .isEmpty()) {
                                        log.debug(
                                                "Skip translation file {}({}) since it has no translation in it",
                                                localDocName, locale);
                                        return;
                                    }
                                    pushTargetDocToServer(docUri, locale,
                                            qualifiedDocName, targetDoc,
                                            extensions);
                                }
                            });
                }

                // Copy Trans after pushing (only when pushing source)
                if (getOpts().getCopyTrans()
                        && (getOpts().getPushType() == PushPullType.Both || getOpts()
                                .getPushType() == PushPullType.Source)) {
                    this.copyTransForDocument(qualifiedDocName);
                }
            } catch (Exception e) {
                String message =
                        "Operation failed: " + e.getMessage() + "\n\n"
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
                        getOpts().buildFromDocArgument(
                                qualifiedDocName(localDocName))
                                + "\n\n.";
                log.error(message);
                if (e instanceof UniformInterfaceException) {
                    String entity =
                            ((UniformInterfaceException) e).getResponse()
                                    .getEntity(String.class);

                    throw new RuntimeException(String.format(
                            "%n * Error Message: %s;%n * Response From Server: %s]",
                            e.getMessage(), entity));
                }
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        deleteSourceDocsFromServer(obsoleteDocs);
    }

    private static void stripUntranslatedEntriesIfMergeTypeIsNotImport(
            PushOptions opts, TranslationsResource translationResources) {
        String mergeType = opts.getMergeType();
        if (!MergeType.IMPORT.name().equalsIgnoreCase(mergeType)) {
            List<TextFlowTarget> originalTargets =
                    translationResources.getTextFlowTargets();
            final Predicate<String> blankStringPredicate =
                    new Predicate<String>() {
                        @Override
                        public boolean apply(String input) {
                            return Strings.isNullOrEmpty(input);
                        }
                    };

            Collection<TextFlowTarget> untranslatedEntries =
                    filter(originalTargets,
                            new Predicate<TextFlowTarget>() {
                                @Override
                                public boolean apply(
                                        TextFlowTarget input) {
                                    // it's unsafe to rely on content state (plural entries)
                                    return input == null
                                            || input.getContents().isEmpty()
                                            || all(input.getContents(),
                                                    blankStringPredicate);
                                }
                            });
            log.debug(
                    "Remove {} untranslated entries from the payload since merge type is NOT import ({})",
                    untranslatedEntries.size(), mergeType);
            translationResources.getTextFlowTargets()
                    .removeAll(untranslatedEntries);
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
        SortedSet<String> docsToPush;
        if (!docNames.contains(fromDoc)) {
            throw new RuntimeException(
                    "Document with id "
                            + fromDoc
                            + " not found, unable to start push from unknown document. Aborting.");
        }
        docsToPush = docNames.tailSet(fromDoc);
        int numSkippedDocs = docNames.size() - docsToPush.size();
        log.info("Skipping {} document(s) before {}.", numSkippedDocs, fromDoc);
        return docsToPush;
    }

    /**
     * Returns obsolete docs which belong to the current module. Returns any
     * docs in the current module from the server, unless they are found in the
     * localDocNames set.
     *
     * @param localDocNames
     */
    private List<String> getObsoleteDocsInModuleFromServer(
            Set<String> localDocNames) {
        List<String> qualifiedDocNames =
                getQualifiedDocNamesForCurrentModuleFromServer();
        List<String> obsoleteDocs =
                new ArrayList<String>(qualifiedDocNames.size());
        for (String qualifiedDocName : qualifiedDocNames) {
            String unqualifiedDocName = unqualifiedDocName(qualifiedDocName);
            if (!localDocNames.contains(unqualifiedDocName)) {
                obsoleteDocs.add(qualifiedDocName);
            }
        }
        return obsoleteDocs;
    }

    /**
     * @param qualifiedDocNames
     */
    private void deleteSourceDocsFromServer(List<String> qualifiedDocNames) {
        for (String qualifiedDocName : qualifiedDocNames) {
            deleteSourceDocFromServer(qualifiedDocName);
        }
    }

    private void pushSrcDocToServer(final String docUri, final Resource srcDoc,
            final StringSet extensions) {
        if (!getOpts().isDryRun()) {
            log.info("pushing source doc [name={} size={}] to server",
                    srcDoc.getName(), srcDoc.getTextFlows().size());

            ConsoleUtils.startProgressFeedback();
            // NB: Copy trans is set to false as using copy trans in this manner
            // is deprecated.
            // see PushCommand.copyTransForDocument
            ProcessStatus status =
                    asyncProcessClient.startSourceDocCreationOrUpdate(docUri,
                            getOpts().getProj(), getOpts().getProjectVersion(),
                            srcDoc, extensions, false);

            boolean waitForCompletion = true;

            while (waitForCompletion) {
                switch (status.getStatusCode()) {
                case Failed:
                    throw new RuntimeException(
                            "Failed while pushing document: "
                                    + status.getMessages());

                case Finished:
                    waitForCompletion = false;
                    break;

                case Running:
                    ConsoleUtils.setProgressFeedbackMessage("Pushing ...");
                    break;

                case Waiting:
                    ConsoleUtils
                            .setProgressFeedbackMessage("Waiting to start ...");
                    break;

                case NotAccepted:
                    // try to submit the process again
                    status =
                            asyncProcessClient
                                    .startSourceDocCreationOrUpdate(docUri,
                                            getOpts().getProj(), getOpts()
                                                    .getProjectVersion(),
                                            srcDoc, extensions, false);
                    ConsoleUtils
                            .setProgressFeedbackMessage("Waiting for other clients ...");
                    break;
                }

                // Wait before retrying
                wait(POLL_PERIOD);
                status = asyncProcessClient.getProcessStatus(status.getUrl());
            }

            ConsoleUtils.endProgressFeedback();
        } else {
            log.info(
                    "pushing source doc [name={} size={}] to server (skipped due to dry run)",
                    srcDoc.getName(), srcDoc.getTextFlows().size());
        }
    }

    /**
     * Split TranslationsResource into List&lt;TranslationsResource&gt;
     * according to maxBatchSize, but only if mergeType=AUTO
     *
     * @param doc
     * @param maxBatchSize
     * @return list of TranslationsResource, each containing up to maxBatchSize
     *         TextFlowTargets
     */
    public List<TranslationsResource> splitIntoBatch(TranslationsResource doc,
            int maxBatchSize) {
        List<TranslationsResource> targetDocList =
                new ArrayList<TranslationsResource>();
        int numTargets = doc.getTextFlowTargets().size();

        if (numTargets > maxBatchSize && mergeAuto()) {
            int numBatches = numTargets / maxBatchSize;

            if (numTargets % maxBatchSize != 0) {
                ++numBatches;
            }

            int fromIndex = 0;
            int toIndex;

            for (int i = 1; i <= numBatches; i++) {
                // make a dummy TranslationsResource to hold just the
                // TextFlowTargets for each batch
                TranslationsResource resource = new TranslationsResource();
                resource.setExtensions(doc.getExtensions());
                resource.setLinks(doc.getLinks());
                resource.setRevision(doc.getRevision());

                if ((i * maxBatchSize) > numTargets) {
                    toIndex = numTargets;
                } else {
                    toIndex = i * maxBatchSize;
                }

                resource.getTextFlowTargets().addAll(
                        doc.getTextFlowTargets().subList(fromIndex, toIndex));

                fromIndex = i * maxBatchSize;

                targetDocList.add(resource);
            }
        } else {
            targetDocList.add(doc);
        }
        return targetDocList;
    }

    private void pushTargetDocToServer(final String docUri,
            LocaleMapping locale, final String localDocName,
            TranslationsResource targetDoc, final StringSet extensions) {
        if (!getOpts().isDryRun()) {
            log.info(
                    "Pushing target doc [name={} size={} client-locale={}] to server [locale={}]",
                    localDocName,
                    targetDoc.getTextFlowTargets().size(),
                    locale.getLocalLocale(), locale.getLocale());

            ConsoleUtils.startProgressFeedback();

            ProcessStatus status =
                    asyncProcessClient.startTranslatedDocCreationOrUpdate(
                            docUri, getOpts().getProj(), getOpts()
                                    .getProjectVersion(),
                            new LocaleId(locale.getLocale()), targetDoc,
                            extensions, getOpts().getMergeType(),
                            getOpts().isMyTrans());

            boolean waitForCompletion = true;

            while (waitForCompletion) {
                switch (status.getStatusCode()) {
                case Failed:
                    throw new RuntimeException(
                            "Failed while pushing document translations: "
                                    + status.getMessages());

                case Finished:
                    waitForCompletion = false;
                    break;

                case Running:
                    ConsoleUtils.setProgressFeedbackMessage(status
                            .getPercentageComplete() + "%");
                    break;

                case Waiting:
                    ConsoleUtils
                            .setProgressFeedbackMessage("Waiting to start ...");
                    break;

                case NotAccepted:
                    // try to submit the process again
                    status =
                            asyncProcessClient
                                    .startTranslatedDocCreationOrUpdate(docUri,
                                            getOpts().getProj(), getOpts()
                                                    .getProjectVersion(),
                                            new LocaleId(locale.getLocale()),
                                            targetDoc, extensions,
                                            getOpts().getMergeType(),
                                            getOpts().isMyTrans());
                    ConsoleUtils
                            .setProgressFeedbackMessage("Waiting for other clients ...");
                    break;
                }

                // Wait before retrying
                wait(POLL_PERIOD);
                status = asyncProcessClient.getProcessStatus(status.getUrl());
            }
            ConsoleUtils.endProgressFeedback();

            // Show warning messages
            if (status.getMessages().size() > 0) {
                log.warn("Pushed translations with warnings:");
                for (String mssg : status.getMessages()) {
                    log.warn(mssg);
                }
            }
        } else {
            log.info(
                    "pushing target doc [name={} size={} client-locale={}] to server [locale={}] (skipped due to dry run)",
                    localDocName, targetDoc.getTextFlowTargets().size(),
                    locale.getLocalLocale(), locale.getLocale());
        }
    }

    private void deleteSourceDocFromServer(String qualifiedDocName) {
        if (!getOpts().isDryRun()) {
            log.info("deleting resource {} from server", qualifiedDocName);
            String docUri = RestUtil.convertToDocumentURIId(qualifiedDocName);
            sourceDocResourceClient.deleteResource(docUri);
        } else {
            log.info(
                    "deleting resource {} from server (skipped due to dry run)",
                    qualifiedDocName);
        }
    }

    private void copyTransForDocument(String docName) {
        if (getOpts().isDryRun()) {
            log.info("Skipping Copy Trans for " + docName + " (due to dry run)");
            return;
        }
        log.info("Running Copy Trans for " + docName);
        try {
            this.copyTransClient.startCopyTrans(getOpts().getProj(),
                    getOpts().getProjectVersion(), docName);
        } catch (Exception ex) {
            log.warn("Could not start Copy Trans for above document. Proceeding");
            return;
        }
        CopyTransStatus copyTransStatus;

        try {
            copyTransStatus =
                    this.copyTransClient.getCopyTransStatus(getOpts()
                            .getProj(), getOpts().getProjectVersion(), docName);
        } catch (UniformInterfaceException failure) {
            // 404 - Probably because of an old server
            if (failure.getResponse().getClientResponseStatus() == ClientResponse.Status.NOT_FOUND) {
                if (getClientFactory()
                        .compareToServerVersion("1.8.0-SNAPSHOT") < 0) {
                    log.warn("Copy Trans not started (Incompatible server version.)");
                    return;
                } else {
                    throw new RuntimeException(
                            "Could not invoke copy trans. The service was not available (404)");
                }
            } else if (failure.getCause() != null) {
                throw new RuntimeException("Problem invoking copy trans.",
                        failure.getCause());
            } else {
                throw new RuntimeException(
                        "Problem invoking copy trans: [Server response code:"
                                + failure.getResponse().getStatus() + "]");
            }
        }
        ConsoleUtils.startProgressFeedback();

        while (copyTransStatus.isInProgress()) {
            try {
                Thread.sleep(POLL_PERIOD);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for Copy Trans to finish.");
            }
            ConsoleUtils.setProgressFeedbackMessage(copyTransStatus
                    .getPercentageComplete() + "%");
            copyTransStatus =
                    this.copyTransClient.getCopyTransStatus(getOpts()
                            .getProj(), getOpts().getProjectVersion(), docName);
        }
        ConsoleUtils.endProgressFeedback();

        if (copyTransStatus.getPercentageComplete() < 100) {
            log.warn("Copy Trans for the above document stopped unexpectedly.");
        }
    }

    // TODO Perhaps move this to ConsoleUtils
    private static void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting");
        }
    }

}
