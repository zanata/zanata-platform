package org.zanata.liquibase.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

import org.zanata.util.OkapiUtil;

public class CountWordsInHTextFlow implements CustomTaskChange {

    @Override
    public String getConfirmationMessage() {
        return "CountWordsInHTextFlow updated wordCount column in HTextFlow table";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor accessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection conn = (JdbcConnection) database.getConnection();

        try (Statement stmt =
                conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE)) {

            Map<Long, String> docToLocaleMap = new HashMap<>();
            String docLocaleSql =
                    "select doc.id, loc.localeId from HDocument doc, HLocale loc where doc.locale = loc.id";
            try (ResultSet rs1 = stmt.executeQuery(docLocaleSql)) {
                while (rs1.next()) {
                    long docId = rs1.getLong(1);
                    String locale = rs1.getString(2);
                    docToLocaleMap.put(docId, locale);
                }
            }

            long totalRows;
            String countSql =
                    "select count(*) from HTextFlow where wordCount is null or wordCount = 0";
            try (ResultSet rs2 = stmt.executeQuery(countSql)) {
                rs2.next();
                totalRows = rs2.getLong(1);
            }

            Logger log = LogFactory.getInstance().getLog();
            log.info("CountWordsInHTextFlow: updating " + totalRows + " rows");
            // NB primary key 'id' is included to get an updatable ResultSet
            String textFlowSql =
                    "select id, document_id, content, wordCount from HTextFlow where wordCount is null or wordCount = 0";
            try (ResultSet rs3 = stmt.executeQuery(textFlowSql)) {
                long rowsUpdated = 0;
                while (rs3.next()) {
                    long docId = rs3.getLong(2);
                    String content = rs3.getString(3);
                    String locale = docToLocaleMap.get(docId);
                    long wordCount = OkapiUtil.countWords(content, locale);
                    rs3.updateLong(4, wordCount);
                    rs3.updateRow();
                    if (++rowsUpdated % 10000 == 0) {
                        log.info("CountWordsInHTextFlow: updated "
                                + rowsUpdated + "/" + totalRows);
                    }
                }
            }
            log.info("CountWordsInHTextFlow: finished");
        } catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

}
