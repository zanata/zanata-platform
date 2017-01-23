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
package org.zanata.service.impl;

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.xml.sax.InputSource;
import org.zanata.adapter.DTDAdapter;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.GettextAdapter;
import org.zanata.adapter.HTMLAdapter;
import org.zanata.adapter.IDMLAdapter;
import org.zanata.adapter.JsonAdapter;
import org.zanata.adapter.OpenOfficeAdapter;
import org.zanata.adapter.PlainTextAdapter;
import org.zanata.adapter.PropertiesLatinOneAdapter;
import org.zanata.adapter.PropertiesUTF8Adapter;
import org.zanata.adapter.SubtitleAdapter;
import org.zanata.adapter.TSAdapter;
import org.zanata.adapter.XliffAdapter;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;
import org.zanata.util.FileUtil;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.zanata.common.DocumentType.GETTEXT;
import static org.zanata.common.DocumentType.HTML;
import static org.zanata.common.DocumentType.IDML;
import static org.zanata.common.DocumentType.JSON;
import static org.zanata.common.DocumentType.OPEN_DOCUMENT_GRAPHICS;
import static org.zanata.common.DocumentType.OPEN_DOCUMENT_PRESENTATION;
import static org.zanata.common.DocumentType.OPEN_DOCUMENT_SPREADSHEET;
import static org.zanata.common.DocumentType.OPEN_DOCUMENT_TEXT;
import static org.zanata.common.DocumentType.PLAIN_TEXT;
import static org.zanata.common.DocumentType.PROPERTIES;
import static org.zanata.common.DocumentType.PROPERTIES_UTF8;
import static org.zanata.common.DocumentType.SUBTITLE;
import static org.zanata.common.DocumentType.TS;
import static org.zanata.common.DocumentType.XLIFF;
import static org.zanata.common.DocumentType.XML_DOCUMENT_TYPE_DEFINITION;

/**
 * Default implementation of the TranslationFileService interface.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("translationFileServiceImpl")
@RequestScoped
@Transactional
public class TranslationFileServiceImpl implements TranslationFileService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationFileServiceImpl.class);

    private static Map<DocumentType, Class<? extends FileFormatAdapter>> DOCTYPEMAP =
            new MapMaker().makeMap();
    private static DocumentType[] ODF_TYPES =
            { OPEN_DOCUMENT_TEXT, OPEN_DOCUMENT_PRESENTATION,
                    OPEN_DOCUMENT_SPREADSHEET, OPEN_DOCUMENT_GRAPHICS };
    static {
        for (DocumentType type : ODF_TYPES) {
            DOCTYPEMAP.put(type, OpenOfficeAdapter.class);
        }
        DOCTYPEMAP.put(PLAIN_TEXT, PlainTextAdapter.class);
        DOCTYPEMAP.put(XML_DOCUMENT_TYPE_DEFINITION, DTDAdapter.class);
        DOCTYPEMAP.put(IDML, IDMLAdapter.class);
        DOCTYPEMAP.put(HTML, HTMLAdapter.class);
        DOCTYPEMAP.put(JSON, JsonAdapter.class);
        DOCTYPEMAP.put(SUBTITLE, SubtitleAdapter.class);
        DOCTYPEMAP.put(PROPERTIES, PropertiesLatinOneAdapter.class);
        DOCTYPEMAP.put(PROPERTIES_UTF8, PropertiesUTF8Adapter.class);
        DOCTYPEMAP.put(XLIFF, XliffAdapter.class);
        DOCTYPEMAP.put(GETTEXT, GettextAdapter.class);
        DOCTYPEMAP.put(TS, TSAdapter.class);
    }
    private static Set<String> SUPPORTED_EXTENSIONS =
            buildSupportedExtensionSet();

    private static Set<String> buildSupportedExtensionSet() {
        Set<String> supported = new HashSet<String>();
        for (DocumentType type : DOCTYPEMAP.keySet()) {
            supported.addAll(type.getSourceExtensions());
        }
        return supported;
    }

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Override
    public TranslationsResource parseTranslationFile(InputStream fileContents,
            String fileName, String localeId, String projectSlug,
            String iterationSlug, String docId, Optional<String> documentType)
            throws ZanataServiceException {
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        if (version == null) {
            throw new ZanataServiceException("Project version not found: "
                    + projectSlug + " " + iterationSlug);
        }
        if (version.getProjectType() == ProjectType.File) {
            File tempFile = persistToTempFile(fileContents);
            TranslationsResource transRes = parseAdapterTranslationFile(
                    tempFile, projectSlug, iterationSlug, docId, localeId,
                    fileName, documentType);
            removeTempFile(tempFile);
            return transRes;
        } else if (fileName.endsWith(".po")) {
            return parsePoFile(fileContents, projectSlug, iterationSlug, docId);
        } else {
            throw new ZanataServiceException(
                    "Unsupported Translation file: " + fileName);
        }
    }

    @Override
    public TranslationsResource parsePoFile(InputStream fileContents,
            String projectSlug, String iterationSlug, String docId) {
        boolean originalIsPo = isPoDocument(projectSlug, iterationSlug, docId);
        try {
            return parsePoFile(fileContents, !originalIsPo);
        } catch (Exception e) {
            throw new ZanataServiceException(
                    "Invalid PO file contents on file: " + docId, e);
        }
    }

    @Override
    public TranslationsResource parseAdapterTranslationFile(File tempFile,
            String projectSlug, String iterationSlug, String docId,
            String localeId, String fileName, Optional<String> documentType) {
        HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug,
                iterationSlug, docId);
        TranslationsResource transRes;
        FileFormatAdapter adapter = getAdapterFor(documentType, fileName);
        try {
            transRes = adapter.parseTranslationFile(tempFile.toURI(),
                    doc.getSourceLocaleId(), localeId, getAdapterParams(doc));
        } catch (FileFormatAdapterException e) {
            throw new ZanataServiceException(
                    "Error parsing translation file: " + fileName, e);
        } catch (RuntimeException e) {
            throw new ZanataServiceException(e);
        }
        return transRes;
    }

    public Optional<String> getAdapterParams(HDocument doc) {
        if (doc != null) {
            HRawDocument rawDoc = doc.getRawDocument();
            if (rawDoc != null) {
                return Optional.fromNullable(rawDoc.getAdapterParameters());
            }
        }
        return Optional.<String> absent();
    }

    @Override
    public Resource parseUpdatedPotFile(InputStream fileContents, String docId,
            String fileName, boolean offlinePo) {
        if (fileName.endsWith(".pot")) {
            try {
                return parsePotFile(fileContents, docId, offlinePo);
            } catch (Exception e) {
                throw new ZanataServiceException(
                        "Invalid POT file contents on file: " + docId, e);
            }
        } else {
            throw new ZanataServiceException(
                    "Unsupported Document file: " + docId);
        }
    }

    @Override
    public boolean hasMultipleDocumentTypes(String fileNameOrExtension) {
        String extension = FilenameUtils.getExtension(fileNameOrExtension);
        return DocumentType.fromSourceExtension(extension).size() > 1;
    }

    @Override
    public Set<DocumentType> getDocumentTypes(String fileNameOrExtension) {
        String extension = FilenameUtils.getExtension(fileNameOrExtension);
        Set<DocumentType> documentTypes =
                DocumentType.fromSourceExtension(extension);
        documentTypes.addAll(DocumentType.fromTranslationExtension(extension));
        return documentTypes;
    }

    @Override
    public Resource parseAdapterDocumentFile(URI documentFile,
            String documentPath, String fileName, Optional<String> params,
            Optional<String> documentType) throws ZanataServiceException {
        return parseUpdatedAdapterDocumentFile(documentFile,
                FileUtil.convertToValidPath(documentPath) + fileName, fileName,
                params, documentType);
    }

    @Override
    public Resource parseUpdatedAdapterDocumentFile(URI documentFile,
            String docId, String fileName, Optional<String> params,
            Optional<String> documentType) throws ZanataServiceException {
        FileFormatAdapter adapter = getAdapterFor(documentType, fileName);
        Resource doc;
        try {
            doc = adapter.parseDocumentFile(documentFile, new LocaleId("en"),
                    params);
        } catch (FileFormatAdapterException e) {
            throw new ZanataServiceException(
                    "Error parsing document file: " + fileName, e);
        }
        doc.setName(docId);
        return doc;
    }

    private TranslationsResource parsePoFile(InputStream fileContents,
            boolean offlinePo) {
        PoReader2 poReader = new PoReader2(offlinePo);
        return poReader.extractTarget(new InputSource(fileContents));
    }

    private Resource parsePotFile(InputStream fileContents, String docId,
            boolean offlinePo) {
        PoReader2 poReader = new PoReader2(offlinePo);
        // assume english as source locale
        Resource res = poReader.extractTemplate(new InputSource(fileContents),
                new LocaleId("en"), docId);
        return res;
    }
    // TODO replace these with values from DocumentType

    @Override
    public Set<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public boolean hasAdapterFor(DocumentType type) {
        if (type == null) {
            return false;
        }
        return DOCTYPEMAP.containsKey(type);
    }

    @Override
    public Set<DocumentType> getSupportedDocumentTypes() {
        return DOCTYPEMAP.keySet();
    }

    private boolean hasAdapterFor(String fileNameOrExtension) {
        String extension = FilenameUtils.getExtension(fileNameOrExtension);
        if (extension == null) {
            return false;
        }
        DocumentType documentType = DocumentType.getByName(extension);
        if (documentType == null) {
            return false;
        }
        return hasAdapterFor(documentType);
    }

    private FileFormatAdapter getAdapterFor(String fileNameOrExtension) {
        String extension = FilenameUtils.getExtension(fileNameOrExtension);
        if (extension == null) {
            throw new RuntimeException(
                    "Cannot find adapter for null filename or extension.");
        }
        DocumentType documentType = DocumentType.getByName(extension);
        if (documentType == null) {
            throw new RuntimeException(
                    "Cannot choose an adapter because the provided string \'"
                            + fileNameOrExtension
                            + "\' does not match any known document type.");
        }
        FileFormatAdapter adapter = getAdapterFor(documentType);
        if (hasMultipleDocumentTypes(fileNameOrExtension)) {
            log.warn(
                    "More than 1 adapter found for this file extension: \'{}\'. Adapter \'{}\' will be used.",
                    extension, adapter.getClass().getName());
        }
        return adapter;
    }

    /**
     * TODO: throw runtime error. Need to wait for all upload file dialog
     * implement multiple adapter check for file extension.
     *
     * https://bugzilla.redhat.com/show_bug.cgi?id=1217671
     */
    @Override
    public FileFormatAdapter getAdapterFor(DocumentType type) {
        Class<? extends FileFormatAdapter> clazz = DOCTYPEMAP.get(type);
        if (clazz == null) {
            throw new RuntimeException("No adapter for document type: " + type);
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to construct adapter for document type: " + type,
                    e);
        }
    }

    /**
     * Get an appropriate adapter for a document type or file name.
     *
     * @param documentType
     * @param fileName
     * @return adapter for given documentType if present, otherwise return
     *         adapter with given fileName.
     */
    private FileFormatAdapter getAdapterFor(Optional<String> documentType,
            @Nonnull String fileName) {
        if (documentType.isPresent()
                && StringUtils.isNotEmpty(documentType.get())) {
            DocumentType docType = DocumentType.valueOf(documentType.get());
            return docType != null ? getAdapterFor(docType)
                    : getAdapterFor(fileName);
        }
        return getAdapterFor(fileName);
    }

    @Override
    public File persistToTempFile(InputStream fileContents) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("zupload", ".tmp");
            byte[] buffer = new byte[4096]; // To hold file contents
            int bytesRead;
            FileOutputStream output = new FileOutputStream(tempFile);
            while ((bytesRead = fileContents.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.close();
        } catch (IOException e) {
            throw new ZanataServiceException(
                    "Error while writing uploaded file to temporary location",
                    e);
        }
        return tempFile;
    }

    @Override
    public void removeTempFile(File tempFile) {
        if (tempFile != null) {
            if (!tempFile.delete()) {
                log.warn(
                        "unable to remove temporary file {}, marking for delete on exit",
                        tempFile.getAbsolutePath());
                tempFile.deleteOnExit();
            }
        }
    }

    @Override
    public String getSourceFileExtension(String projectSlug,
            String iterationSlug, String docPath, String docName) {
        return FilenameUtils.getExtension(docName);
    }

    @Override
    public String getTranslationFileExtension(String projectSlug,
            String iterationSlug, String docPath, String docName) {
        String srcExt = getSourceFileExtension(projectSlug, iterationSlug,
                docPath, docName);
        HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug,
                iterationSlug, docPath + docName);
        return doc.getRawDocument().getType().getExtensions().get(srcExt);
    }

    @Override
    public boolean isPoDocument(String projectSlug, String iterationSlug,
            String docId) {
        HProjectIteration projectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        ProjectType projectType = projectIteration.getProjectType();
        if (projectType == null) {
            projectType = projectIteration.getProject().getDefaultProjectType();
        }
        if (projectType == ProjectType.Gettext
                || projectType == ProjectType.Podir) {
            return true;
        }
        if (projectType == ProjectType.File) {
            HDocument doc =
                    documentDAO.getByDocIdAndIteration(projectIteration, docId);
            if (doc.getRawDocument() == null) {
                // po is the only format in File projects for which no raw
                // document is stored
                return true;
            }
            // additional check in case we do start storing raw documents for po
            DocumentType docType = doc.getRawDocument().getType();
            return docType == GETTEXT;
        }
        return false;
    }
}
