/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.ApplicationConfiguration;
import org.zanata.config.SystemPropertyConfigStore;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.service.VirusScanner;
import com.google.common.io.Files;

@Named("filePersistService")
@RequestScoped
public class FileSystemPersistService implements FilePersistService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FileSystemPersistService.class);

    private static final String RAW_DOCUMENTS_SUBDIRECTORY = "documents";
    @Inject
    private ApplicationConfiguration appConfig;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private VirusScanner virusScanner;

    @Override
    public void persistRawDocumentContentFromFile(HRawDocument rawDocument,
            File fromFile, String extension) {
        String fileName = generateFileNameFor(rawDocument, extension);
        rawDocument.setFileId(fileName);
        File newFile = getFileForName(fileName);
        try {
            Files.copy(fromFile, newFile);
        } catch (IOException e) {
            // FIXME damason: throw something more specific and handle at call
            // sites
            throw new RuntimeException(e);
        }
        GlobalDocumentId globalId = getGlobalId(rawDocument);
        log.info("Persisted raw document {} to file {}", globalId,
                newFile.getAbsolutePath());
        virusScanner.scan(newFile, globalId.toString());
    }

    @Override
    public void copyAndPersistRawDocument(HRawDocument fromDoc,
            HRawDocument toDoc) {
        File file = getFileForRawDocument(fromDoc);
        persistRawDocumentContentFromFile(toDoc, file,
                FilenameUtils.getExtension(file.getName()));
    }

    private File getFileForName(String fileName) {
        File docsPath = ensureDocsDirectory();
        File newFile = new File(docsPath, fileName);
        return newFile;
    }

    private File ensureDocsDirectory() {
        String basePathStringOrNull =
                appConfig.getDocumentFileStorageLocation();
        if (basePathStringOrNull == null) {
            throw new RuntimeException(
                    "Document storage location is not configured as system property:"
                            + SystemPropertyConfigStore.KEY_DOCUMENT_FILE_STORE);
        }
        File docsDirectory =
                new File(basePathStringOrNull, RAW_DOCUMENTS_SUBDIRECTORY);
        docsDirectory.mkdirs();
        return docsDirectory;
    }

    private static String generateFileNameFor(HRawDocument rawDocument,
            String extension) {
        // Could change to use id of rawDocument, and throw if rawDocument has
        // no id yet.
        String idAsString = rawDocument.getDocument().getId().toString();
        return idAsString + "." + extension;
    }
    // TODO damason: put this in a more appropriate location

    private static GlobalDocumentId getGlobalId(HRawDocument rawDocument) {
        HDocument document = rawDocument.getDocument();
        HProjectIteration version = document.getProjectIteration();
        HProject project = version.getProject();
        GlobalDocumentId id = new GlobalDocumentId(project.getSlug(),
                version.getSlug(), document.getDocId());
        return id;
    }

    @Override
    public InputStream getRawDocumentContentAsStream(HRawDocument document)
            throws RawDocumentContentAccessException {
        File rawFile = getFileForRawDocument(document);
        try {
            return new FileInputStream(rawFile);
        } catch (FileNotFoundException e) {
            // FIXME damason: throw more specific exception and handle at call
            // sites
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPersistedDocument(GlobalDocumentId id) {
        HDocument doc = documentDAO.getByGlobalId(id);
        if (doc != null) {
            HRawDocument rawDocument = doc.getRawDocument();
            return rawDocument != null
                    && getFileForRawDocument(rawDocument).exists();
        }
        return false;
    }

    private File getFileForRawDocument(HRawDocument rawDocument) {
        return getFileForName(rawDocument.getFileId());
    }
}
