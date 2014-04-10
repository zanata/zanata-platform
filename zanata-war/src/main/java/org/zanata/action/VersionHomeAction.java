/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.File;
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
import javax.faces.application.FacesMessage;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.util.Hex;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.common.ProjectType;
import org.zanata.dao.CredentialsDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.VirusDetectedException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
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
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.AbstractSortAction;
import org.zanata.ui.FilterUtil;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.DateUtil;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.model.DocumentStatus;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Name("versionHomeAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class VersionHomeAction extends AbstractSortAction implements
        Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private DocumentDAO documentDAO;

    @In
    private LocaleService localeServiceImpl;

    @In
    private VersionStateCache versionStateCacheImpl;

    @In
    private TranslationStateCache translationStateCacheImpl;

    @In
    private ZanataMessages zanataMessages;

    @In
    private DocumentService documentServiceImpl;

    @In
    private ZanataIdentity identity;

    @In
    private TranslationFileService translationFileServiceImpl;

    @In
    private VirusScanner virusScanner;

    @In
    private LocaleDAO localeDAO;

    @In
    private TranslationService translationServiceImpl;

    @Setter
    @Getter
    private String versionSlug;

    @Setter
    @Getter
    private String projectSlug;

    @Getter
    private boolean pageRendered = false;

    @Getter
    private WordStatistic overallStatistic;

    @Getter
    private HLocale selectedLocale;

    @Getter
    @Setter
    private HDocument selectedDocument;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @In("filePersistService")
    private FilePersistService filePersistService;

    private List<HLocale> supportedLocale;

    private List<HDocument> documents;

    private Map<LocaleId, WordStatistic> localeStatisticMap;

    private Map<DocumentLocaleKey, WordStatistic> documentStatisticMap;

    private List<HIterationGroup> groups;

    private HProjectIteration version;

    @Getter
    private SourceFileUploadHelper sourceFileUpload =
            new SourceFileUploadHelper();

    @Getter
    private TranslationFileUploadHelper translationFileUpload =
            new TranslationFileUploadHelper();

    @Getter
    private SortingType documentSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.LAST_SOURCE_UPDATE,
                    SortingType.SortOption.LAST_TRANSLATED));

    @Getter
    private SortingType sourceDocumentSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.LAST_SOURCE_UPDATE));

    @Getter
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

    @Getter
    private final SourceDocumentFilter documentsTabDocumentFilter =
            new SourceDocumentFilter();

    @Getter
    private final DocumentFilter settingsTabDocumentFilter =
            new DocumentFilter();

    @Getter
    private final DocumentFilter languageTabDocumentFilter =
            new DocumentFilter();

    @Getter
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

    @Getter
    private final AbstractListFilter<HLocale> languageTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {
                @Override
                protected List<HLocale> fetchAll() {
                    return getSupportedLocale();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(elem.getLocaleId()
                            .getId(), filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.retrieveDisplayName(), filter);
                }
            };

    @Getter
    private final AbstractListFilter<HLocale> documentsTabLanguageFilter =
            new InMemoryListFilter<HLocale>() {
                @Override
                protected List<HLocale> fetchAll() {
                    return getSupportedLocale();
                }

                @Override
                protected boolean include(HLocale elem, String filter) {
                    return StringUtils.startsWithIgnoreCase(elem.getLocaleId()
                            .getId(), filter)
                            || StringUtils.containsIgnoreCase(
                            elem.retrieveDisplayName(), filter);
                }
            };

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
        loadStatistics();
    }

    @Override
    protected void loadStatistics() {
        localeStatisticMap = Maps.newHashMap();
        for (HLocale locale : getSupportedLocale()) {
            WordStatistic wordStatistic =
                    versionStateCacheImpl.getVersionStatistics(getVersion()
                            .getId(), locale.getLocaleId());
            wordStatistic.setRemainingHours(StatisticsUtil
                    .getRemainingHours(wordStatistic));
            localeStatisticMap.put(locale.getLocaleId(), wordStatistic);
        }

        overallStatistic = new WordStatistic();
        for (Map.Entry<LocaleId, WordStatistic> entry : localeStatisticMap.entrySet()) {
            overallStatistic.add(entry.getValue());
        }
        overallStatistic.setRemainingHours(StatisticsUtil
                .getRemainingHours(overallStatistic));

        documentStatisticMap = Maps.newHashMap();
    }

    @Override
    protected String getMessage(String key, Object... args) {
        return zanataMessages.getMessage(key, args);
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

    public List<HDocument> getDocuments() {
        if (documents == null) {
            documents =
                    documentDAO.getByProjectIteration(projectSlug, versionSlug,
                            false);
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
            documents =
                    documentDAO.getByProjectIteration(projectSlug, versionSlug,
                            false);
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

    public void setSelectedLocale(HLocale hLocale) {
        this.selectedLocale = hLocale;
        resetPageData();
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public class DocumentLocaleKey {
        private Long documentId;
        private LocaleId localeId;
    }

    public WordStatistic getStatisticsForLocale(LocaleId localeId) {
        return localeStatisticMap.get(localeId);
    }

    public WordStatistic getStatisticForDocument(Long documentId,
            LocaleId localeId) {
        DocumentLocaleKey key = new DocumentLocaleKey(documentId, localeId);
        if (!documentStatisticMap.containsKey(key)) {
            WordStatistic wordStatistic =
                    documentDAO.getWordStatistics(documentId, localeId);
            wordStatistic.setRemainingHours(StatisticsUtil
                    .getRemainingHours(wordStatistic));
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

        wordStatistic.setRemainingHours(StatisticsUtil
                .getRemainingHours(wordStatistic));
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
                DocumentStatus docStat =
                        translationStateCacheImpl.getDocumentStatus(
                                document.getId(), localeId);
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
        return isVersionActive()
                && identity != null
                && (identity.hasPermission("add-translation", getVersion()
                        .getProject(), hLocale) || identity.hasPermission(
                        "translation-review", getVersion().getProject(),
                        hLocale));
    }

    private boolean isVersionActive() {
        return getVersion().getProject().getStatus() == EntityStatus.ACTIVE
                || getVersion().getStatus() == EntityStatus.ACTIVE;
    }

    @Restrict("#{versionHomeAction.documentRemovalAllowed}")
    public void deleteDocument(Long docId) {
        HDocument doc = documentDAO.getById(docId);
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

    public boolean isDocumentRemovalAllowed() {
        // currently same permissions as uploading a document
        return this.isDocumentUploadAllowed();
    }

    public boolean isDocumentUploadAllowed() {
        return isVersionActive() && identity != null
                && identity.hasPermission("import-template", getVersion());
    }

    public boolean isZipFileDownloadAllowed() {
        return getVersion().getProjectType() != null
                && identity.hasPermission("download-all", getVersion());
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
                message =
                        zanataMessages
                                .getMessage("jsf.iteration.files.DownloadAllFiles.ProjectTypeNotSet");
            } else if (getVersion().getProjectType() != ProjectType.Gettext
                    && getVersion().getProjectType() != ProjectType.Podir) {
                message =
                        zanataMessages
                                .getMessage("jsf.iteration.files.DownloadAllFiles.ProjectTypeNotAllowed");
            }
        } else {
            message =
                    zanataMessages
                            .getMessage("jsf.iteration.files.DownloadAll");
        }
        return message;
    }

    public boolean isKnownProjectType() {
        ProjectType type =
                projectIterationDAO.getBySlug(projectSlug, versionSlug)
                        .getProjectType();
        return type != null;
    }

    public boolean isFileUploadAllowed(HLocale hLocale) {
        return isVersionActive()
                && identity != null
                && identity.hasPermission("modify-translation", getVersion()
                        .getProject(), hLocale);
    }

    public void uploadSourceFile() {
        identity.checkPermission("import-template", getVersion());

        if (sourceFileUpload.getFileName().endsWith(".pot")) {
            uploadPotFile();
        } else {
            DocumentType type =
                    translationFileServiceImpl.getDocumentType(sourceFileUpload
                            .getFileName());
            if (translationFileServiceImpl.hasAdapterFor(type)) {
                uploadAdapterFile();
            } else {
                conversationScopeMessages.setMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Unrecognized file extension for "
                                + sourceFileUpload.getFileName());
            }
        }
        resetPageData();
    }

    public boolean isPoDocument(String docId) {
        return translationFileServiceImpl.isPoDocument(projectSlug,
                versionSlug, docId);
    }

    public String extensionOf(String docPath, String docName) {
        return "."
                + translationFileServiceImpl.getFileExtension(projectSlug,
                        versionSlug, docPath, docName);
    }

    public boolean hasOriginal(String docPath, String docName) {
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, versionSlug, docPath
                        + docName);
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
            docId =
                    translationFileServiceImpl.generateDocId(
                            sourceFileUpload.getDocumentPath(),
                            sourceFileUpload.getFileName());
        }
        HDocument existingDoc =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        versionSlug, docId);
        boolean docExists = existingDoc != null;
        boolean useOfflinePo = docExists && !isPoDocument(docId);

        try {
            Resource doc =
                    translationFileServiceImpl.parseUpdatedPotFile(
                            sourceFileUpload.getFileContents(), docId,
                            sourceFileUpload.getFileName(), useOfflinePo);

            doc.setLang(new LocaleId(sourceFileUpload.getSourceLang()));

            // TODO Copy Trans values
            documentServiceImpl.saveDocument(projectSlug, versionSlug, doc,
                    new StringSet(ExtensionType.GetText.toString()), false);

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
        return Optional.fromNullable(Strings.emptyToNull(sourceFileUpload
                .getAdapterParams()));
    }

    // TODO add logging for disk writing errors
    // TODO damason: unify this with Source/TranslationDocumentUpload
    private void uploadAdapterFile() {
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
            InputStream fileContents =
                    new DigestInputStream(sourceFileUpload.getFileContents(),
                            md);
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
                doc =
                        translationFileServiceImpl.parseAdapterDocumentFile(
                                tempFile.toURI(), documentPath, fileName,
                                getOptionalParams());
            } else {
                doc =
                        translationFileServiceImpl
                                .parseUpdatedAdapterDocumentFile(
                                        tempFile.toURI(), docId, fileName,
                                        getOptionalParams());
            }
            doc.setLang(new LocaleId(sourceFileUpload.getSourceLang()));
            Set<String> extensions = Collections.<String> emptySet();
            // TODO Copy Trans values
            document =
                    documentServiceImpl.saveDocument(projectSlug, versionSlug,
                            doc, extensions, false);
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
            rawDocument.setContentHash(new String(Hex.encodeHex(md5hash)));
            rawDocument.setType(DocumentType.typeFor(translationFileServiceImpl
                    .extractExtension(fileName)));
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
                    tempFile);
            documentDAO.addRawDocument(document, rawDocument);
            documentDAO.flush();
        }

        translationFileServiceImpl.removeTempFile(tempFile);
    }

    public void uploadTranslationFile(HLocale hLocale) {
        identity.checkPermission("modify-translation", hLocale, getVersion()
                .getProject());
        try {
            // process the file
            TranslationsResource transRes =
                    translationFileServiceImpl.parseTranslationFile(
                            translationFileUpload.getFileContents(),
                            translationFileUpload.getFileName(), hLocale
                                    .getLocaleId().getId(), projectSlug,
                            versionSlug, translationFileUpload.docId);

            // translate it
            Set<String> extensions;
            if (translationFileUpload.getFileName().endsWith(".po")) {
                extensions = new StringSet(ExtensionType.GetText.toString());
            } else {
                extensions = Collections.<String> emptySet();
            }
            List<String> warnings =
                    translationServiceImpl
                            .translateAllInDoc(
                                    projectSlug,
                                    versionSlug,
                                    translationFileUpload.getDocId(),
                                    hLocale.getLocaleId(),
                                    transRes,
                                    extensions,
                                    translationFileUpload.isMergeTranslations() ? MergeType.AUTO
                                            : MergeType.IMPORT);

            StringBuilder infoMsg =
                    new StringBuilder("File ").append(
                            translationFileUpload.getFileName()).append(
                            " uploaded.");

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
                conversationScopeMessages.setMessages(warningMessages);
            }
        } catch (ZanataServiceException e) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_ERROR,
                    translationFileUpload.getFileName() + "-" + e.getMessage());
        }
        resetPageData();
    }

    private class DocumentFilter extends InMemoryListFilter<HDocument> {
        private DocumentDAO documentDAO = (DocumentDAO) Component
                .getInstance(DocumentDAO.class);

        @Override
        protected List<HDocument> fetchAll() {
            return getDocuments(documentDAO);
        }

        @Override
        protected boolean include(HDocument elem, String filter) {
            return StringUtils.containsIgnoreCase(elem.getName(), filter)
                    || StringUtils.containsIgnoreCase(elem.getPath(), filter);
        }
    };

    private class SourceDocumentFilter extends InMemoryListFilter<HDocument> {
        private DocumentDAO documentDAO = (DocumentDAO) Component
                .getInstance(DocumentDAO.class);

        @Override
        protected List<HDocument> fetchAll() {
            return getSourceDocuments(documentDAO);
        }

        @Override
        protected boolean include(HDocument elem, String filter) {
            return StringUtils.containsIgnoreCase(elem.getName(), filter)
                    || StringUtils.containsIgnoreCase(elem.getPath(), filter);
        }
    };

    private class DocumentComparator implements Comparator<HDocument> {
        private SortingType sortingType;

        @Setter
        private LocaleId selectedLocaleId;

        public DocumentComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HDocument o1, HDocument o2) {
            final HDocument item1, item2;

            if (sortingType.isDescending()) {
                item1 = o1;
                item2 = o2;
            } else {
                item1 = o2;
                item2 = o1;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            if (selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                return item1.getName().compareTo(item2.getName());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_SOURCE_UPDATE)) {
                return DateUtil.compareDate(item1.getLastChanged(),
                        item2.getLastChanged());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LAST_TRANSLATED)) {
                if (selectedLocaleId != null) {
                    DocumentStatus docStat1 =
                            translationStateCacheImpl.getDocumentStatus(
                                    o1.getId(), selectedLocaleId);
                    DocumentStatus docStat2 =
                            translationStateCacheImpl.getDocumentStatus(
                                    o2.getId(), selectedLocaleId);

                    return DateUtil.compareDate(
                            docStat1.getLastTranslatedDate(),
                            docStat2.getLastTranslatedDate());
                }
            } else {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;
                if (selectedLocaleId != null) {
                    wordStatistic1 =
                            getStatisticForDocument(item1.getId(),
                                    selectedLocaleId);

                    wordStatistic2 =
                            getStatisticForDocument(item2.getId(),
                                    selectedLocaleId);

                } else {
                    wordStatistic1 = getDocumentStatistic(item1.getId());
                    wordStatistic2 = getDocumentStatistic(item2.getId());
                }
                return compareWordStatistic(wordStatistic1, wordStatistic2,
                        selectedSortOption);
            }
            return 0;
        }
    }

    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;

        @Setter
        private Long selectedDocumentId;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale compareFrom, HLocale compareTo) {
            final HLocale item1, item2;

            if (sortingType.isDescending()) {
                item1 = compareFrom;
                item2 = compareTo;
            } else {
                item1 = compareTo;
                item2 = compareFrom;
            }

            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                WordStatistic wordStatistic1;
                WordStatistic wordStatistic2;

                if (selectedDocumentId == null) {
                    wordStatistic1 =
                            getStatisticsForLocale(item1.getLocaleId());
                    wordStatistic2 =
                            getStatisticsForLocale(item2.getLocaleId());
                } else {
                    wordStatistic1 =
                            getStatisticForDocument(selectedDocumentId,
                                    item1.getLocaleId());
                    wordStatistic2 =
                            getStatisticForDocument(selectedDocumentId,
                                    item2.getLocaleId());
                }
                return compareWordStatistic(wordStatistic1, wordStatistic2,
                        selectedSortOption);
            } else {
                return item1.retrieveDisplayName().compareTo(
                        item2.retrieveDisplayName());
            }
        }
    }

    /**
     * Helper class to upload documents.
     */
    @Getter
    @Setter
    public static class SourceFileUploadHelper implements Serializable {
        private static final long serialVersionUID = 1L;

        private InputStream fileContents;

        private String docId;

        private String fileName;

        // TODO rename to customDocumentPath (update in EL also)
        private String documentPath;

        private String sourceLang = "en-US"; // en-US by default

        private String adapterParams = "";
    }

    /**
     * Helper class to upload translation files.
     */
    @Getter
    @Setter
    public static class TranslationFileUploadHelper implements Serializable {
        private static final long serialVersionUID = 1L;

        private String docId;

        private InputStream fileContents;

        private String fileName;

        private boolean mergeTranslations = true; // Merge by default
    }
}
