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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zanata.common.LocaleId;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.zanata.util.GlossaryUtil;

/**
 * Generate content hash for glossary entry
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GenerateHashForGlossaryEntry implements CustomTaskChange {

    @Override
    public String getConfirmationMessage() {
        return "GenerateHashForGlossaryEntry generated contentHash column in HGlossaryEntry table";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    private final static String DATE_FORMAT = "dd/MM/yy HH:mm:ss:SS";
    private final static SimpleDateFormat formatter = new SimpleDateFormat(
            DATE_FORMAT);

    /**
     * Generate unique description by appending timestamp.
     */
    private String generateConflictMessage(Long id) {
        return " (Zanata: This description has been updated during data migration to prevent conflict: "
                + formatter.format(new Date()) + " -" + id + ")";
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        final JdbcConnection conn = (JdbcConnection) database.getConnection();
        try (Statement stmt =
                conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE)) {

            Map<Long, String> entryLocaleMap = new HashMap<Long, String>();
            Map<Long, String> entryDescriptionMap = new HashMap<Long, String>();

            String entryLocaleSql = "select entry.id, entry.pos, entry.description, locale.localeId, term.content from " +
                "HGlossaryEntry entry, HGlossaryTerm term, HLocale locale  " +
                "where term.glossaryEntryId = entry.id and term.localeId = locale.id";
            ResultSet rs1 = stmt.executeQuery(entryLocaleSql);
            while (rs1.next()) {
                long entryId = rs1.getLong(1);
                String pos = rs1.getString(2);
                String desc = rs1.getString(3);
                if(desc == null) {
                    desc = "";
                }
                String localeId = rs1.getString(4);
                String content = rs1.getString(5);

                String hash = GlossaryUtil.generateHash(new LocaleId(localeId),
                    content, pos, desc);

                /**
                 * Conflict on source content, pos, description
                 * Update description to unique message string with timestamp.
                 */
                if(entryLocaleMap.containsValue(hash)) {
                    desc = desc + generateConflictMessage(entryId);
                    hash = GlossaryUtil.generateHash(new LocaleId(localeId),
                        content, pos, desc);
                    entryDescriptionMap.put(entryId, desc);
                }
                entryLocaleMap.put(entryId, hash);
            }

            String entrySql =
                "select entry.id, entry.contentHash, entry.description from HGlossaryEntry entry";
            ResultSet rs2 = stmt.executeQuery(entrySql);

            while (rs2.next()) {
                long id = rs2.getLong(1);
                String hash = entryLocaleMap.get(id);
                rs2.updateString(2, hash);
                if (entryDescriptionMap.containsKey(id)) {
                    rs2.updateString(3, entryDescriptionMap.get(id));
                }
                rs2.updateRow();
            }
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }
}
