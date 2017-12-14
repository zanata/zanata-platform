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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ws.rs.client.ResponseProcessingException;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.zanata.common.FileTypeInfo;
import org.zanata.common.FileTypeName;
import org.zanata.common.ProjectType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.ChunkUploadResponse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode;
import static org.zanata.client.commands.StringUtil.multiline;

/**
 * Command to send files directly to the server without parsing on the client.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPushCommand extends PushPullCommand<PushOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(RawPushCommand.class);

    private static final Pattern fileTypeSpecPattern = Pattern.compile(multiline(
            "(?x)        # enable regex comments",
            "([^\\[]*)   # capture (1) everything before EOL or brackets (DocumentType)",
            "(?:         # optional non-capture group for any bracketed text",
            "\\[(.*)\\]  # capture (2) any filename extensions inside brackets (semicolon-separated)",
            ")?"
    ));

    private final ConsoleInteractor consoleInteractor;

    // TODO rename to fileResource or similar
    private final FileResourceClient client;

    public RawPushCommand(PushOptions opts) {
        super(opts);
        client = getClientFactory().getFileResourceClient();
        consoleInteractor = new ConsoleInteractorImpl(opts);
    }

    @VisibleForTesting
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
     * Extract extensions from input string as a Map (source, target). Note
     * that source and target will be the same in the current implementation, since
     * the command-line option doesn't yet allow target extensions to be specified.
     */
    @VisibleForTesting
    ImmutableMap<String, String> extractExtensions(String fileTypeSpec) {
        Matcher matcher = fileTypeSpecPattern.matcher(fileTypeSpec);
        if (matcher.find()) {
            String extensions = matcher.group(2);
            if (!StringUtils.isEmpty(extensions)) {
                ImmutableMap.Builder<String, String> builder =
                        ImmutableMap.builder();
                Arrays.asList(extensions.split(";")).forEach(
                        ext -> builder.put(ext, ext));
                return builder.build();
            }
        }
        return ImmutableMap.of();
    }

    /**
     * Extract fileType from input string
     * input pattern: fileType[extension;extension],fileType[extension;extension]
     *
     * @param fileTypeMappingSpec
     * @return an unvalidated FileTypeName if one was given, otherwise null
     */
    @VisibleForTesting
    @Nullable FileTypeName extractFileTypeName(String fileTypeMappingSpec) {
        Matcher matcher = fileTypeSpecPattern.matcher(fileTypeMappingSpec);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (StringUtils.isNotBlank(name)) {
                return new FileTypeName(name.toUpperCase());
            }
        }
        return null;
    }

    /**
     * Validate file extensions
     *  @param inputFileType
     * @param userExtensions
     * @param acceptedTypes
     */
    private void validateFileExtensions(@Nullable FileTypeName inputFileType,
            ImmutableMap<String, String> userExtensions, List<FileTypeInfo> acceptedTypes) {
        if (inputFileType != null) {
            return;
        }
        // if file type is missing but extensions are present, try to provide
        // a helpful error message
        if (userExtensions.isEmpty()) {
            //throw error if inputFileType and inputExtensions is empty
            throw new InvalidUserInputException(
                    "Invalid expression for '--file-types' option");
        } else {
            //suggest --file-types options for this extension
            for (FileTypeInfo docType: acceptedTypes) {
                for (String srcExt: docType.getSourceExtensions()) {
                    if (userExtensions.containsKey(srcExt)) {
                        String msg =
                                Messages.format(
                                        "file.type.suggestFromExtension",
                                        docType,
                                        srcExt, docType);
                        throw new InvalidUserInputException(msg);
                    }
                }
            }
            throw new InvalidUserInputException(
                    "Invalid expression for '--file-types' option");
        }
    }

    /**
     * Return map of validated DocumentType to set of corresponding extensions, by applying the user's
     * file type options to the server's accepted file types.
     *
     * Validate user input file types against server accepted file types
     *
     * Valid input - properties_utf8,properties[txt],plain_text[md;markdown]
     *
     * - Each file type must appear only once - e.g.
     *    - valid: "html,properties,txt"
     *    - invalid: "html,properties,html"
     * - Same file extension must not appear in multiple file types - e.g. plain_text[txt],properties[txt]
     * @param serverFileTypes
     * @param fileTypesSpec
     */
    @SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST"})
    public ImmutableList<FileTypeInfo> getActualFileTypes(
            List<FileTypeInfo> serverFileTypes, List<String> fileTypesSpec) {

        // cumulative list of activated types
        ImmutableList.Builder<FileTypeInfo> docTypeMappings =
                new ImmutableList.Builder<>();
        // types which have been specified by the user so far
        Set<FileTypeName> seenUserDocTypes = new HashSet<>();
        // extensions which have been specified by the user so far
        Set<String> seenUserExtensions = new HashSet<>();

        if (fileTypesSpec.isEmpty()) {
            return ImmutableList.of();
        }

        for (String fileTypeSpec : fileTypesSpec) {
            @Nullable FileTypeName userType = extractFileTypeName(fileTypeSpec);
            ImmutableMap<String, String> userExtensions;
            if (userType == null) {
                // try parameter as a list of file extensions: ZNTA-1248
                String[] exts = fileTypeSpec.split(",");
                ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
                for (String ext : exts) {
                    builder.put(ext, ext);
                }
                userExtensions = builder.build();
            } else {
                userExtensions = extractExtensions(fileTypeSpec);
            }

            validateFileExtensions(userType, userExtensions, serverFileTypes);
            assert userType != null;
            @Nullable FileTypeInfo fileTypeInfo = serverFileTypes.stream()
                    .filter((FileTypeInfo info) -> info.getType().equals(userType))
                    .findAny().orElse(null);

            // throw error if file type is not supported by server
            if (fileTypeInfo == null) {
                String msg = Messages.format(
                        "file.type.typeNotSupported", userType);
                throw new InvalidUserInputException(msg);
            }

            if (!seenUserDocTypes.add(userType)) {
                //throw error if file type is listed more than once
                String msg = Messages.format(
                        "file.type.duplicateFileType", userType);
                log.error(msg);
                throw new RuntimeException(msg);
            }
            for (String srcExt : userExtensions.keySet()) {
                //throw error if same file extension found in multiple file types
                if (!seenUserExtensions.add(srcExt)) {
                    String msg = Messages.format(
                            "file.type.conflictExtension", srcExt, userType);
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
            }

            // Use the extensions from docTypeMappingSpec if specified,
            // otherwise use the extensions from server.
            Map<String, String> filteredExtensions =
                    userExtensions.isEmpty() ? fileTypeInfo.getExtensions()
                            : userExtensions;
            docTypeMappings.add(new FileTypeInfo(userType, filteredExtensions));
        }
        return docTypeMappings.build();
    }

    private boolean pushSource() {
        return getOpts().getPushType() == PushPullType.Source
                || getOpts().getPushType() == PushPullType.Both;
    }

    private boolean pushTrans() {
        return getOpts().getPushType() == PushPullType.Trans
                || getOpts().getPushType() == PushPullType.Both;
    }

    @Override
    public void run() throws IOException {
        PushCommand.logOptions(log, getOpts());

        consoleInteractor.printfln(DisplayMode.Warning,
                "Using EXPERIMENTAL project type 'file'.");

        List<FileTypeInfo> serverAcceptedTypes = fileTypeInfoList(client);

        if (getOpts().getListFileTypes()) {
            printFileTypes(serverAcceptedTypes);
            return;
        }

        if (!pushSource() && !pushTrans()) {
            throw new RuntimeException("Invalid option for push type");
        }
        // only supporting single module for now

        File sourceDir = getOpts().getSrcDir();
        if (!sourceDir.exists()) {
            boolean enableModules = getOpts().getEnableModules();
            // TODO(files) remove warning when modules supported
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

        ImmutableList<FileTypeInfo> actualFileTypes =
                getActualFileTypes(serverAcceptedTypes, getOpts().getFileTypes());

        if (actualFileTypes.isEmpty()) {
            log.info("no valid types specified; nothing to do");
            return;
        }

        ImmutableList.Builder<String> sourceFileExtensionsBuilder = ImmutableList.builder();
        actualFileTypes.forEach(fileTypeInfo ->
                sourceFileExtensionsBuilder.addAll(fileTypeInfo.getSourceExtensions()));
        ImmutableList<String> sourceFileExtensions = sourceFileExtensionsBuilder.build();

        String[] srcFiles =
                strat.getSrcFiles(sourceDir, getOpts().getIncludes(), getOpts()
                        .getExcludes(), sourceFileExtensions, true, getOpts()
                        .getCaseSensitive());

        SortedSet<String> localDocNames =
                new TreeSet<String>(Arrays.asList(srcFiles));

        // TODO(files) handle obsolete document deletion
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
                    FileTypeName fileType = getFileTypeNameBySourceExtension(actualFileTypes,
                            FilenameUtils.getExtension(docName));
                    consoleInteractor.printfln("           "
                            + Messages.format("push.info.documentToPush",
                            docName, fileType.getName()));
                } else {
                    consoleInteractor.printfln(
                            Messages.format("push.info.skipDocument", docName));
                }
            }
        }

        if (pushTrans()) {
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
                final FileTypeInfo fileType = getFileType(actualFileTypes,
                        srcExtension);
                final String qualifiedDocName = qualifiedDocName(localDocName);
                if (pushSource()) {
                    if (!getOpts().isDryRun()) {
                        boolean sourcePushed =
                                pushSourceDocumentToServer(sourceDir,
                                        localDocName, qualifiedDocName,
                                        fileType.getType().getName());
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

                if (pushTrans()) {
                    Optional<String> translationFileExtension =
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
                                            fileType.getType().getName(), locale.getLocale(),
                                            translatedDoc);
                                }
                            }, translationFileExtension);
                }
            } catch (IOException | RuntimeException e) {
                log.error(
                        "Operation failed: {}\n\n"
                                + "    To retry from the last document, please add the option: {}\n",
                        e.getMessage(),
                        getOpts().buildFromDocArgument(localDocName));
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        if (hasErrors) {
            throw new RuntimeException(
                    "Push completed with errors, see log for details.");
        }

    }

    private void printFileTypes(List<FileTypeInfo> serverAcceptedTypes) {
        consoleInteractor.printfln(DisplayMode.Information, "Listing supported file types [with default source extensions]:");
        List<FileTypeInfo> types = new ArrayList<>(serverAcceptedTypes);
        Collections.sort(types, (a, b) -> 0);
        for (FileTypeInfo docType : types) {
            String exts = docType.getSourceExtensions()
                    .stream()
                    .sorted()
                    .collect(Collectors.joining(";"));
            consoleInteractor.printfln(DisplayMode.Information, "  %s[%s]", docType.getType(), exts);
        }
        log.info("Listed file types: no files were pushed");
    }

    /**
     * Get translation file extension in given docType with the srcExtension.
     * If no extension found, return source file extension.
     *
     * @param docType
     * @param srcExtension
     */
    private Optional<String> getTranslationFileExtension(FileTypeInfo docType,
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
    private @Nullable FileTypeInfo getFileType(
            ImmutableList<FileTypeInfo> fileTypes, String srcExtension) {
        for (FileTypeInfo entry : fileTypes) {
            Collection<String> extensions = entry.getSourceExtensions();
            if (extensions.contains(srcExtension)) {
                return entry;
            }
        }
        return null;
    }

    private @Nullable FileTypeName getFileTypeNameBySourceExtension(
            ImmutableList<FileTypeInfo> fileTypes, String srcExtension) {
        for (FileTypeInfo entry : fileTypes) {
            Collection<String> extensions = entry.getSourceExtensions();
            if (extensions.contains(srcExtension)) {
                return entry.getType();
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

        File srcFile = new File(sourceDir, localDocName);
        pushDocumentToServer(qualifiedDocName, fileType, null /*locale*/, srcFile);
        return true;
    }

    /**
     * @param docId
     * @param fileType
     * @param locale
     * @param docFile
     */
    private void pushDocumentToServer(String docId, String fileType,
            @Nullable String locale, File docFile) {
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
            String docName, @Nullable String locale, DocumentFileUploadForm uploadForm) {
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
                //noinspection StatementWithEmptyBody
                while (fileStream.read(buffer) > 0) {
                    // just keep digesting the input
                }
            } finally {
                fileStream.close();
            }
            //noinspection UnnecessaryLocalVariable
            String md5hash = new String(Hex.encodeHex(md.digest()));
            return md5hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Try to get the file types from the server (if server is new enough), otherwise use
     * compiled-in values (as older versions of the client did).
     * @param client
     * @return
     */
    public List<FileTypeInfo> fileTypeInfoList(FileResourceClient client) {
        try {
            return client.fileTypeInfoList();
        } catch (ResponseProcessingException e) {
            if (e.getResponse().getStatus() == 404) {
                log.info("Detected old Zanata Server; using hard-coded file types.");
                // probably running against an old Zanata Server
                return fileTypeInfoListWorkaround();
            }
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    private List<FileTypeInfo> fileTypeInfoListWorkaround() {
        return ProjectType.fileProjectSourceDocTypes().stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .map(DocumentType::toFileTypeInfo)
                .collect(Collectors.toList());
    }

    private static class StreamChunker implements Iterable<InputStream>,
            Closeable {
        private final int totalChunkCount;
        private int chunksRetrieved;

        private final File file;
        private final byte[] buffer;
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
            if (fileStream != null) {
                fileStream.close();
            }
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
                            "failed to close input stream for file {}",
                                    file.getAbsolutePath(), e);
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
