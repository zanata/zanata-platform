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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.Messages;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.commands.push.RawPushStrategy.TranslationFilesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.client.exceptions.InvalidUserInputException;
import org.zanata.client.util.ConsoleUtils;
import org.zanata.common.DocumentType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.ChunkUploadResponse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode;

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

    private static final Pattern fileNameExtensionsPattern = Pattern.compile(
        "(?:([^\\[]*)?(?:\\[(.*?)\\])?)");

    private final ConsoleInteractor consoleInteractor;

    private FileResourceClient client;

    public RawPushCommand(PushOptions opts) {
        super(opts);
        client = getClientFactory().getFileResourceClient();
        consoleInteractor = new ConsoleInteractorImpl(opts);
    }

    public RawPushCommand(PushOptions opts, RestClientFactory clientFactory) {
        super(opts, clientFactory);
        client = getClientFactory().getFileResourceClient();
        consoleInteractor = new ConsoleInteractorImpl(opts);
    }

    public RawPushCommand(PushOptions opts, RestClientFactory clientFactory,
        ConsoleInteractor console) {
        super(opts, clientFactory);
        client = getClientFactory().getFileResourceClient();
        this.consoleInteractor = console;
    }

    /**
     * Extract extensions from input string
     */
    public List<String> extractExtensions(String typeWithExtension) {
        Matcher matcher = fileNameExtensionsPattern.matcher(typeWithExtension);
        if (matcher.find()) {
            String rawExtensions = matcher.group(2);
            if (!StringUtils.isEmpty(rawExtensions)) {
                return Arrays.asList(rawExtensions.split(";"));
            }
        }
        return Collections.emptyList();
    }

    /**
     * Extract fileType from input string
     * input pattern: fileType[extension;extension], [extension;extension]
     *
     * @param typeWithExtension
     * @return result[0] - type, result[1] - extensions. e.g [txt,html]
     */
    public @Nullable String extractType(String typeWithExtension) {
        Matcher matcher = fileNameExtensionsPattern.matcher(typeWithExtension);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Validate inputFileType to make sure it's not empty
     *
     * @param inputFileType
     * @param inputExtensions
     * @param acceptedTypes
     */
    private void validateInputFileType(String inputFileType,
            List<String> inputExtensions, List<DocumentType> acceptedTypes) {
        if (StringUtils.isNotBlank(inputFileType)) {
            return;
        }
        if (inputExtensions.isEmpty()) {
            //throw error if inputFileType and inputExtensions is empty
            throw new InvalidUserInputException(
                    "Invalid expression for '--file-types' option");
        } else {
            //suggest --file-types options for this extension
            for (DocumentType docType: acceptedTypes) {
                for (String extension: docType.getSourceExtensions()) {
                    if (inputExtensions.contains(extension)) {
                        String msg =
                                Messages.format(
                                        "file.type.suggestFromExtension",
                                        docType,
                            extension, docType);
                        throw new InvalidUserInputException(msg);
                    }
                }
            }
        }
    }

    /**
     * Return map of validated type with set of extensions
     *
     * Validate user input file types against server accepted file types
     *
     * Valid input - properties_utf8,properties[txt],plain_text[md;markdown]
     *
     * - Each file type is only input once - e.g.
     *    - valid: "html,properties,txt"
     *    - invalid: "html,properties,html"
     * - Same file extension cannot be in multiple file type - e.g. plain_text[txt],properties[txt]
     *
     * @param acceptedTypes
     * @param inputFileTypes
     */
    public Map<DocumentType, Set<String>> validateFileTypes(
            List<DocumentType> acceptedTypes, List<String> inputFileTypes) {

        Map<DocumentType, Set<String>> filteredFileTypes = new HashMap<>();

        for (String typeWithExtension : inputFileTypes) {
            String fileType = extractType(typeWithExtension);
            List<String> extensions = extractExtensions(typeWithExtension);

            validateInputFileType(fileType, extensions, acceptedTypes);

            DocumentType inputFileType = DocumentType.getByName(fileType);

            //skip file type if its not supported
            if (inputFileType == null) {
                String msg = Messages.format("file.type.typeNotSupported", fileType);
                consoleInteractor.printfln(DisplayMode.Warning, msg);
                continue;
            }

            //throw error if file type is input more than once
            if (filteredFileTypes.containsKey(inputFileType)) {
                String msg =
                        Messages.format("file.type.duplicateFileType", fileType);
                log.error(msg);
                throw new RuntimeException(msg);
            }

            Set<String> filteredExtensions = new HashSet<>(extensions);

            /**
             * Use the extensions from typeWithExtension input if exists,
             * otherwise, use the extensions from server.
             */
            filteredExtensions =
                filteredExtensions.isEmpty() ? inputFileType.getSourceExtensions()
                            : filteredExtensions;

            //throw error if same file extension found in multiple file type
            for (Map.Entry<DocumentType, Set<String>> entry: filteredFileTypes.entrySet()) {
                for (String filteredExtension: entry.getValue()) {
                    if (filteredExtensions.contains(filteredExtension)) {
                        String msg =
                            Messages.format(
                                "file.type.conflictExtension",
                                fileType, entry.getKey().name(),
                                filteredExtension);
                        log.error(msg);
                        throw new RuntimeException(msg);
                    }
                }
            }
            filteredFileTypes.put(inputFileType, filteredExtensions);
        }
        return filteredFileTypes;
    }

    @Override
    public void run() throws IOException {
        PushCommand.logOptions(log, getOpts());

        consoleInteractor.printfln(DisplayMode.Warning,
            "Using EXPERIMENTAL project type 'file'.");

        // only supporting single module for now

        File sourceDir = getOpts().getSrcDir();
        if (!sourceDir.exists()) {
            boolean enableModules = getOpts().getEnableModules();
            // TODO remove when modules implemented
            if (enableModules) {
                consoleInteractor
                        .printfln(DisplayMode.Warning,
                            "enableModules=true but multi-modules not yet supported for this command. Using single module push.");
            }

            throw new RuntimeException("directory '" + sourceDir
                    + "' does not exist - check "
                    + getOpts().getSrcDirParameterName() + " option");
        }

        RawPushStrategy strat = new RawPushStrategy();
        strat.setPushOptions(getOpts());

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<DocumentType> serverAcceptedTypes = client.acceptedFileTypes();

        Map<DocumentType, Set<String>> filteredDocTypes =
            validateFileTypes(serverAcceptedTypes, getOpts().getFileTypes());

        if (filteredDocTypes.isEmpty()) {
            log.info("no valid types specified; nothing to do");
            return;
        }

        ImmutableList.Builder<String> sourceFileExtensionsBuilder = ImmutableList.builder();
        for (Set<String> filteredSourceExtensions : filteredDocTypes.values()) {
            sourceFileExtensionsBuilder.addAll(filteredSourceExtensions);
        }

        String[] srcFiles =
                strat.getSrcFiles(sourceDir, getOpts().getIncludes(), getOpts()
                        .getExcludes(), sourceFileExtensionsBuilder.build(), true, getOpts()
                        .getCaseSensitive());

        SortedSet<String> localDocNames =
                new TreeSet<String>(Arrays.asList(srcFiles));

        // TODO handle obsolete document deletion
        consoleInteractor
                .printfln(DisplayMode.Warning,
                    "Obsolete document removal is not yet implemented, no documents will be removed from the server.");

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
            consoleInteractor.printfln("Found source documents:");
            for (String docName : localDocNames) {
                if (docsToPush.contains(docName)) {
                    DocumentType fileType = getFileType(filteredDocTypes,
                        FilenameUtils.getExtension(docName));
                    consoleInteractor.printfln("           "
                            + Messages.format("push.info.documentToPush",
                                    docName, fileType.name()));
                } else {
                    consoleInteractor.printfln(
                        Messages.format("push.info.skipDocument", docName));
                }
            }
        }

        if (getOpts().getPushType() == PushPullType.Trans
                || getOpts().getPushType() == PushPullType.Both) {
            if (getOpts().getLocaleMapList() == null)
                throw new ConfigException("pushType set to '"
                        + getOpts().getPushType()
                        + "', but project has no locales configured");
            consoleInteractor.printfln(DisplayMode.Warning, Messages.format(
                    "push.warn.overrideTranslations", getOpts().getPushType()));

            if (getOpts().getPushType() == PushPullType.Both) {
                confirmWithUser("This will overwrite existing documents AND TRANSLATIONS on the server.\n");
                // , and delete obsolete documents.\n");
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
                final String srcExtension = FilenameUtils.getExtension(localDocName);
                final DocumentType fileType = getFileType(filteredDocTypes,
                    srcExtension);
                final String qualifiedDocName = qualifiedDocName(localDocName);
                if (getOpts().getPushType() == PushPullType.Source
                        || getOpts().getPushType() == PushPullType.Both) {
                    if (!getOpts().isDryRun()) {
                        boolean sourcePushed =
                                pushSourceDocumentToServer(sourceDir,
                                        localDocName, qualifiedDocName,
                                        fileType.name());
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

                    Optional translationFileExtension =
                            getTranslationFileExtension(fileType, srcExtension);

                    strat.visitTranslationFiles(localDocName,
                            new TranslationFilesVisitor() {

                                @Override
                                public void visit(LocaleMapping locale,
                                        File translatedDoc) {
                                    log.info("pushing {} translation of {}",
                                            locale.getLocale(),
                                            qualifiedDocName);
                                    pushDocumentToServer(qualifiedDocName,
                                            fileType.name(), locale.getLocale(),
                                            translatedDoc);
                                }
                            }, translationFileExtension);
                }
            } catch (IOException | RuntimeException e) {
                log.error(
                        "Operation failed: " + e.getMessage() + "\n\n"
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
     * Get translation file extension in given docType with the srcExtension.
     * If no extension found, return source file extension.
     *
     * @param docType
     * @param srcExtension
     */
    private Optional<String> getTranslationFileExtension(DocumentType docType,
            String srcExtension) {
        String transFileExtension = docType.getExtensions().get(srcExtension);

        return StringUtils.isEmpty(transFileExtension) ? Optional
                .of(srcExtension) : Optional.of(transFileExtension);
    }

    /**
     * Search and return file type with given source file extension
     *
     * @param fileTypes
     * @param srcExtension
     */
    private @Nullable DocumentType getFileType(Map<DocumentType, Set<String>> fileTypes,
            String srcExtension) {
        for (Map.Entry<DocumentType, Set<String>> entry : fileTypes.entrySet()) {

            if (entry.getValue().contains(srcExtension)) {
                return entry.getKey();
            }
        }
        return null;
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
                    uploadDocumentPart(docId, locale, uploadForm);
                }
            } else {
                try (StreamChunker chunker = new StreamChunker(docFile,
                        getOpts().getChunkSize())) {
                    log.info("    transmitting file [{}] as {} chunks",
                            docFile.getAbsolutePath(), chunker.totalChunks());
                    ChunkUploadResponse uploadResponse;
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
                        if (isFirst) {
                            uploadId = uploadResponse.getUploadId();
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

    private ChunkUploadResponse uploadDocumentPart(
            String docName, String locale, DocumentFileUploadForm uploadForm) {
        ConsoleUtils.startProgressFeedback();
        ChunkUploadResponse response;
        if (locale == null) {
            response =
                    client.uploadSourceFile(getOpts().getProj(),
                            getOpts().getProjectVersion(), docName, uploadForm);
        } else {
            response =
                    client.uploadTranslationFile(getOpts().getProj(),
                            getOpts().getProjectVersion(), locale, docName,
                            getOpts().getMergeType(), uploadForm);
        }
        log.debug("response from server: {}", response);
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
                    if (hasNext()) {
                        return getNextChunk();
                    } else {
                        throw new NoSuchElementException(
                                "getNextChunk() must not be called after all chunks have been retrieved");
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
