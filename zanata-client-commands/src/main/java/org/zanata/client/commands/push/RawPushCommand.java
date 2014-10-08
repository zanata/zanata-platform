/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands.push;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Hex;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.push.RawPushStrategy.TranslationFilesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.client.util.ConsoleUtils;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.IFileResource;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.ChunkUploadResponse;

/**
 * Command to send files directly to the server without parsing on the client.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPushCommand extends PushPullCommand<PushOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PushCommand.class);

    protected final IFileResource fileResource;

    public RawPushCommand(PushOptions opts) {
        super(opts);
        this.fileResource = getRequestFactory().getFileResource();
    }

    public RawPushCommand(PushOptions opts, ZanataProxyFactory factory,
            ISourceDocResource sourceDocResource,
            ITranslatedDocResource translationResources, URI uri) {
        super(opts, factory, sourceDocResource, translationResources, uri);
        this.fileResource = factory.getFileResource();
    }

    @Override
    public void run() throws IOException {
        PushCommand.logOptions(log, getOpts());
        log.warn("Using EXPERIMENTAL project type 'file'.");

        // only supporting single module for now

        File sourceDir = getOpts().getSrcDir();
        if (!sourceDir.exists()) {
            boolean enableModules = getOpts().getEnableModules();
            // TODO remove when modules implemented
            if (enableModules) {
                log.warn("enableModules=true but multi-modules not yet supported for this command. Using single module push.");
            }

            throw new RuntimeException("directory '" + sourceDir
                    + "' does not exist - check "
                    + getOpts().getSrcDirParameterName() + " option");
        }

        RawPushStrategy strat = new RawPushStrategy();
        strat.setPushOptions(getOpts());

        ImmutableList.Builder<String> builder = ImmutableList.builder();

        ClientResponse<String> response = fileResource.acceptedFileTypes();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        StringSet serverAcceptedTypes = new StringSet(response.getEntity());
        for (String type : getOpts().getFileTypes()) {
            if (serverAcceptedTypes.contains(type)) {
                builder.add(type);
            } else {
                log.warn(
                        "Requested type '{}' is not supported by the target server and will be ignored.",
                        type);
            }
        }

        ImmutableList<String> types = builder.build();

        if (types.isEmpty()) {
            log.info("no valid types specified; nothing to do");
            return;
        }

        String[] srcFiles =
                strat.getSrcFiles(sourceDir, getOpts().getIncludes(), getOpts()
                        .getExcludes(), types, true, getOpts()
                        .getCaseSensitive());

        SortedSet<String> localDocNames =
                new TreeSet<String>(Arrays.asList(srcFiles));

        // TODO handle obsolete document deletion
        log.warn("Obsolete document removal is not yet implemented, no documents will be removed from the server.");

        SortedSet<String> docsToPush = localDocNames;
        if (getOpts().getFromDoc() != null) {
            if (!localDocNames.contains(getOpts().getFromDoc())) {
                log.error(
                        "Document with id {} not found, unable to start push from unknown document. Aborting.",
                        getOpts().getFromDoc());
                // FIXME should this be throwing an exception to properly abort?
                // need to see behaviour with modules
                return;
            }
            docsToPush = localDocNames.tailSet(getOpts().getFromDoc());
            int numSkippedDocs = localDocNames.size() - docsToPush.size();
            log.info("Skipping {} document(s) before {}.", numSkippedDocs,
                    getOpts().getFromDoc());
        }

        if (docsToPush.isEmpty()) {
            log.info("no documents in module: {}; nothing to do", getOpts()
                    .getCurrentModule());
            return;
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

        if (getOpts().getPushType() == PushPullType.Trans
                || getOpts().getPushType() == PushPullType.Both) {
            if (getOpts().getLocaleMapList() == null)
                throw new ConfigException("pushType set to '"
                        + getOpts().getPushType()
                        + "', but zanata.xml contains no <locales>");
            log.warn("pushType set to '"
                    + getOpts().getPushType()
                    + "': existing translations on server may be overwritten/deleted");

            if (getOpts().getPushType() == PushPullType.Both) {
                confirmWithUser("This will overwrite existing documents AND TRANSLATIONS on the server.\n"); // ,
                                                                                                             // and
                                                                                                             // delete
                                                                                                             // obsolete
                                                                                                             // documents.\n");
            } else if (getOpts().getPushType() == PushPullType.Trans) {
                confirmWithUser("This will overwrite existing TRANSLATIONS on the server.\n");
            }
        } else {
            // confirmWithUser("This will overwrite existing documents on the server, and delete obsolete documents.\n");
            confirmWithUser("This will overwrite existing documents on the server.\n");
        }

        boolean hasErrors = false;

        for (final String localDocName : docsToPush) {
            try {
                final String fileType = getExtensionFor(localDocName);
                final String qualifiedDocName = qualifiedDocName(localDocName);
                boolean sourcePushed;
                if (getOpts().getPushType() == PushPullType.Source
                        || getOpts().getPushType() == PushPullType.Both) {
                    if (!getOpts().isDryRun()) {
                        sourcePushed =
                                pushSourceDocumentToServer(sourceDir,
                                        localDocName, qualifiedDocName,
                                        fileType);
                        // ClientUtility.checkResult(putResponse, uri);
                        if (!sourcePushed) {
                            hasErrors = true;
                        }
                    } else {
                        log.info(
                                "pushing source doc [qualifiedname={}] to server (skipped due to dry run)",
                                qualifiedDocName);
                    }
                }

                if (getOpts().getPushType() == PushPullType.Trans
                        || getOpts().getPushType() == PushPullType.Both) {
                    strat.visitTranslationFiles(localDocName,
                            new TranslationFilesVisitor() {

                                @Override
                                public void visit(LocaleMapping locale,
                                        File translatedDoc) {
                                    log.info("pushing {} translation of {}",
                                            locale.getLocale(),
                                            qualifiedDocName);
                                    pushDocumentToServer(qualifiedDocName,
                                            fileType, locale.getLocale(),
                                            translatedDoc);
                                }
                            });
                }
            } catch (IOException | RuntimeException e) {
                log.error(
                        "Operation failed: "+e.getMessage()+"\n\n"
                        + "    To retry from the last document, please add the option: {}\n",
                        getOpts().buildFromDocArgument(localDocName));
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        if (hasErrors) {
            throw new RuntimeException(
                    "Push completed with errors, see log for details.");
        }

    }

    /**
     * @param localDocName
     * @return extension of document (all characters after final '.'), or null
     *         if no characters after a final . are found.
     */
    private String getExtensionFor(final String localDocName) {
        if (localDocName == null || localDocName.length() == 0
                || localDocName.endsWith(".") || !localDocName.contains(".")) {
            return null;
        }
        return localDocName.substring(localDocName.lastIndexOf('.') + 1);
    }

    /**
     *
     * @param sourceDir
     * @param localDocName
     * @param qualifiedDocName
     *            docName with added module prefix
     * @return true if the push was successful
     * @throws IOException
     */
    private boolean pushSourceDocumentToServer(File sourceDir,
            String localDocName, String qualifiedDocName, String fileType)
            throws IOException {
        log.info("pushing source document [{}] to server", qualifiedDocName);

        String locale = null;

        File srcFile = new File(sourceDir, localDocName);

        pushDocumentToServer(qualifiedDocName, fileType, locale, srcFile);
        return true;
    }

    /**
     * @param docId
     * @param fileType
     * @param locale
     * @param docFile
     */
    private void pushDocumentToServer(String docId, String fileType,
            String locale, File docFile) {
        try {
            String md5hash = calculateFileHash(docFile);
            if (docFile.length() <= getOpts().getChunkSize()) {
                log.info("    transmitting file [{}] as single chunk",
                        docFile.getAbsolutePath());
                try (InputStream fileStream = new FileInputStream(docFile)) {
                    DocumentFileUploadForm uploadForm =
                            generateUploadForm(true, true, fileType, md5hash,
                                    docFile.length(), fileStream);
                    ClientResponse<ChunkUploadResponse> response =
                            uploadDocumentPart(docId, locale, uploadForm);
                    checkChunkUploadStatus(response);
                }
            } else {
                try (StreamChunker chunker = new StreamChunker(docFile,
                        getOpts().getChunkSize())) {
                    log.info("    transmitting file [{}] as {} chunks",
                            docFile.getAbsolutePath(), chunker.totalChunks());
                    ClientResponse<ChunkUploadResponse> uploadResponse;
                    DocumentFileUploadForm uploadForm;
                    Long uploadId = null;

                    for (InputStream chunkStream : chunker) {
                        log.info("        pushing chunk {} of {}",
                                chunker.currentChunkNumber(),
                                chunker.totalChunks());
                        boolean isFirst = chunker.currentChunkNumber() == 1;
                        boolean isLast = chunker.getRemainingChunks() == 0;
                        long chunkSize = chunker.currentChunkSize();
                        uploadForm =
                                generateUploadForm(isFirst, isLast, fileType,
                                        md5hash,
                                        chunkSize, chunkStream);
                        if (!isFirst) {
                            uploadForm.setUploadId(uploadId);
                        }
                        uploadResponse =
                                uploadDocumentPart(docId, locale, uploadForm);
                        checkChunkUploadStatus(uploadResponse);
                        if (isFirst) {
                            uploadId = uploadResponse.getEntity().getUploadId();
                            if (uploadId == null) {
                                throw new RuntimeException(
                                        "server did not return upload id");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkChunkUploadStatus(
            ClientResponse<ChunkUploadResponse> uploadResponse) {
        if (uploadResponse.getStatus() >= 300) {
            throw new RuntimeException("Server returned error status: "
                    + uploadResponse.getStatus() + ". Error message: "
                    + uploadResponse.getEntity().getErrorMessage());
        }
    }

    private DocumentFileUploadForm generateUploadForm(boolean isFirst,
            boolean isLast, String fileType, String md5hash, long streamSize,
            InputStream fileStream) {
        DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
        uploadForm.setFirst(isFirst);
        uploadForm.setLast(isLast);
        uploadForm.setFileType(fileType);
        uploadForm.setHash(md5hash);
        uploadForm.setSize(streamSize);
        uploadForm.setFileStream(fileStream);
        return uploadForm;
    }

    private ClientResponse<ChunkUploadResponse> uploadDocumentPart(
            String docName, String locale, DocumentFileUploadForm uploadForm) {
        ConsoleUtils.startProgressFeedback();
        ClientResponse<ChunkUploadResponse> response;
        if (locale == null) {
            response =
                    fileResource.uploadSourceFile(getOpts().getProj(),
                            getOpts().getProjectVersion(), docName, uploadForm);
        } else {
            response =
                    fileResource.uploadTranslationFile(getOpts().getProj(),
                            getOpts().getProjectVersion(), locale, docName,
                            getOpts().getMergeType(), uploadForm);
        }
        log.debug("response from server: {}", response.getEntity());
        ConsoleUtils.endProgressFeedback();
        return response;
    }

    private String calculateFileHash(File srcFile) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileStream = new FileInputStream(srcFile);
            try {
                fileStream = new DigestInputStream(fileStream, md);
                byte[] buffer = new byte[256];
                while (fileStream.read(buffer) > 0) {
                    // continue
                }
            } finally {
                fileStream.close();
            }
            String md5hash = new String(Hex.encodeHex(md.digest()));
            return md5hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class StreamChunker implements Iterable<InputStream>,
            Closeable {
        private int totalChunkCount;
        private int chunksRetrieved;

        private File file;
        private byte[] buffer;
        private InputStream fileStream;
        private int actualChunkSize;

        public StreamChunker(File file, int chunkSize)
                throws FileNotFoundException {
            this.file = file;
            buffer = new byte[chunkSize];
            chunksRetrieved = 0;
            totalChunkCount =
                    (int) (file.length() / chunkSize + (file.length()
                            % chunkSize == 0 ? 0 : 1));
            fileStream = new FileInputStream(file);
        }

        @Override
        public void close() throws IOException {
            fileStream.close();
        }

        public int totalChunks() {
            return totalChunkCount;
        }

        public int currentChunkNumber() {
            return chunksRetrieved;
        }

        /**
         * Value is only valid after calling getNextChunk or Iterator.next().
         *
         * @return the size in bytes of the most recently returned chunk.
         */
        public int currentChunkSize() {
            return actualChunkSize;
        }

        public int getRemainingChunks() {
            return totalChunkCount - chunksRetrieved;
        }

        private InputStream getNextChunk() {
            if (chunksRetrieved == totalChunkCount) {
                throw new IllegalStateException(
                        "getNextChunk() must not be called after all chunks have been retrieved");
            }

            try {
                actualChunkSize = fileStream.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            chunksRetrieved++;
            if (chunksRetrieved == totalChunkCount) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    log.error(
                            "failed to close input stream for file "
                                    + file.getAbsolutePath(), e);
                }
                fileStream = null;
            }
            return new ByteArrayInputStream(buffer, 0, actualChunkSize);
        }

        @Override
        public Iterator<InputStream> iterator() {
            return new Iterator<InputStream>() {

                @Override
                public boolean hasNext() {
                    return chunksRetrieved < totalChunkCount;
                }

                @Override
                public InputStream next() {
                    return getNextChunk();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
