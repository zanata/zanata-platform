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

import static com.google.common.base.Strings.isNullOrEmpty;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Vector;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response.Status;
import org.hibernate.Session;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.exception.HashMismatchException;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HDocumentUploadPart;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.util.PasswordUtil;
import com.google.common.base.Optional;
// TODO damason: add thorough unit testing

@Named("documentUploadUtil")
@Dependent
public class DocumentUploadUtil {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DocumentUploadUtil.class);

    @Inject
    private ZanataIdentity identity;
    // TODO technical debt: use entityManager
    @Inject
    private Session session;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TranslationFileService translationFileServiceImpl;
    @Inject
    private UploadPartPersistService uploadPartPersistService;
    @Inject
    private DocumentUploadDAO documentUploadDAO;
    // TODO damason: move all validation checks to separate class

    public void failIfUploadNotValid(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        failIfNotLoggedIn();
        failIfRequiredParametersNullOrEmpty(id, uploadForm);
        failIfUploadPartIsOrphaned(id, uploadForm);
        failIfDocumentTypeNotRecognized(uploadForm);
        failIfVersionCannotAcceptUpload(id);
    }

    public void failIfHashNotPresent(DocumentFileUploadForm uploadForm) {
        if (isNullOrEmpty(uploadForm.getHash())) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Required form parameter \'hash\' was not found.");
        }
    }

    private void failIfNotLoggedIn() throws ChunkUploadException {
        if (!identity.isLoggedIn()) {
            throw new ChunkUploadException(Status.UNAUTHORIZED,
                    "Valid combination of username and api-key for this server were not included in the request.");
        }
    }

    private static void failIfRequiredParametersNullOrEmpty(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        if (isNullOrEmpty(id.getDocId())) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Required query string parameter \'docId\' was not found.");
        }
        if (uploadForm.getFileStream() == null) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Required form parameter \'file\' containing file content was not found.");
        }
        if (uploadForm.getFirst() == null || uploadForm.getLast() == null) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Form parameters \'first\' and \'last\' must both be provided.");
        }
        if (isNullOrEmpty(uploadForm.getFileType())) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Required form parameter \'type\' was not found.");
        }
    }

    private void failIfUploadPartIsOrphaned(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        if (!uploadForm.getFirst()) {
            failIfUploadIdNotValidAndMatching(id, uploadForm);
        }
    }

    private void failIfUploadIdNotValidAndMatching(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        if (uploadForm.getUploadId() == null) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Form parameter \'uploadId\' must be provided when this is not the first part.");
        }
        HDocumentUpload upload =
                documentUploadDAO.findById(uploadForm.getUploadId());
        if (upload == null) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "No incomplete uploads found for uploadId \'"
                            + uploadForm.getUploadId() + "\'.");
        }
        if (!upload.getDocId().equals(id.getDocId())) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Supplied uploadId \'" + uploadForm.getUploadId()
                            + "\' in request is not valid for document \'"
                            + id.getDocId() + "\'.");
        }
    }

    private static void failIfDocumentTypeNotRecognized(
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        try {
            DocumentType type = DocumentType.valueOf(uploadForm.getFileType());
            if (type == null) {
                throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                        "Value \'" + uploadForm.getFileType()
                                + "\' is not a recognized document type.");
            }
        } catch (IllegalArgumentException e) {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                    "Value \'" + uploadForm.getFileType()
                            + "\' is not a recognized document type.");
        }
    }

    private void failIfVersionCannotAcceptUpload(GlobalDocumentId id)
            throws ChunkUploadException {
        HProjectIteration projectIteration = projectIterationDAO
                .getBySlug(id.getProjectSlug(), id.getVersionSlug());
        if (projectIteration == null) {
            throw new ChunkUploadException(Status.NOT_FOUND,
                    "The specified project-version \"" + id.getProjectSlug()
                            + ":" + id.getVersionSlug()
                            + "\" does not exist on this server.");
        }
        if (projectIteration.getProject().getStatus() != EntityStatus.ACTIVE) {
            throw new ChunkUploadException(Status.FORBIDDEN, "The project \""
                    + id.getProjectSlug()
                    + "\" is not active. Document upload is not allowed.");
        }
        if (projectIteration.getStatus() != EntityStatus.ACTIVE) {
            throw new ChunkUploadException(Status.FORBIDDEN,
                    "The project-version \"" + id.getProjectSlug() + ":"
                            + id.getVersionSlug()
                            + "\" is not active. Document upload is not allowed.");
        }
    }

    protected HDocumentUpload saveUploadPart(GlobalDocumentId id,
            HLocale locale, DocumentFileUploadForm uploadForm) {
        HDocumentUpload upload;
        if (uploadForm.getFirst()) {
            upload = createMultipartUpload(id, uploadForm, locale);
        } else {
            upload = documentUploadDAO.findById(uploadForm.getUploadId());
        }
        saveUploadPart(uploadForm, upload);
        return upload;
    }

    private HDocumentUpload createMultipartUpload(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm, HLocale locale) {
        HProjectIteration projectIteration = projectIterationDAO
                .getBySlug(id.getProjectSlug(), id.getVersionSlug());
        HDocumentUpload newUpload = new HDocumentUpload();
        newUpload.setProjectIteration(projectIteration);
        newUpload.setDocId(id.getDocId());
        newUpload.setType(DocumentType.getByName(uploadForm.getFileType()));
        // locale intentionally left null for source
        newUpload.setLocale(locale);
        newUpload.setContentHash(uploadForm.getHash());
        return newUpload;
    }

    private void saveUploadPart(DocumentFileUploadForm uploadForm,
            HDocumentUpload upload) {
        InputStream contentStream = uploadForm.getFileStream();
        int contentLength = uploadForm.getSize().intValue();
        HDocumentUploadPart newPart = uploadPartPersistService
                .newUploadPartFromStream(contentStream, contentLength);
        upload.getParts().add(newPart);
        session.saveOrUpdate(upload);
        session.flush();
    }

    protected static boolean isSinglePart(DocumentFileUploadForm uploadForm) {
        return uploadForm.getFirst() && uploadForm.getLast();
    }

    public File combineToTempFileAndDeleteUploadRecord(HDocumentUpload upload,
            DocumentFileUploadForm finalPart) {
        File tempFile;
        try {
            tempFile = combineToTempFile(upload, finalPart);
        } catch (HashMismatchException e) {
            throw new ChunkUploadException(Status.CONFLICT, "MD5 hash \""
                    + e.getExpectedHash()
                    + "\" sent with initial request does not match server-generated hash of combined parts \""
                    + e.getGeneratedHash()
                    + "\". Upload aborted. Retry upload from first part.");
        } catch (SQLException e) {
            throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
                    "Error while retrieving document upload part contents", e);
        } finally {
            // no more need for upload
            session.delete(upload);
        }
        return tempFile;
    }

    private File combineToTempFile(HDocumentUpload upload,
            DocumentFileUploadForm finalPart) throws SQLException {
        Vector<InputStream> partStreams = new Vector<InputStream>();
        for (HDocumentUploadPart part : upload.getParts()) {
            partStreams.add(part.getContent().getBinaryStream());
        }
        partStreams.add(finalPart.getFileStream());
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available.", e);
            throw new RuntimeException(e);
        }
        InputStream combinedParts =
                new SequenceInputStream(partStreams.elements());
        combinedParts = new DigestInputStream(combinedParts, md);
        File tempFile =
                translationFileServiceImpl.persistToTempFile(combinedParts);
        checkAndUpdateHash(finalPart, md, upload.getContentHash());
        return tempFile;
    }

    protected static InputStream getInputStream(Optional<File> tempFile,
            DocumentFileUploadForm uploadForm) throws FileNotFoundException {
        if (tempFile.isPresent()) {
            return new FileInputStream(tempFile.get());
        } else {
            return uploadForm.getFileStream();
        }
    }
    // TODO damason: add getByGlobalDocumentId(GlobalDocumentId) to documentDAO,
    // use it, and inline this method

    protected boolean isNewDocument(GlobalDocumentId id) {
        return documentDAO.getByProjectIterationAndDocId(id.getProjectSlug(),
                id.getVersionSlug(), id.getDocId()) == null;
    }

    protected File
            persistTempFileFromUpload(DocumentFileUploadForm uploadForm) {
        File tempFile;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileContents =
                    new DigestInputStream(uploadForm.getFileStream(), md);
            tempFile =
                    translationFileServiceImpl.persistToTempFile(fileContents);
            String providedHash = uploadForm.getHash();
            checkAndUpdateHash(uploadForm, md, providedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
                    "MD5 hash algorithm not available", e);
        }
        return tempFile;
    }

    /**
     * Makes sure any provided hash matches the calculated hash, and sets the
     * calculated hash into the given upload form for use in subsequent steps.
     *
     * @param md
     *            an MD5 message digester that has had the contents of the file
     *            streamed through it.
     * @param providedHash
     *            provided by client, may be null or empty
     * @throws ChunkUploadException
     *             if a hash is provided and it does not match the hash of the
     *             file contents.
     */
    private void checkAndUpdateHash(DocumentFileUploadForm uploadForm,
            MessageDigest md, String providedHash) {
        String md5hash = new String(PasswordUtil.encodeHex(md.digest()));
        if (isNullOrEmpty(providedHash)) {
            // Web upload with no hash provided, use generated hash for metadata
            uploadForm.setHash(md5hash);
        } else if (!md5hash.equals(providedHash)) {
            throw new ChunkUploadException(Status.CONFLICT, "MD5 hash \""
                    + providedHash
                    + "\" sent with request does not match server-generated hash. Aborted upload operation.");
        }
    }
}
