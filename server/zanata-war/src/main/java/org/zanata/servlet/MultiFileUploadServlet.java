/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zanata.common.DocumentType;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.UserFileUploadTracker;
import org.zanata.model.HAccount;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.util.FileUtil;
import org.zanata.util.ServiceLocator;
import static com.google.common.base.Strings.emptyToNull;

/**
 * Endpoint for upload dialogs using multi-file upload forms.
 *
 * Use GET on the endpoint to check that upload is acceptable, including whether
 * the user is signed in and whether they already have an upload in-progress in
 * a separate tab.
 */
public class MultiFileUploadServlet extends HttpServlet {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MultiFileUploadServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig config) {
    }

    /**
     * Use GET to check the endpoint before doing a POST.
     *
     * This allows the browser to check that the upload is allowed before
     * attempting an upload.
     */
    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        respondWithUploadAvailability(response);
    }

    /**
     * Indicate in response any errors that would prevent upload to this
     * endpoint.
     *
     * @param response
     *            to respond with error or success JSON
     */
    private void respondWithUploadAvailability(HttpServletResponse response)
            throws IOException {
        Optional<String> reason = getCannotUploadReason();
        String responseBody =
                reason.isPresent() ? "{ \"error\": \"" + reason.get() + "\" }"
                        : "{ \"success\": \"ok to upload\" }";
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(responseBody);
        writer.close();
    }

    /**
     * @return the reason that the user cannot upload at this time. Reason will
     *         be absent if the user is able to upload.
     */
    private Optional<String> getCannotUploadReason() {
        Optional<Long> accountId = getAccountId();
        boolean loggedIn = accountId.isPresent();
        if (loggedIn) {
            UserFileUploadTracker tracker = ServiceLocator.instance()
                    .getInstance(UserFileUploadTracker.class);
            boolean alreadyUploading = tracker.isUserUploading(accountId.get());
            if (alreadyUploading) {
                return Optional.of("already uploading");
            }
        } else {
            return Optional.of("not logged in");
        }
        return Optional.absent();
    }
    // FIXME consolidate to one Optional

    private Optional<Long> getAccountId() {
        java.util.Optional<HAccount> optionalAuthenticatedAccount =
                ServiceLocator.instance().getOptionalInstance(HAccount.class,
                        new AuthenticatedLiteral());
        if (optionalAuthenticatedAccount.isPresent()) {
            return Optional.of(optionalAuthenticatedAccount.get().getId());
        }
        return Optional.absent();
    }

    @Override
    protected void doPost(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        processPost(request, response);
    }

    /**
     * Ensure the request is a Multipart request.
     *
     * Initiates processing if the request is multipart, otherwise respond with
     * an error.
     */
    private void processPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (ServletFileUpload.isMultipartContent(request)) {
            registerForUploadAndProcessMultipartPost(request, response);
        } else {
            log.error("File upload received non-multipart request");
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported request type. File upload supports only multipart requests.");
        }
    }

    /**
     * Ensure that upload is allowed at this time, then initiate processing of
     * the upload request.
     *
     * This method is responsible for making sure that a user only has one
     * active upload at a time.
     */
    private void registerForUploadAndProcessMultipartPost(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // TODO at this point the server will expect a response per-file. The
        // simplistic error responses may not be handled well
        Optional<Long> accountId = getAccountId();
        if (accountId.isPresent()) {
            UserFileUploadTracker tracker = ServiceLocator.instance()
                    .getInstance(UserFileUploadTracker.class);
            boolean registeredForUpload =
                    tracker.tryToRegisterUserForFileUpload(accountId.get());
            if (!registeredForUpload) {
                log.error("User with id {} is already uploading something.",
                        accountId.get());
                respondWithError(response, "already uploading");
            } else {
                try {
                    processMultipartPost(request, response);
                } finally {
                    tracker.deRegisterUserForFileUpload(accountId.get());
                }
            }
        } else {
            log.error("User attempted upload when not logged in.");
            respondWithError(response, "not logged in");
        }
    }

    private void respondWithError(HttpServletResponse response, String error)
            throws IOException {
        JSONObject responseObject = new JSONObject();
        try {
            responseObject.put("error", "upload failed: " + error);
        } catch (JSONException e) {
            log.error("Error while generating JSON", e);
        }
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(responseObject.toString());
        writer.close();
    }

    private void processMultipartPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        FileUploadRequestHandler uploadRequestHandler =
                new FileUploadRequestHandler(request);
        JSONArray filesJson;
        try {
            filesJson = uploadRequestHandler.process();
        } catch (FileUploadException e) {
            respondWithError(response, "upload failed: " + e.getMessage());
            return;
        }
        respondWithFiles(response, filesJson);
    }

    private void respondWithFiles(HttpServletResponse response,
            JSONArray filesJson) throws IOException {
        JSONObject responseObject = new JSONObject();
        try {
            responseObject.put("files", filesJson);
        } catch (JSONException e) {
            log.error("error adding files list to JSON", e);
        }
        String responseString = responseObject.toString();
        log.info("response string: " + responseString);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(responseString);
        writer.close();
    }

    private class FileUploadRequestHandler {
        private final HttpServletRequest request;
        private String projectSlug;
        private String versionSlug;
        private final List<String> fileTypes;
        private String path = "";
        private String lang = "en-US";
        private String fileParams = "";
        private SourceDocumentUpload sourceUploader;

        public FileUploadRequestHandler(HttpServletRequest request) {
            this.request = request;
            projectSlug = request.getParameter("p");
            versionSlug = request.getParameter("v");
            /**
             * TODO: add types parameter in all caller of /files/upload
             * (multifile upload)
             * https://bugzilla.redhat.com/show_bug.cgi?id=1217671
             */
            String fileTypesQuery = request.getParameter("types");
            if (StringUtils.isNotEmpty(fileTypesQuery)) {
                fileTypes = Lists.newArrayList(fileTypesQuery.split(","));
            } else {
                fileTypes = Collections.emptyList();
            }
            sourceUploader = ServiceLocator.instance()
                    .getInstance(SourceDocumentUpload.class);
        }

        public JSONArray process() throws FileUploadException {
            List<FileItem> items = getRequestItems();
            JSONArray filesJson = processFilesFromItems(items);
            return filesJson;
        }

        private List<FileItem> getRequestItems() throws FileUploadException {
            // Create a factory for disk-based file items
            List<FileItem> items;
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload uploadHandler = new ServletFileUpload(factory);
            items = uploadHandler.parseRequest(request);
            return items;
        }

        private JSONArray processFilesFromItems(List<FileItem> items) {
            // parameters are required before processing files
            recordParametersFromItems(items);
            JSONArray filesJson = new JSONArray();
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    JSONObject jsono = processFileItem(item);
                    filesJson.put(jsono);
                }
            }
            return filesJson;
        }

        /**
         * Must be called before processing any file items.
         *
         * @param items
         *            all items from multipart request.
         */

        private void recordParametersFromItems(List<FileItem> items) {
            // Make sure params are available before processing files
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String field = item.getFieldName();
                    String value = item.getString();
                    if (field.equals("filepath")) {
                        this.path = value;
                    } else if (field.equals("filelang")) {
                        this.lang = value;
                    } else if (field.equals("fileparams")) {
                        this.fileParams = value;
                    }
                }
            }
        }

        /**
         * Process upload of a single file item from a multipart request.
         *
         * @return JSON summary of outcome of the attempt.
         */

        private JSONObject processFileItem(FileItem item) {
            String docId = FileUtil.generateDocId(path, item.getName());
            GlobalDocumentId id =
                    new GlobalDocumentId(projectSlug, versionSlug, docId);
            Optional<String> errorMessage;
            Optional<String> successMessage = Optional.absent();
            Optional<String> concurrentUploadError = Optional
                    .of("failed: someone else is already uploading this file");
            try {
                DocumentFileUploadForm form = createUploadFormForItem(item);
                Response response =
                        sourceUploader.tryUploadSourceFileWithoutHash(id, form);
                ChunkUploadResponse responseEntity =
                        (ChunkUploadResponse) response.getEntity();
                errorMessage = optionalStringEmptyIsAbsent(
                        responseEntity.getErrorMessage());
                successMessage = optionalStringEmptyIsAbsent(
                        responseEntity.getSuccessMessage());
            } catch (IOException e) {
                errorMessage = Optional.of("could not access file data");
            } catch (OptimisticLockException e) {
                errorMessage = concurrentUploadError;
            } catch (StaleStateException e) {
                // this happens in the same circumstances as
                // OptimisticLockException
                // but is thrown because we are using hibernate directly rather
                // than
                // through JPA.
                errorMessage = concurrentUploadError;
            } catch (ConstraintViolationException e) {
                errorMessage = concurrentUploadError;
            } catch (PersistenceException e) {
                errorMessage = Optional.of(
                        "Timed out: failed because the file took too long to process");
            }
            return createJSONInfo(item, docId, errorMessage, successMessage);
        }

        /**
         * Create JSON summary of outcome of an attempt to process an item.
         */

        private JSONObject createJSONInfo(FileItem item, String docId,
                Optional<String> error, Optional<String> success) {
            JSONObject jsono = new JSONObject();
            try {
                jsono.put("name", docId);
                jsono.put("size", item.getSize());
                if (error.isPresent()) {
                    if (error.get().equals(
                            "Valid combination of username and api-key for this server were not included in the request.")) {
                        error = Optional.of("not logged in");
                    }
                    jsono.put("error", error.get());
                } else {
                    if (success.isPresent()) {
                        jsono.put("message", success.get());
                    }
                    // TODO could provide REST URL for this file
                    // jsono.put("url", "upload?getfile=" + item.getName());
                }
            } catch (JSONException e) {
                log.error("Error while generating JSON", e);
            }
            return jsono;
        }

        /**
         * Create the upload form required by sourceUploader for file upload.
         *
         * @throws IOException
         *             if the input stream cannot be opened for the file data.
         */

        private DocumentFileUploadForm createUploadFormForItem(FileItem item)
                throws IOException {
            DocumentFileUploadForm form = new DocumentFileUploadForm();
            form.setAdapterParams(fileParams);
            form.setFirst(true);
            form.setLast(true);
            form.setSize(item.getSize());
            form.setFileType(getFileTypeForItem(item.getName()));
            form.setFileStream(item.getInputStream());
            return form;
        }

        private String getFileTypeForItem(String filename) {
            String extension = FilenameUtils.getExtension(filename);
            /**
             * TODO: Implement docType selection for multifile upload. At the
             * moment, get the first docType from returned list.
             */
            DocumentType fileType = DocumentType.fromSourceExtension(extension)
                    .iterator().next();
            if (!fileTypes.isEmpty()) {
                for (String parsedFileType : fileTypes) {
                    DocumentType docType =
                            DocumentType.getByName(parsedFileType);
                    if (docType != null && docType.getSourceExtensions()
                            .contains(extension)) {
                        fileType = docType;
                        break;
                    }
                }
            }
            return fileType == null ? extension : fileType.name();
        }

        /**
         * @return absent if the given String is null or empty, otherwise an
         *         Option containing the given String.
         */

        private Optional<String> optionalStringEmptyIsAbsent(String value) {
            return (Optional.fromNullable(emptyToNull(value)));
        }
    }
}
