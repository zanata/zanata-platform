/*
 *
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
package org.zanata.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.validation.ConstraintViolationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.zanata.dao.WebHookDAO;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.exception.AuthorizationException;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.VirusDetectedException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.i18n.Messages;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.model.WebHook;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.model.type.WebhookType;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.VirusScanner;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionStateCache;
import org.zanata.service.impl.WebhookServiceImpl;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.CopyAction;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.DateUtil;
import org.zanata.util.FileUtil;
import org.zanata.util.PasswordUtil;
import org.zanata.util.ServiceLocator;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.util.TokenUtil;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Named("versionHomeAction")
@ViewScoped
@Model
@Transactional
public class VersionHomeAction extends AbstractSortAction
        implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionHomeAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private CopyVersionManager copyVersionManager;
    @Inject
    private MergeTranslationsManager mergeTranslationsManager;
    @Inject
    private CopyTransManager copyTransManager;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    @Inject
    private Messages msgs;
    @Inject
    private DocumentService documentServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private TranslationFileService translationFileServiceImpl;
    @Inject
    private VirusScanner virusScanner;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private TranslationService translationServiceImpl;
    private String versionSlug;
    private String projectSlug;
    private boolean pageRendered = false;
    private WordStatistic overallStatistic;
    private HLocale selectedLocale;
    private HDocument selectedDocument;
    @Inject
    private ConversationScopeMessages conversationScopeMessages;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    private WebhookServiceImpl webhookService;
    @Inject
    private WebHookDAO webHookDAO;
    private List<HLocale> supportedLocale;
    private List<HDocument> documents;
    private Map<LocaleId, WordStatistic> localeStatisticMap;
    private Map<DocumentLocaleKey, WordStatistic> documentStatisticMap;
    private List<HIterationGroup> groups;
    private HProjectIteration version;
    private SourceFileUploadHelper sourceFileUpload =
            new SourceFileUploadHelper();
    private TranslationFileUploadHelper translationFileUpload =
            new TranslationFileUploadHelper();
    private SortingType documentSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS,
                    SortingType.SortOption.LAST_SOURCE_UPDATE,
                    SortingType.SortOption.LAST_TRANSLATED));
    private SortingType sourceDocumentSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS,
                    SortingType.SortOption.LAST_SOURCE_UPDATE));
    private SortingType settingsDocumentSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.LAST_SOURCE_UPDATE));
    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());
    private final DocumentComparator documentComparator =
            new DocumentComparator(getDocumentSortingList());
    private final DocumentComparator sourceDocumentComparator =
            new DocumentComparator(getSourceDocumentSortingList());
    private final DocumentComparator settingsDocumentComparator =
            new DocumentComparator(getSettingsDocumentSortingList());
    private final SourceDocumentFilter documentsTabDocumentFilter =
            new SourceDocumentFilter();
    private final DocumentFilter settingsTabDocumentFilter =
            new DocumentFilter();
    private final DocumentFilter languageTabDocumentFilter =
            new DocumentFilter();
    @Inject
    private CopyVersionHandler copyVersionHandler;
    private final AbstractListFilter<HIterationGroup> groupFilter =
            new InMemoryListFilter<HIterationGroup>() {

                @Override
                protected List<HIterationGroup> fetchAll() {
                    return getGroups();
                }

                @Override
                protected boolean include(HIterationGroup elem, String filter) {
                    return StringUtils.containsIgnoreCase(elem.getName(),
                            filter)
                            || StringUtils.containsIgnoreCase(elem.getSlug(),
                                    filter);
                }
            };
    private final AbstractListFilter<HLocale> languageTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {

                @Override
                protected List<HLocale> fetchAll() {
                    return getSupportedLocale();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(
                            elem.getLocaleId().getId(), filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.retrieveDisplayName(), filter);
                }
            };
    private final AbstractListFilter<HLocale> documentsTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {

                @Override
                protected List<HLocale> fetchAll() {
                    return getSupportedLocale();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(
                            elem.getLocaleId().getId(), filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.retrieveDisplayName(), filter);
                }
            };
    private List<WebHook> manualWebhooks;

    public void setVersionSlug(String versionSlug) {
        this.versionSlug = versionSlug;
        copyVersionHandler.setVersionSlug(versionSlug);
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
        copyVersionHandler.setProjectSlug(projectSlug);
    }

    public void cancelCopyVersion() {
        copyVersionManager.cancelCopyVersion(projectSlug, versionSlug);
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.copyVersion.Cancelled", versionSlug));
    }

    private List<WebHook> getManualWebhooks() {
        if (manualWebhooks == null) {
            manualWebhooks = webHookDAO.getWebHooksForType(projectSlug,
                    WebhookType.ManuallyTriggeredEvent);
        }
        return manualWebhooks;
    }

    public boolean canTriggerManualWebhook() {
        boolean hasLocalePermission =
                isUserAllowedToTranslateOrReview(selectedLocale);
        return hasLocalePermission && !getManualWebhooks().isEmpty();
    }

    public void triggerManualWebhookEvent() {
        List<WebHook> manualWebhooks = getManualWebhooks();
        if (selectedLocale != null && !manualWebhooks.isEmpty()) {
            webhookService.processManualEvent(projectSlug, versionSlug,
                    selectedLocale.getLocaleId(), manualWebhooks);
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.iteration.manualWebhook.triggered"));
        }
    }
    // TODO Serializable only because it's a dependent bean

    public static class CopyVersionHandler extends CopyAction
            implements Serializable {
        private String projectSlug;
        private String versionSlug;
        @Inject
        private Messages messages;
        @Inject
        private CopyVersionManager copyVersionManager;
        @Inject
        private FacesMessages facesMessages;

        @Override
        public boolean isInProgress() {
            return copyVersionManager.isCopyVersionRunning(projectSlug,
                    versionSlug);
        }

        @Override
        public String getProgressMessage() {
            return messages.format("jsf.copyVersion.processedDocuments",
                    getProcessedDocuments(), getTotalDocuments());
        }

        @Override
        public void onComplete() {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    messages.format("jsf.copyVersion.Completed", versionSlug));
        }

        public int getProcessedDocuments() {
            CopyVersionTaskHandle handle = getHandle();
            if (handle != null) {
                return handle.getDocumentCopied();
            }
            return 0;
        }

        public int getTotalDocuments() {
            CopyVersionTaskHandle handle = getHandle();
            if (handle != null) {
                return handle.getTotalDoc();
            }
            return 0;
        }

        protected CopyVersionTaskHandle getHandle() {
            return copyVersionManager.getCopyVersionProcessHandle(projectSlug,
                    versionSlug);
        }

        public CopyVersionHandler() {
        }

        public void setProjectSlug(final String projectSlug) {
            this.projectSlug = projectSlug;
        }

        public void setVersionSlug(final String versionSlug) {
            this.versionSlug = versionSlug;
        }
    }

    /**
     * Sort language list based on locale statistic
     */
    public void sortLanguageList() {
        languageComparator.setSelectedDocumentId(null);
        Collections.sort(getSupportedLocale(), languageComparator);
        languageTabLanguageFilter.reset();
    }

    /**
     * Sort language list based on selected document
     */
    public void sortLanguageList(Long documentId) {
        languageComparator.setSelectedDocumentId(documentId);
        Collections.sort(getSupportedLocale(), languageComparator);
        documentsTabLanguageFilter.reset();
    }

    /**
     * Sort document list based on selected locale
     *
     * @param localeId
     */
    public void sortDocumentList(LocaleId localeId) {
        documentComparator.setSelectedLocaleId(localeId);
        Collections.sort(getDocuments(), documentComparator);
        languageTabDocumentFilter.reset();
    }

    public void sortSourceDocumentList() {
        sourceDocumentComparator.setSelectedLocaleId(null);
        Collections.sort(getSourceDocuments(), sourceDocumentComparator);
        documentsTabDocumentFilter.reset();
    }

    public void sortSettingsDocumentList() {
        settingsDocumentComparator.setSelectedLocaleId(null);
        Collections.sort(getDocuments(), settingsDocumentComparator);
        settingsTabDocumentFilter.reset();
    }

    @Override
    public void resetPageData() {
        languageTabDocumentFilter.reset();
        documentsTabDocumentFilter.reset();
        settingsTabDocumentFilter.reset();
        languageTabLanguageFilter.reset();
        documents = null;
        version = null;
        supportedLocale = null;
        manualWebhooks = null;
        loadStatistics();
    }

    @Override
    protected void loadStatistics() {
        localeStatisticMap = Maps.newHashMap();
        for (HLocale locale : getSupportedLocale()) {
            WordStatistic wordStatistic =
                    versionStateCacheImpl.getVersionStatistics(
                            getVersion().getId(), locale.getLocaleId());
            wordStatistic.setRemainingHours(
                    StatisticsUtil.getRemainingHours(wordStatistic));
            localeStatisticMap.put(locale.getLocaleId(), wordStatistic);
        }
        overallStatistic = new WordStatistic();
        for (Map.Entry<LocaleId, WordStatistic> entry : localeStatisticMap
                .entrySet()) {
            overallStatistic.add(entry.getValue());
        }
        overallStatistic.setRemainingHours(
                StatisticsUtil.getRemainingHours(overallStatistic));
        documentStatisticMap = Maps.newHashMap();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return msgs.formatWithAnyArgs(key, args);
    }

    public List<HLocale> getSupportedLocale() {
        if (supportedLocale == null) {
            supportedLocale =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                            projectSlug, versionSlug);
            Collections.sort(supportedLocale, languageComparator);
        }
        return supportedLocale;
    }

    public int getVersionSupportedLocaleCount(String projectSlug,
            String versionSlug) {
        return localeServiceImpl.getSupportedLanguageByProjectIteration(
                projectSlug, versionSlug).size();
    }

    public List<HDocument> getDocuments() {
        if (documents == null) {
            documents = documentDAO.getByProjectIteration(projectSlug,
                    versionSlug, false);
            Collections.sort(documents, documentComparator);
        }
        return documents;
    }

    public List<HDocument> getDocuments(DocumentDAO documentDAO) {
        if (this.documentDAO == null) {
            this.documentDAO = documentDAO;
        }
        return getDocuments();
    }

    public List<HDocument> getSourceDocuments() {
        if (documents == null) {
            documents = documentDAO.getByProjectIteration(projectSlug,
                    versionSlug, false);
            Collections.sort(documents, sourceDocumentComparator);
        }
        return documents;
    }

    public List<HDocument> getSourceDocuments(DocumentDAO documentDAO) {
        if (this.documentDAO == null) {
            this.documentDAO = documentDAO;
        }
        return getSourceDocuments();
    }

    public List<HIterationGroup> getGroups() {
        if (groups == null) {
            HProjectIteration version = getVersion();
            if (version != null) {
                groups = Lists.newArrayList(version.getGroups());
            }
        }
        return groups;
    }

    public HProjectIteration getVersion() {
        if (version == null) {
            version = projectIterationDAO.getBySlug(projectSlug, versionSlug);
        }
        return version;
    }

    public void setPageRendered(boolean pageRendered) {
        if (pageRendered) {
            loadStatistics();
        }
        this.pageRendered = pageRendered;
    }

    public WordStatistic getStatisticsForLocale(LocaleId localeId) {
        return localeStatisticMap.get(localeId);
    }

    public WordStatistic getStatisticForDocument(Long documentId,
            LocaleId localeId) {
        DocumentLocaleKey key = new DocumentLocaleKey(documentId, localeId);
        if (!documentStatisticMap.containsKey(key)) {
            WordStatistic wordStatistic = translationStateCacheImpl
                    .getDocumentStatistics(documentId, localeId);
            wordStatistic.setRemainingHours(
                    StatisticsUtil.getRemainingHours(wordStatistic));
            documentStatisticMap.put(key, wordStatistic);
        }
        return documentStatisticMap.get(key);
    }

    public WordStatistic getDocumentStatistic(Long documentId) {
        WordStatistic wordStatistic = new WordStatistic();
        for (HLocale locale : getSupportedLocale()) {
            WordStatistic statistic =
                    getStatisticForDocument(documentId, locale.getLocaleId());
            wordStatistic.add(statistic);
        }
        wordStatistic.setRemainingHours(
                StatisticsUtil.getRemainingHours(wordStatistic));
        return wordStatistic;
    }

    public DisplayUnit getStatisticFigureForDocument(
            SortingType.SortOption sortOption, LocaleId localeId,
            HDocument document) {
        WordStatistic statistic =
                getStatisticForDocument(document.getId(), localeId);
        Date date = null;
        if (sortOption.equals(SortingType.SortOption.LAST_SOURCE_UPDATE)
                || sortOption.equals(SortingType.SortOption.LAST_TRANSLATED)) {
            if (sortOption.equals(SortingType.SortOption.LAST_SOURCE_UPDATE)) {
                date = document.getLastChanged();
            } else {
                DocumentStatus docStat = translationStateCacheImpl
                        .getDocumentStatus(document.getId(), localeId);
                date = docStat.getLastTranslatedDate();
            }
        }
        return getDisplayUnit(sortOption, statistic, date);
    }

    public DisplayUnit getStatisticFigureForDocument(
            SortingType.SortOption sortOption, HDocument document) {
        WordStatistic statistic = getDocumentStatistic(document.getId());
        return getDisplayUnit(sortOption, statistic, document.getLastChanged());
    }

    public DisplayUnit getStatisticFigureForLocale(
            SortingType.SortOption sortOption, LocaleId localeId) {
        WordStatistic statistic = getStatisticsForLocale(localeId);
        return getDisplayUnit(sortOption, statistic, null);
    }

    public boolean isUserAllowedToTranslateOrReview(HLocale hLocale) {
        return isVersionActive() && identity != null
                && (identity.hasPermissionWithAnyTargets("add-translation",
                        getVersion().getProject(), hLocale)
                        || identity.hasPermissionWithAnyTargets(
                                "translation-review", getVersion().getProject(),
                                hLocale));
    }

    private boolean isVersionActive() {
        return getVersion().getProject().getStatus() == EntityStatus.ACTIVE
                || getVersion().getStatus() == EntityStatus.ACTIVE;
    }

    public void deleteDocument(String docId) {
        checkDocumentRemovalAllowed();
        HDocument doc = documentDAO.getById(Long.valueOf(docId));
        documentServiceImpl.makeObsolete(doc);
        resetPageData();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                doc.getDocId() + " has been removed.");
    }

    public List<HLocale> getAvailableSourceLocales() {
        return localeDAO.findAllActive();
    }

    public String getLastUpdatedDescription(HDocument document) {
        return DateUtil.getHowLongAgoDescription(document.getLastChanged());
    }

    public String getFormattedDate(HDocument hdoc) {
        return DateUtil.formatShortDate(hdoc.getLastChanged());
    }

    public void checkDocumentRemovalAllowed() {
        if (!isDocumentRemovalAllowed()) {
            throw new AuthorizationException(
                    "Current user is not allowed to delete document");
        }
    }

    public boolean isDocumentRemovalAllowed() {
        // currently same permissions as uploading a document
        return this.isDocumentUploadAllowed();
    }

    public boolean isDocumentUploadAllowed() {
        return isVersionActive() && identity != null && identity
                .hasPermissionWithAnyTargets("import-template", getVersion());
    }

    public boolean isZipFileDownloadAllowed() {
        return getVersion().getProjectType() != null && identity
                .hasPermissionWithAnyTargets("download-all", getVersion());
    }

    public boolean isPoProject() {
        HProjectIteration projectIteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        ProjectType type = projectIteration.getProjectType();
        if (type == null) {
            type = projectIteration.getProject().getDefaultProjectType();
        }
        return type == ProjectType.Gettext || type == ProjectType.Podir;
    }

    public String getZipFileDownloadTitle() {
        String message = null;
        if (!isZipFileDownloadAllowed()) {
            if (getVersion().getProjectType() == null) {
                message = msgs.get(
                        "jsf.iteration.files.DownloadAllFiles.ProjectTypeNotSet");
            } else if (getVersion().getProjectType() != ProjectType.Gettext
                    && getVersion().getProjectType() != ProjectType.Podir) {
                message = msgs.get(
                        "jsf.iteration.files.DownloadAllFiles.ProjectTypeNotAllowed");
            }
        } else {
            message = msgs.get("jsf.iteration.files.DownloadAll");
        }
        return message;
    }

    public boolean isKnownProjectType() {
        ProjectType type = projectIterationDAO
                .getBySlug(projectSlug, versionSlug).getProjectType();
        return type != null;
    }

    public boolean isFileUploadAllowed(HLocale hLocale) {
        return isVersionActive() && identity != null
                && identity.hasPermissionWithAnyTargets("modify-translation",
                        getVersion().getProject(), hLocale);
    }

    private Optional<DocumentType> getDocumentType(Optional<String> docType) {
        DocumentType type;
        if (!docType.isPresent()) {
            // if docType null, only 1 document type available for selected file
            // extensions
            Set<DocumentType> documentTypes = translationFileServiceImpl
                    .getDocumentTypes(sourceFileUpload.getFileName());
            if (documentTypes.isEmpty()) {
                return Optional.absent();
            }
            type = documentTypes.iterator().next();
        } else {
            // if docType not null, adapter is selected by user from drop down
            type = DocumentType.getByName(docType.get());
        }
        return Optional.of(type);
    }

    public String uploadSourceFile() {
        identity.checkPermission("import-template", getVersion());
        if (sourceFileUpload.getFileName().endsWith(".pot")) {
            uploadPotFile();
        } else {
            Optional<String> docType =
                    Optional.fromNullable(sourceFileUpload.documentType);
            Optional<DocumentType> documentTypeOpt = getDocumentType(docType);
            if (documentTypeOpt.isPresent() && translationFileServiceImpl
                    .hasAdapterFor(documentTypeOpt.get())) {
                uploadAdapterFile(documentTypeOpt.get());
                resetPageData();
            } else {
                String summary = "Unrecognized file extension for "
                        + sourceFileUpload.getFileName();
                // TODO this message is not displayed
                conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                        summary);
                throw new IllegalArgumentException(summary);
            }
        }
        return "success";
    }

    public boolean isPoDocument(String docId) {
        return translationFileServiceImpl.isPoDocument(projectSlug, versionSlug,
                docId);
    }

    public String sourceExtensionOf(String docPath, String docName) {
        return "." + translationFileServiceImpl.getSourceFileExtension(
                projectSlug, versionSlug, docPath, docName);
    }

    public String translationExtensionOf(String docPath, String docName) {
        return "." + translationFileServiceImpl.getTranslationFileExtension(
                projectSlug, versionSlug, docPath, docName);
    }

    public boolean hasOriginal(String docPath, String docName) {
        GlobalDocumentId id = new GlobalDocumentId(projectSlug, versionSlug,
                docPath + docName);
        return filePersistService.hasPersistedDocument(id);
    }

    private void showUploadSuccessMessage() {
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                "Document " + sourceFileUpload.getFileName() + " uploaded.");
    }

    /**
     * <p>
     * Upload a pot file. File may be new or overwriting an existing file.
     * </p>
     * <p/>
     * <p>
     * If there is an existing file that is not a pot file, the pot file will be
     * parsed using msgctxt as Zanata id, otherwise id will be generated from a
     * hash of msgctxt and msgid.
     * </p>
     */
    private void uploadPotFile() {
        String docId = sourceFileUpload.getDocId();
        if (docId == null) {
            docId = FileUtil.generateDocId(sourceFileUpload.getDocumentPath(),
                    sourceFileUpload.getFileName());
        }
        HDocument existingDoc = documentDAO
                .getByProjectIterationAndDocId(projectSlug, versionSlug, docId);
        boolean docExists = existingDoc != null;
        boolean useOfflinePo = docExists && !isPoDocument(docId);
        try {
            Resource doc = translationFileServiceImpl.parseUpdatedPotFile(
                    sourceFileUpload.getFileContents(), docId,
                    sourceFileUpload.getFileName(), useOfflinePo);
            doc.setLang(new LocaleId(sourceFileUpload.getSourceLang()));
            // TODO Copy Trans values
            StringSet extensions =
                    new StringSet(ExtensionType.GetText.toString());
            extensions.add(SimpleComment.ID);
            documentServiceImpl.saveDocument(projectSlug, versionSlug, doc,
                    extensions, false);
            showUploadSuccessMessage();
        } catch (ZanataServiceException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage() + "-" + sourceFileUpload.getFileName());
        } catch (ConstraintViolationException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid arguments");
        }
    }

    private Optional<String> getOptionalParams() {
        return Optional.fromNullable(
                Strings.emptyToNull(sourceFileUpload.getAdapterParams()));
    }

    public void setSelectedLocaleId(String localeId) {
        this.selectedLocale = localeDAO.findByLocaleId(new LocaleId(localeId));
    }

    public void setSelectedDocumentId(String projectSlug, String versionSlug,
            String docId) {
        docId = UrlUtil.decodeString(docId);
        this.selectedDocument = documentDAO
                .getByProjectIterationAndDocId(projectSlug, versionSlug, docId);
    }
    // TODO add logging for disk writing errors
    // TODO damason: unify this with Source/TranslationDocumentUpload

    private void uploadAdapterFile(DocumentType docType) {
        String fileName = sourceFileUpload.getFileName();
        String docId = sourceFileUpload.getDocId();
        String documentPath = "";
        if (docId == null) {
            documentPath = sourceFileUpload.getDocumentPath();
        } else if (docId.contains("/")) {
            documentPath = docId.substring(0, docId.lastIndexOf('/'));
        }
        File tempFile = null;
        byte[] md5hash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileContents = new DigestInputStream(
                    sourceFileUpload.getFileContents(), md);
            tempFile =
                    translationFileServiceImpl.persistToTempFile(fileContents);
            md5hash = md.digest();
        } catch (ZanataServiceException e) {
            VersionHomeAction.log.error(
                    "Failed writing temp file for document {}", e,
                    sourceFileUpload.getDocId());
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    "Error saving uploaded document " + fileName
                            + " to server.");
            return;
        } catch (NoSuchAlgorithmException e) {
            VersionHomeAction.log.error("MD5 hash algorithm not available", e);
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    "Error generating hash for uploaded document " + fileName
                            + ".");
            return;
        }
        HDocument document = null;
        try {
            Resource doc;
            if (docId == null) {
                doc = translationFileServiceImpl.parseAdapterDocumentFile(
                        tempFile.toURI(), documentPath, fileName,
                        getOptionalParams(), Optional.of(docType.name()));
            } else {
                doc = translationFileServiceImpl
                        .parseUpdatedAdapterDocumentFile(tempFile.toURI(),
                                docId, fileName, getOptionalParams(),
                                Optional.of(docType.name()));
            }
            doc.setLang(new LocaleId(sourceFileUpload.getSourceLang()));
            Set<String> extensions = Collections.<String> emptySet();
            // TODO Copy Trans values
            document = documentServiceImpl.saveDocument(projectSlug,
                    versionSlug, doc, extensions, false);
            showUploadSuccessMessage();
        } catch (SecurityException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    "Error reading uploaded document " + fileName
                            + " on server.");
        } catch (ZanataServiceException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid document format for " + fileName);
        }
        if (document == null) {
            // error message for failed parse already added.
        } else {
            HRawDocument rawDocument = new HRawDocument();
            rawDocument.setDocument(document);
            rawDocument.setContentHash(
                    new String(PasswordUtil.encodeHex(md5hash)));
            rawDocument.setType(docType);
            rawDocument.setUploadedBy(identity.getCredentials().getUsername());
            Optional<String> params = getOptionalParams();
            if (params.isPresent()) {
                rawDocument.setAdapterParameters(params.get());
            }
            try {
                String name = projectSlug + ":" + versionSlug + ":" + docId;
                virusScanner.scan(tempFile, name);
            } catch (VirusDetectedException e) {
                VersionHomeAction.log.warn("File failed virus scan: {}",
                        e.getMessage());
                conversationScopeMessages.setMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "uploaded file did not pass virus scan");
            }
            filePersistService.persistRawDocumentContentFromFile(rawDocument,
                    tempFile, FilenameUtils.getExtension(fileName));
            documentDAO.addRawDocument(document, rawDocument);
            documentDAO.flush();
        }
        translationFileServiceImpl.removeTempFile(tempFile);
    }
    // Check if copy-trans, copy version or merge-trans is running for given
    // version

    public boolean isCopyActionsRunning() {
        return mergeTranslationsManager.isRunning(projectSlug, versionSlug)
                || copyVersionManager.isCopyVersionRunning(projectSlug,
                        versionSlug)
                || copyTransManager.isCopyTransRunning(getVersion());
    }

    public boolean needDocumentTypeSelection(String fileName) {
        return translationFileServiceImpl.hasMultipleDocumentTypes(fileName);
    }

    public List<DocumentType> getDocumentTypes(String fileName) {
        return Lists.newArrayList(
                translationFileServiceImpl.getDocumentTypes(fileName));
    }

    public void setDefaultTranslationDocType(String fileName) {
        translationFileUpload.setDocumentType(null);
    }

    public String getEditorUrl(String sourceLocale, String docId) {
        return urlUtil.editorDocumentUrl(projectSlug, versionSlug,
                selectedLocale.getLocaleId(),
                LocaleId.fromJavaName(sourceLocale), TokenUtil.encode(docId));
    }

    public String encodeDocId(String docId) {
        return urlUtil.encodeString(docId);
    }

    public String uploadTranslationFile(HLocale hLocale) {
        identity.checkPermission("modify-translation", hLocale,
                getVersion().getProject());
        try {
            // process the file
            String fileName = translationFileUpload.fileName;
            if (!needDocumentTypeSelection(fileName)) {
                // Get documentType for this file name
                DocumentType documentType = getDocumentTypes(fileName).get(0);
                translationFileUpload.setDocumentType(documentType.name());
            }
            Optional<String> docType =
                    Optional.fromNullable(translationFileUpload.documentType);
            TranslationsResource transRes =
                    translationFileServiceImpl.parseTranslationFile(
                            translationFileUpload.getFileContents(),
                            translationFileUpload.getFileName(),
                            hLocale.getLocaleId().getId(), projectSlug,
                            versionSlug, translationFileUpload.docId, docType);
            // translate it
            Set<String> extensions;
            if (translationFileUpload.getFileName().endsWith(".po")) {
                extensions = new StringSet(ExtensionType.GetText.toString());
            } else {
                extensions = Collections.<String> emptySet();
            }
            List<String> warnings = translationServiceImpl.translateAllInDoc(
                    projectSlug, versionSlug, translationFileUpload.getDocId(),
                    hLocale.getLocaleId(), transRes, extensions,
                    translationFileUpload.isMergeTranslations() ? MergeType.AUTO
                            : MergeType.IMPORT,
                    translationFileUpload.isAssignCreditToUploader(),
                    TranslationSourceType.WEB_UPLOAD);
            StringBuilder infoMsg = new StringBuilder("File ")
                    .append(translationFileUpload.getFileName())
                    .append(" uploaded.");
            if (!warnings.isEmpty()) {
                infoMsg.append(" There were some warnings, see below.");
            }
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    infoMsg.toString());
            if (!warnings.isEmpty()) {
                List<FacesMessage> warningMessages = Lists.newArrayList();
                for (String warning : warnings) {
                    warningMessages.add(new FacesMessage(
                            FacesMessage.SEVERITY_WARN, warning, null));
                }
                conversationScopeMessages.addMessages(warningMessages);
            }
        } catch (ZanataServiceException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    translationFileUpload.getFileName() + "-" + e.getMessage());
        }
        resetPageData();
        return "success";
    }

    public void sourceFileUploaded(FileUploadEvent event) throws IOException {
        UploadedFile uploadedFile = event.getUploadedFile();
        sourceFileUpload.setFileName(uploadedFile.getName());
        sourceFileUpload.setFileContents(uploadedFile.getInputStream());
        // return "/iteration/view.xhtml?projectSlug=" + projectSlug
        // +"&iterationSlug=" + versionSlug;
    }

    public void clearSourceFileUpload() {
        sourceFileUpload = new SourceFileUploadHelper();
    }

    public void transFileUploaded(FileUploadEvent event) throws IOException {
        UploadedFile uploadedFile = event.getUploadedFile();
        translationFileUpload.setFileName(uploadedFile.getName());
        translationFileUpload.setFileContents(uploadedFile.getInputStream());
    }

    public void clearTransFileUpload() {
        translationFileUpload = new TranslationFileUploadHelper();
    }
    // TODO turn this into a CDI bean?

    private class DocumentFilter extends InMemoryListFilter<HDocument> {

        @Override
        protected List<HDocument> fetchAll() {
            return getDocuments(documentDAO);
        }

        @Override
        protected boolean include(HDocument elem, String filter) {
            return StringUtils.containsIgnoreCase(elem.getName(), filter)
                    || StringUtils.containsIgnoreCase(elem.getPath(), filter);
        }
    }
    // TODO turn this into a CDI bean?

    private class SourceDocumentFilter extends InMemoryListFilter<HDocument> {

        private DocumentDAO documentDAO =
                ServiceLocator.instance().getInstance(DocumentDAO.class);

        @Override
        protected List<HDocument> fetchAll() {
            return getSourceDocuments(documentDAO);
        }

        @Override
        protected boolean include(HDocument elem, String filter) {
            return StringUtils.containsIgnoreCase(elem.getName(), filter)
                    || StringUtils.containsIgnoreCase(elem.getPath(), filter);
        }
    }

    private class DocumentComparator implements Comparator<HDocument> {
        private SortingType sortingType;
        private LocaleId selectedLocaleId;

        public DocumentComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HDocument o1, HDocument o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                HDocument temp = o1;
                o1 = o2;
                o2 = temp;
            }
            if (selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_SOURCE_UPDATE)) {
                return DateUtil.compareDate(o1.getLastChanged(),
                        o2.getLastChanged());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_TRANSLATED)) {
                if (selectedLocaleId != null) {
                    DocumentStatus docStat1 = translationStateCacheImpl
                            .getDocumentStatus(o1.getId(), selectedLocaleId);
                    DocumentStatus docStat2 = translationStateCacheImpl
                            .getDocumentStatus(o2.getId(), selectedLocaleId);
                    return DateUtil.compareDate(
                            docStat1.getLastTranslatedDate(),
                            docStat2.getLastTranslatedDate());
                }
            } else {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedLocaleId != null) {
                    wordStatistic1 = getStatisticForDocument(o1.getId(),
                            selectedLocaleId);
                    wordStatistic2 = getStatisticForDocument(o2.getId(),
                            selectedLocaleId);
                } else {
                    wordStatistic1 = getDocumentStatistic(o1.getId());
                    wordStatistic2 = getDocumentStatistic(o2.getId());
                }
                return compareWordStatistic(wordStatistic1, wordStatistic2,
                        selectedSortOption);
            }
            return 0;
        }

        public void setSelectedLocaleId(final LocaleId selectedLocaleId) {
            this.selectedLocaleId = selectedLocaleId;
        }
    }

    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;
        private Long selectedDocumentId;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale o1, HLocale o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();
            if (!selectedSortOption.isAscending()) {
                HLocale temp = o1;
                o1 = o2;
                o2 = temp;
            }
            // Need to get statistic for comparison
            if (!selectedSortOption
                    .equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedDocumentId == null) {
                    wordStatistic1 = getStatisticsForLocale(o1.getLocaleId());
                    wordStatistic2 = getStatisticsForLocale(o2.getLocaleId());
                } else {
                    wordStatistic1 = getStatisticForDocument(selectedDocumentId,
                            o1.getLocaleId());
                    wordStatistic2 = getStatisticForDocument(selectedDocumentId,
                            o2.getLocaleId());
                }
                return compareWordStatistic(wordStatistic1, wordStatistic2,
                        selectedSortOption);
            } else {
                return o1.retrieveDisplayName()
                        .compareTo(o2.retrieveDisplayName());
            }
        }

        public void setSelectedDocumentId(final Long selectedDocumentId) {
            this.selectedDocumentId = selectedDocumentId;
        }
    }

    /**
     * Helper class to upload documents.
     */
    public static class SourceFileUploadHelper implements Serializable {

        private static final long serialVersionUID = 1L;
        private InputStream fileContents;
        private String docId;
        private String fileName;
        // TODO rename to customDocumentPath (update in EL also)
        private String documentPath;
        private String sourceLang = "en-US"; // en-US by default
        private String adapterParams = "";
        private String documentType;

        public InputStream getFileContents() {
            return this.fileContents;
        }

        public String getDocId() {
            return this.docId;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getDocumentPath() {
            return this.documentPath;
        }

        public String getSourceLang() {
            return this.sourceLang;
        }

        public String getAdapterParams() {
            return this.adapterParams;
        }

        public String getDocumentType() {
            return this.documentType;
        }

        public void setFileContents(final InputStream fileContents) {
            this.fileContents = fileContents;
        }

        public void setDocId(final String docId) {
            this.docId = docId;
        }

        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        public void setDocumentPath(final String documentPath) {
            this.documentPath = documentPath;
        }

        public void setSourceLang(final String sourceLang) {
            this.sourceLang = sourceLang;
        }

        public void setAdapterParams(final String adapterParams) {
            this.adapterParams = adapterParams;
        }

        public void setDocumentType(final String documentType) {
            this.documentType = documentType;
        }
    }

    /**
     * Helper class to upload translation files.
     */
    public static class TranslationFileUploadHelper implements Serializable {

        private static final long serialVersionUID = 1L;
        private String docId;
        private InputStream fileContents;
        private String fileName;
        private boolean mergeTranslations = true; // Merge by default
        private boolean assignCreditToUploader = false;
        private String documentType;

        public String getDocId() {
            return this.docId;
        }

        public InputStream getFileContents() {
            return this.fileContents;
        }

        public String getFileName() {
            return this.fileName;
        }

        public boolean isMergeTranslations() {
            return this.mergeTranslations;
        }

        public boolean isAssignCreditToUploader() {
            return this.assignCreditToUploader;
        }

        public String getDocumentType() {
            return this.documentType;
        }

        public void setDocId(final String docId) {
            this.docId = docId;
        }

        public void setFileContents(final InputStream fileContents) {
            this.fileContents = fileContents;
        }

        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        public void setMergeTranslations(final boolean mergeTranslations) {
            this.mergeTranslations = mergeTranslations;
        }

        public void setAssignCreditToUploader(
                final boolean assignCreditToUploader) {
            this.assignCreditToUploader = assignCreditToUploader;
        }

        public void setDocumentType(final String documentType) {
            this.documentType = documentType;
        }
    }

    public String getVersionSlug() {
        return this.versionSlug;
    }

    public String getProjectSlug() {
        return this.projectSlug;
    }

    public boolean isPageRendered() {
        return this.pageRendered;
    }

    public WordStatistic getOverallStatistic() {
        return this.overallStatistic;
    }

    public HLocale getSelectedLocale() {
        return this.selectedLocale;
    }

    public HDocument getSelectedDocument() {
        return this.selectedDocument;
    }

    public SourceFileUploadHelper getSourceFileUpload() {
        return this.sourceFileUpload;
    }

    public TranslationFileUploadHelper getTranslationFileUpload() {
        return this.translationFileUpload;
    }

    public SortingType getDocumentSortingList() {
        return this.documentSortingList;
    }

    public SortingType getSourceDocumentSortingList() {
        return this.sourceDocumentSortingList;
    }

    public SortingType getSettingsDocumentSortingList() {
        return this.settingsDocumentSortingList;
    }

    public SourceDocumentFilter getDocumentsTabDocumentFilter() {
        return this.documentsTabDocumentFilter;
    }

    public DocumentFilter getSettingsTabDocumentFilter() {
        return this.settingsTabDocumentFilter;
    }

    public DocumentFilter getLanguageTabDocumentFilter() {
        return this.languageTabDocumentFilter;
    }

    public CopyVersionHandler getCopyVersionHandler() {
        return this.copyVersionHandler;
    }

    public AbstractListFilter<HIterationGroup> getGroupFilter() {
        return this.groupFilter;
    }

    public AbstractListFilter<HLocale> getLanguageTabLanguageFilter() {
        return this.languageTabLanguageFilter;
    }

    public AbstractListFilter<HLocale> getDocumentsTabLanguageFilter() {
        return this.documentsTabLanguageFilter;
    }
}
