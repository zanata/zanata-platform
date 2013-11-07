package org.zanata.file;

import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.Mock;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;

public class DocumentUploadTest {

    protected static final GlobalDocumentId ANY_ID = new GlobalDocumentId(
            "myproject", "myversion", "mydoc");
    protected static final DocumentFileUploadForm ANY_UPLOAD_FORM =
            new DocumentFileUploadForm();

    protected SeamAutowire seam = SeamAutowire.instance();

    @Mock
    protected DocumentDAO documentDAO;
    @Mock
    protected DocumentUploadDAO documentUploadDAO;
    @Mock
    protected ZanataIdentity identity;
    @Mock
    protected ProjectIterationDAO projectIterationDAO;

    @Mock
    protected HProject project;
    @Mock
    protected HProjectIteration projectIteration;

    protected MockConfig conf;
    protected Response response;

    /**
     * @return builder with default valid set of upload parameters for a valid
     *         4-character plain text document that does not yet exist in the
     *         server.
     */
    protected static MockConfig.Builder defaultUpload() {
        MockConfig.Builder builder = new MockConfig.Builder();
        builder.setSimpleUpload();
        return builder;
    }

    protected void mockVersionDoesNotExist() {
        when(projectIterationDAO.getBySlug(conf.projectSlug, conf.versionSlug))
                .thenReturn(null);
    }

    protected void mockProjectAndVersionStatus() {
        when(projectIterationDAO.getBySlug(conf.projectSlug, conf.versionSlug))
                .thenReturn(projectIteration);
        when(projectIteration.getProject()).thenReturn(project);
        when(project.getStatus()).thenReturn(conf.projectStatus);
        when(projectIteration.getStatus()).thenReturn(conf.versionStatus);
    }

    protected void mockLoggedIn() {
        when(identity.isLoggedIn()).thenReturn(true);
    }

    protected void mockNotLoggedIn() {
        when(identity.isLoggedIn()).thenReturn(false);
    }

    protected ChunkUploadResponse responseEntity() {
        return (ChunkUploadResponse) response.getEntity();
    }

    protected void assertResponseHasStatus(Status errorStatus) {
        assertThat(fromStatusCode(response.getStatus()), is(errorStatus));
    }

    protected void assertResponseHasErrorMessage(String errorMessage) {
        assertThat(responseEntity().getErrorMessage(), is(errorMessage));
    }

    protected void assertUploadTerminated() {
        assertThat(responseEntity().getAcceptedChunks(), is(0));
        assertThat(responseEntity().isExpectingMore(), is(false));
    }

    /**
     * Simplifies setup of mock scenarios that differ slightly from a standard
     * scenario.
     *
     * Exposes immutable fields to be used when mocking so that it is easy to
     * use the same values when verifying.
     *
     */
    protected static class MockConfig {
        public final String fileType;
        public final boolean first, last;
        public final Long uploadId;
        public final long size;
        public final InputStream fileStream;
        public final String hash;
        public final String params, storedParams;

        public final DocumentFileUploadForm uploadForm;

        public final GlobalDocumentId id;
        public final String projectSlug, versionSlug, docId;
        public final EntityStatus projectStatus, versionStatus;

        public HDocument existingDocument;

        public final boolean hasImportTemplatePermission,
                plaintextAdapterAvailable;

        private MockConfig(Builder builder) {
            id =
                    new GlobalDocumentId(builder.projectSlug,
                            builder.versionSlug, builder.docId);
            projectSlug = builder.projectSlug;
            versionSlug = builder.versionSlug;
            docId = builder.docId;
            projectStatus = builder.projectStatus;
            versionStatus = builder.versionStatus;

            fileType = builder.fileType;
            first = builder.first;
            last = builder.last;
            uploadId = builder.uploadId;
            size = builder.size;
            fileStream = builder.fileStream;
            hash = builder.hash;
            params = builder.params;
            storedParams = builder.storedParams;

            uploadForm = new DocumentFileUploadForm();
            uploadForm.setFileType(fileType);
            uploadForm.setFirst(first);
            uploadForm.setLast(last);
            uploadForm.setUploadId(uploadId);
            uploadForm.setSize(size);
            uploadForm.setFileStream(fileStream);
            uploadForm.setHash(hash);
            uploadForm.setAdapterParams(params);

            existingDocument = builder.existingDocument;

            hasImportTemplatePermission = builder.hasImportTemplatePermission;
            plaintextAdapterAvailable = builder.plaintextAdapterAvailable;
        }

        protected static class Builder {
            private static final String basicDocumentContent = "test";
            private static final String hashOfBasicDocumentContent =
                    "d41d8cd98f00b204e9800998ecf8427e";

            private String projectSlug, versionSlug, docId;
            private EntityStatus projectStatus, versionStatus;
            private String fileType;
            private boolean first, last;
            private Long uploadId;
            private long size;
            private InputStream fileStream;
            private String hash;
            private String params, storedParams;
            public HDocument existingDocument;
            private boolean hasImportTemplatePermission,
                    plaintextAdapterAvailable;

            public Builder projectSlug(String projectSlug) {
                this.projectSlug = projectSlug;
                return this;
            }

            public Builder versionSlug(String versionSlug) {
                this.versionSlug = versionSlug;
                return this;
            }

            public Builder docId(String docId) {
                this.docId = docId;
                return this;
            }

            public Builder projectStatus(EntityStatus projectStatus) {
                this.projectStatus = projectStatus;
                return this;
            }

            public Builder versionStatus(EntityStatus versionStatus) {
                this.versionStatus = versionStatus;
                return this;
            }

            public Builder fileType(String fileType) {
                this.fileType = fileType;
                return this;
            }

            public Builder first(boolean first) {
                this.first = first;
                return this;
            }

            public Builder last(boolean last) {
                this.last = last;
                return this;
            }

            public Builder uploadId(Long uploadId) {
                this.uploadId = uploadId;
                return this;
            }

            public Builder size(long size) {
                this.size = size;
                return this;
            }

            public Builder fileStream(InputStream fileStream) {
                this.fileStream = fileStream;
                return this;
            }

            public Builder hash(String hash) {
                this.hash = hash;
                return this;
            }

            public Builder params(String params) {
                this.params = params;
                return this;
            }

            public Builder storedParams(String storedParams) {
                this.storedParams = storedParams;
                return this;
            }

            public Builder existingDocument(HDocument document) {
                this.existingDocument = document;
                return this;
            }

            public Builder hasImportTemplatePermission(boolean hasPermission) {
                this.hasImportTemplatePermission = hasPermission;
                return this;
            }

            public Builder plaintextAdapterAvailable(boolean available) {
                this.plaintextAdapterAvailable = available;
                return this;
            }

            public Builder setSimpleUpload() {
                projectSlug = "myproject";
                versionSlug = "myversion";
                docId = "mydoc";
                projectStatus = EntityStatus.ACTIVE;
                versionStatus = EntityStatus.ACTIVE;

                fileType("txt");
                first = true;
                last = true;
                size = 4L;
                fileStream =
                        new ByteArrayInputStream(
                                basicDocumentContent.getBytes());
                hash = hashOfBasicDocumentContent;
                params = "params";
                storedParams = "stored params";

                existingDocument = null;

                hasImportTemplatePermission = true;
                plaintextAdapterAvailable = true;

                return this;
            }

            /**
             * Set up mocks based on configured values.
             */
            public MockConfig build() {
                return new MockConfig(this);
            }

        }
    }

}
