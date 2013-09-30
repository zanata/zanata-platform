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
package org.zanata.liquibase.custom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.zanata.common.DocumentType;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

@Slf4j
public class MigrateRawDocumentsToFileSystem implements CustomTaskChange {

    private static final String BASE_PATH_JNDI_NAME =
            "java:global/zanata/files/document-storage-directory";
    private static final String BASE_PATH_LIQUIBASE_PARAM =
            "document.storage.directory";
    private static final String RAW_DOCUMENTS_SUBDIRECTORY = "documents";

    private static final String CONTENTS_SQL =
            "select fileId, content from HRawDocumentContent";
    private static final String ID_TYPE_SQL = "select d.documentId, rd.type"
            + " from HRawDocument rd, HDocument_RawDocument d"
            + " where d.rawDocumentId = rd.id and rd.fileId = ?";
    private static final String UPDATE_LOCATION_SQL =
            "update HRawDocument set fileId = ? where fileId = ?";
    private static final String DELETE_OLD_CONTENT_SQL =
            "delete from HRawDocumentContent where fileId = ?";

    @Setter
    private String basePathParam;

    private File docsDirectory;

    private int docsCount;
    private int successCount;
    private int problemsCount;

    private PreparedStatement contentsStatement;
    private PreparedStatement idAndTypeStatement;
    private PreparedStatement updateLocationStatement;
    private PreparedStatement deleteOldContentStatement;

    @Override
    public String getConfirmationMessage() {
        return "Raw documents migrated from database to file system under path "
                + docsDirectory.getAbsolutePath()
                + ". Total documents: "
                + docsCount
                + ", success: "
                + successCount
                + ", problems: "
                + problemsCount;
    }

    @Override
    public void setUp() throws SetupException {
        resetCounts();
        createDocsDirectoryFromConfig();
    }

    private void resetCounts() {
        docsCount = 0;
        successCount = 0;
        problemsCount = 0;
    }

    private void createDocsDirectoryFromConfig() throws SetupException {
        String basePath = getCongiguredBasePath();
        log.info("Raw documents will be migrated to: " + basePath);
        docsDirectory = new File(basePath, RAW_DOCUMENTS_SUBDIRECTORY);
        try {
            docsDirectory.mkdirs();
        } catch (SecurityException e) {
            throw new SetupException(e);
        }
    }

    private String getCongiguredBasePath() throws SetupException {
        try {
            return getPathFromJndi();
        } catch (SetupException e) {
            return tryGetFallbackBasePath(e);
        }
    }

    private String getPathFromJndi() throws SetupException {
        String messageJndiNameNotBound =
                "Could not look up document storage directory under jndi name \""
                        + BASE_PATH_JNDI_NAME + "\".";
        InitialContext initContext = null;
        String basePath;
        try {
            initContext = new InitialContext();
            basePath = (String) initContext.lookup(BASE_PATH_JNDI_NAME);

            if (basePath == null) {
                throw new SetupException(messageJndiNameNotBound);
            }
        } catch (NameNotFoundException e) {
            throw new SetupException(messageJndiNameNotBound, e);
        } catch (NamingException e) {
            throw new SetupException(messageJndiNameNotBound, e);
        } finally {
            if (initContext != null) {
                try {
                    initContext.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
        return basePath;
    }

    private String tryGetFallbackBasePath(SetupException initialFailureCause)
            throws SetupException {
        if (!basePathParamIsSet()) {
            throw new SetupException(
                    "No information for document storage directory. "
                            + "Fallback liquibase parameter \""
                            + BASE_PATH_LIQUIBASE_PARAM + "\" not set. "
                            + "Cannot migrate documents to file system.",
                    initialFailureCause);
        }
        return basePathParam;
    }

    private boolean basePathParamIsSet() {
        if (Strings.isNullOrEmpty(basePathParam)) {
            return false;
        }
        boolean paramIsExpanded =
                !basePathParam.equals("${" + BASE_PATH_LIQUIBASE_PARAM + "}");
        return paramIsExpanded;
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection conn = (JdbcConnection) database.getConnection();
        try {
            prepareStatements(conn);
            migrateAllRawContents();
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    private void prepareStatements(JdbcConnection conn)
            throws DatabaseException {
        contentsStatement = conn.prepareStatement(CONTENTS_SQL);
        idAndTypeStatement = conn.prepareStatement(ID_TYPE_SQL);
        updateLocationStatement = conn.prepareStatement(UPDATE_LOCATION_SQL);
        deleteOldContentStatement =
                conn.prepareStatement(DELETE_OLD_CONTENT_SQL);
    }

    private void migrateAllRawContents() throws Exception {
        ResultSet contentsResult = contentsStatement.executeQuery();
        while (contentsResult.next()) {
            try {
                docsCount++;
                String oldFileId = contentsResult.getString("fileId");
                Blob content = contentsResult.getBlob("content");
                migrateRawContentFromLocation(content, oldFileId);
                successCount++;
            } catch (Exception e) {
                problemsCount++;
                throw e;
            }
        }
    }

    private void migrateRawContentFromLocation(Blob content, String oldFileId)
            throws SQLException, IOException {
        idAndTypeStatement.setString(1, oldFileId);
        ResultSet idAndTypeResult = idAndTypeStatement.executeQuery();
        if (idAndTypeResult.next()) {
            String fileName = fileNameFromResults(idAndTypeResult);
            writeBlobToFile(content, fileName);
            changeFileIdFromOldToNew(oldFileId, fileName);
            deleteOldContent(oldFileId);
        } else {
            throw new RuntimeException(
                    "Raw document content with no matching raw document, "
                            + "HRawDocumentContent.fileId = " + oldFileId);
        }
    }

    private static String fileNameFromResults(ResultSet idAndTypeResult)
            throws SQLException {
        long docId = idAndTypeResult.getLong("documentId");
        String type = idAndTypeResult.getString("type");
        return fileNameFromIdAndType(docId, type);
    }

    private static String fileNameFromIdAndType(Long docId, String type) {
        String extension = DocumentType.valueOf(type).getExtension();
        return docId.toString() + "." + extension;
    }

    private void writeBlobToFile(Blob content, String fileName)
            throws SQLException, IOException {
        File outputFile = createFileInConfiguredDirectory(fileName);
        writeStreamToFile(content.getBinaryStream(), outputFile);
        // releasing blob resources may not be necessary, but makes me feel
        // better and shouldn't cause problems
        content.free();
        content = null;
    }

    private File createFileInConfiguredDirectory(String fileName) {
        return new File(docsDirectory, fileName);
    }

    private void writeStreamToFile(final InputStream stream, File file)
            throws IOException {
        InputSupplier<InputStream> input = new InputSupplier<InputStream>() {
            public InputStream getInput() throws IOException {
                return stream;
            }
        };
        Files.copy(input, file);
    }

    private void changeFileIdFromOldToNew(String oldFileId, String newFileId)
            throws SQLException {
        updateLocationStatement.setString(1, newFileId);
        updateLocationStatement.setString(2, oldFileId);
        int updatedRows = updateLocationStatement.executeUpdate();
        if (updatedRows != 1) {
            throw new RuntimeException(
                    "Tried to update fileId for 1 HRawDocument, but updated "
                            + updatedRows);
        }
    }

    private void deleteOldContent(String oldFileId) throws SQLException {
        deleteOldContentStatement.setString(1, oldFileId);
        int deletedRows = deleteOldContentStatement.executeUpdate();
        if (deletedRows != 1) {
            throw new RuntimeException(
                    "Tried to delete 1 row from HRawDocumentContent, but deleted "
                            + deletedRows);
        }
    }

}
