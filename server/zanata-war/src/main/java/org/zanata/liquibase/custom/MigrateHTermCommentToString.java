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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MigrateHTermCommentToString implements CustomTaskChange {

    @Override
    public String getConfirmationMessage() {
        return "MigrateHTermCommentToString migrated HTermComment to HGlossaryTerm comment";
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

    @Override
    public void execute(Database database) throws CustomChangeException {
        final JdbcConnection conn = (JdbcConnection) database.getConnection();

        try (Statement stmt =
            conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE)) {

            Map<Long, String> termCommentsMap = new HashMap<Long, String>();

            String termCommentsSql = "select term.id, comment.comment from " +
                "HGlossaryTerm as term, HTermComment as comment where comment.glossaryTermId = term.id";

            ResultSet rs1 = stmt.executeQuery(termCommentsSql);
            while (rs1.next()) {
                long termId = rs1.getLong(1);
                String comment = rs1.getString(2);
                String newComment = null;

                if(termCommentsMap.containsKey(termId)) {
                    newComment = joinComment(termCommentsMap.get(termId),
                        comment);
                } else {
                    newComment = joinComment(null, comment);
                }
                termCommentsMap.put(termId, newComment);
            }

            String termSql =
                "select term.id, term.comment from HGlossaryTerm term";

            ResultSet rs2 = stmt.executeQuery(termSql);

            while (rs2.next()) {
                long termId = rs2.getLong(1);
                String comment = termCommentsMap.get(termId);
                rs2.updateString(2, comment);
                rs2.updateRow();
            }
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

    private String joinComment(String existingComment, String newComment) {
        if (StringUtils.isBlank(existingComment)
                && StringUtils.isBlank(newComment)) {
            return null;
        }

        if(StringUtils.isBlank(existingComment)) {
            return newComment;
        }

        if(StringUtils.isBlank(newComment)) {
            return existingComment;
        }
        return existingComment + "\n" + newComment;
    }
}
