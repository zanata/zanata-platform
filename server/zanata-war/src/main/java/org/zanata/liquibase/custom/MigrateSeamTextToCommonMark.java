/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.liquibase.custom;

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
import org.zanata.seam.text.SeamTextLexer;
import org.zanata.seam.text.SeamTextToCommonMarkParser;

import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrateSeamTextToCommonMark implements CustomTaskChange {

    private static final int IDX_TOTAL = 1;
    // +1 to account for HApplicationConfiguration/pages.home.content
    private static final String SQL_TOTAL =
            "SELECT count(*) + 1 " +
                    "FROM HProject " +
                    "WHERE homeContent IS NOT NULL";

    private static final int IDX_KEY = 1;
    private static final int IDX_TEXT = 2;
    private static final String[] SQL_QUERIES = {
            "SELECT config_key, config_value, id " +
                    "FROM HApplicationConfiguration " +
                    "WHERE config_key LIKE 'pages.home.content'",
            "SELECT slug, homeContent, id " +
                    "FROM HProject " +
                    "WHERE homeContent IS NOT NULL"
    };

    // NB The extra blank lines are needed to prevent CommonMark from
    // interpreting any following text as part of an HTML block.
    private static final String successfulConversionComment =
            "<!-- The following text was converted from Seam Text to " +
                    "CommonMark by Zanata.  Some formatting changes may " +
                    "have occurred. -->\n\n";

    private static final String unsuccessfulConversionComment =
            "<small><i>The following text failed conversion from Seam Text " +
                    "to CommonMark by Zanata.  It may require editing to " +
                    "fix formatting.</i></small>\n\n";

    private static String stripNonBreakSpace(String text) {
        return text.replace('\u00A0', ' ').replace("&nbsp;", " ");
    }

    /**
     * Converts the specified Seam text to CommonMark if possible,
     * otherwise returns the Seam text with a prepended error message.
     * <p>
     *     Parsing exceptions will not be propagated, but others will.
     * </p>
     */
    public static String convertToCommonMark(String seamText, String key, Logger log)  {
        Reader r = new StringReader(stripNonBreakSpace(seamText));
        SeamTextLexer lexer = new SeamTextLexer(r);
        SeamTextToCommonMarkParser parser = new SeamTextToCommonMarkParser(lexer);
        try {
            parser.startRule();
        } catch (Exception e) {
            log.warning("MigrateSeamTextToCommonMark: error converting Seam text for " + key, e);
            return unsuccessfulConversionComment + seamText;
        }
        return successfulConversionComment + parser.toString();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        Logger log = LogFactory.getLogger();
        JdbcConnection conn = (JdbcConnection) database.getConnection();
        try {
            try (Statement stmt = conn
                    .createStatement(ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_UPDATABLE)) {

                log.info("MigrateSeamTextToCommonMark: counting records");
                ResultSet rs2 = stmt.executeQuery(SQL_TOTAL);
                rs2.next();
                long totalRecords = rs2.getLong(IDX_TOTAL);

                long recordsUpdated = 0;
                for (String sql : SQL_QUERIES) {
                    ResultSet rs3 = stmt.executeQuery(sql);
                    while (rs3.next()) {
                        if (recordsUpdated % 100 == 0) {
                            log.info("MigrateSeamTextToCommonMark: updated "
                                    + recordsUpdated + "/" + totalRecords
                                    + " records");
                        }
                        String key = rs3.getString(IDX_KEY);
                        String seamText = rs3.getString(IDX_TEXT);
                        // pages.home.content could be NULL
                        if (seamText != null) {
                            String commonMark = convertToCommonMark(seamText, key, log);
                            rs3.updateString(IDX_TEXT, commonMark);
                            rs3.updateRow();
                        }
                        ++recordsUpdated;
                    }
                }
                log.info("MigrateSeamTextToCommonMark: updated " +
                        recordsUpdated + " records in total");
            }
        } catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "MigrateSeamTextToCommonMark converted Seam Text to CommonMark";
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

}
