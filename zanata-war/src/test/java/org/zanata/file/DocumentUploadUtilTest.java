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

import static com.google.common.base.Charsets.UTF_8;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;
import static org.zanata.file.DocumentUploadUtil.getInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.EntityStatus;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HDocumentUploadPart;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;
import com.google.common.io.Files;

@Test(groups = { "unit-tests" })
public class DocumentUploadUtilTest extends DocumentUploadTest {

    private static final String HASH_OF_ABCDEFGHI =
            "d41d8cd98f00b204e9800998ecf8427e";
    @Mock
    Session session;
    @Mock
    TranslationFileService translationFileService;
    @Mock
    UploadPartPersistService uploadPartPersistService;

    @Mock
    Blob partBlob0;
    @Mock
    Blob partBlob1;

    @Captor
    private ArgumentCaptor<InputStream> persistedInputStreamCaptor;

    private DocumentUploadUtil util;

    @BeforeMethod
    public void beforeEachMethod() {
        MockitoAnnotations.initMocks(this);
        seam.reset();
        seam.ignoreNonResolvable().use("identity", identity)
                .use("session", session).use("documentDAO", documentDAO)
                .use("documentUploadDAO", documentUploadDAO)
                .use("projectIterationDAO", projectIterationDAO)
                .use("translationFileServiceImpl", translationFileService)
                .use("uploadPartPersistService", uploadPartPersistService)
                .allowCycles();

        util = seam.autowire(DocumentUploadUtil.class);
    }

    public void notValidIfNotLoggedIn() {
        conf = defaultUpload().build();
        mockNotLoggedIn();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if user is not logged in");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(UNAUTHORIZED));
            assertThat(e.getMessage(),
                    is("Valid combination of username and api-key for this "
                            + "server were not included in the request."));
        }
    }

    public void notValidIfNoFileContent() {
        conf = defaultUpload().fileStream(null).build();
        mockLoggedIn();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if there is no file content");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(
                    e.getMessage(),
                    is("Required form parameter 'file' containing file content "
                            + "was not found."));
        }
    }

    public void notValidIfNoFileType() {
        conf = defaultUpload().fileType(null).build();
        mockLoggedIn();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if file type is not set.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(e.getMessage(),
                    is("Required form parameter 'type' was not found."));
        }
    }

    public void notValidIfNoContentHash() {
        conf = defaultUpload().hash(null).build();
        mockLoggedIn();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if hash is not set.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(e.getMessage(),
                    is("Required form parameter 'hash' was not found."));
        }
    }

    public void notValidIfVersionDoesNotExist() {
        conf = defaultUpload().build();
        mockLoggedIn();
        mockVersionDoesNotExist();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if project-version does not exist.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(NOT_FOUND));
            assertThat(e.getMessage(),
                    is("The specified project-version \"myproject:myversion\" "
                            + "does not exist on this server."));
        }
    }

    public void notValidIfProjectIsReadOnly() {
        notValidIfProjectStatusIs(EntityStatus.READONLY);
    }

    public void notValidIfProjectIsObsolete() {
        notValidIfProjectStatusIs(EntityStatus.OBSOLETE);
    }

    private void notValidIfProjectStatusIs(EntityStatus nonActiveStatus) {
        conf = defaultUpload().projectStatus(nonActiveStatus).build();
        mockLoggedIn();
        mockProjectAndVersionStatus();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if project is read only or obsolete.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(FORBIDDEN));
            assertThat(
                    e.getMessage(),
                    is("The project \"myproject\" is not active. Document upload "
                            + "is not allowed."));
        }
    }

    public void notValidIfVersionIsReadOnly() {
        notValidIfVersionStatusIs(EntityStatus.READONLY);
    }

    public void notValidIfVersionIsObsolete() {
        notValidIfVersionStatusIs(EntityStatus.OBSOLETE);
    }

    private void notValidIfVersionStatusIs(EntityStatus nonActiveStatus) {
        conf = defaultUpload().versionStatus(nonActiveStatus).build();
        mockLoggedIn();
        mockProjectAndVersionStatus();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if version is read only or obsolete.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(FORBIDDEN));
            assertThat(e.getMessage(),
                    is("The project-version \"myproject:myversion\" is not "
                            + "active. Document upload is not allowed."));
        }
    }

    public void notValidIfFileTypeInvalid() {
        conf = defaultUpload().fileType("invalid").build();
        mockLoggedIn();
        mockProjectAndVersionStatus();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if file type is not valid.");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(e.getMessage(),
                    is("Value 'invalid' is not a recognized document type."));
        }
    }

    public void subsequentPartNoUploadId() {
        conf = defaultUpload().first(false).uploadId(null).build();
        mockLoggedIn();
        mockProjectAndVersionStatus();
        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
            fail("Should throw exception if this is not the first part but no uploadId is supplied");
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(
                    e.getMessage(),
                    is("Form parameter 'uploadId' must be provided when this is "
                            + "not the first part."));

        }
    }

    public void subsequentPartUploadNotPresent() {
        conf = defaultUpload().first(false).uploadId(5L).build();
        mockLoggedIn();
        mockProjectAndVersionStatus();
        Mockito.when(documentUploadDAO.findById(conf.uploadId))
                .thenReturn(null);

        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(e.getMessage(),
                    is("No incomplete uploads found for uploadId '5'."));
        }
    }

    public void subsequentPartUploadMismatchedDocId() {
        conf =
                defaultUpload().first(false).uploadId(5L)
                        .docId("mismatched-id").build();
        mockLoggedIn();
        mockProjectAndVersionStatus();

        HDocumentUpload upload = new HDocumentUpload();
        upload.setDocId("correct-id");
        when(documentUploadDAO.findById(conf.uploadId)).thenReturn(upload);

        try {
            util.failIfUploadNotValid(conf.id, conf.uploadForm);
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
            assertThat(
                    e.getMessage(),
                    is("Supplied uploadId '5' in request is not valid for document 'mismatched-id'."));
        }
    }

    public void returnFormStreamWhenFileIsAbsent() throws FileNotFoundException {
        InputStream streamFromForm =
                new ByteArrayInputStream("test".getBytes());
        conf = defaultUpload().fileStream(streamFromForm).build();
        InputStream returnedStream =
                getInputStream(Optional.<File> absent(), conf.uploadForm);

        assertThat(returnedStream, is(sameInstance(streamFromForm)));
    }

    public void returnFileStreamWhenFileIsPresent() throws IOException {
        File f = null;
        try {
            f = File.createTempFile("test", "test");
            Files.write("text in file", f, UTF_8);
            Optional<File> presentFile = Optional.of(f);
            InputStream streamFromForm =
                    new ByteArrayInputStream("test".getBytes());
            conf = defaultUpload().fileStream(streamFromForm).build();

            InputStream returnedStream =
                    getInputStream(presentFile, conf.uploadForm);

            assertThat(IOUtils.toString(returnedStream, "utf-8"),
                    is("text in file"));
        } finally {
            if (f != null) {
                f.delete();
            }
        }
    }

    public void canCombineUploadPartsInOrder() throws SQLException, IOException {
        HDocumentUpload upload = mockTwoPartUploadUsingHash(HASH_OF_ABCDEFGHI);

        InputStream finalPartStream =
                new ByteArrayInputStream("ghi".getBytes());
        File persistedFile = new File("test");

        when(
                translationFileService
                        .persistToTempFile(persistedInputStreamCaptor.capture()))
                .thenReturn(persistedFile);

        File returnedFile =
                util.combineToTempFileAndDeleteUploadRecord(upload,
                        finalPartStream);

        assertThat(returnedFile, is(sameInstance(persistedFile)));
        String persistedContents =
                IOUtils.toString(persistedInputStreamCaptor.getValue(), "utf-8");
        assertThat(persistedContents, is("abcdefghi"));
        verify(session).delete(upload);
    }

    public void combineFailsOnHashMismatch() throws SQLException {
        HDocumentUpload upload = mockTwoPartUploadUsingHash("incorrect hash");
        InputStream finalPartStream =
                new ByteArrayInputStream("ghi".getBytes());

        try {
            util.combineToTempFileAndDeleteUploadRecord(upload, finalPartStream);
        } catch (ChunkUploadException e) {
            assertThat(e.getStatusCode(), is(CONFLICT));
            assertThat(
                    e.getMessage(),
                    is("MD5 hash \"incorrect hash\" sent with initial request"
                            + " does not match server-generated hash of combined parts \""
                            + HASH_OF_ABCDEFGHI
                            + "\". Upload aborted. Retry upload from first part."));
        }
    }

    private HDocumentUpload mockTwoPartUploadUsingHash(String hash)
            throws SQLException {
        HDocumentUpload upload = new HDocumentUpload();
        upload.setContentHash(hash);

        HDocumentUploadPart part0 = new HDocumentUploadPart();
        when(partBlob0.getBinaryStream()).thenReturn(
                new ByteArrayInputStream("abc".getBytes()));
        part0.setContent(partBlob0);
        upload.getParts().add(part0);

        HDocumentUploadPart part1 = new HDocumentUploadPart();
        when(partBlob1.getBinaryStream()).thenReturn(
                new ByteArrayInputStream("def".getBytes()));
        part1.setContent(partBlob1);
        upload.getParts().add(part1);
        return upload;
    }

    // TODO damason: test mismatched hash when persisting temp file
}
