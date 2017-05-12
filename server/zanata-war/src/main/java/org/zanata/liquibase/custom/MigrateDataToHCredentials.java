/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

/**
 * Custom change set to migrate authentication data to the HCredentials table.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class MigrateDataToHCredentials implements CustomTaskChange {
    private String dbAuthType;

    @Override
    public void execute(Database database) throws CustomChangeException {
        // (Only Open Id for now)
        if (dbAuthType.equals("OPENID")) {
            JdbcConnection conn = (JdbcConnection) database.getConnection();
            String query = "select acc.id, acc.username, p.email, acc.creationDate, acc.lastChanged "
                    + " from HAccount acc, HPerson p"
                    + " where p.accountId = acc.id";
            try (
                    Statement stmt = conn.createStatement();
                    ResultSet rset = stmt.executeQuery(query)) {
                PreparedStatement insertStmt =
                        conn.prepareStatement("insert into HCredentials "
                                + "(account_id, type, user, email, creationDate, lastChanged, versionNum) values"
                                + "(?, ?, ?, ?, ?, ?, ?)");

                while (rset.next()) {
                    insertStmt.setLong(1, rset.getLong("id"));
                    insertStmt.setString(2, dbAuthType);
                    if (dbAuthType.equals("OPENID")) {
                        insertStmt.setString(3,
                                "http://" + rset.getString("username")
                                        + ".id.fedoraproject.org/");
                    } else {
                        insertStmt.setString(3, rset.getString("username"));
                    }
                    insertStmt.setString(4, rset.getString("email"));
                    insertStmt.setDate(5, rset.getDate("creationDate"));
                    insertStmt.setDate(6, rset.getDate("lastChanged"));
                    insertStmt.setLong(7, 0);

                    insertStmt.executeUpdate();
                }
            } catch (DatabaseException | SQLException e) {
                throw new CustomChangeException(e);
            }
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "User credentials migrated to HCredentials table";
    }

    @Override
    public void setUp() throws SetupException {
        InitialContext initContext = null;
        try {
            initContext = new InitialContext();
            String openid =
                    (String) initContext
                            .lookup("java:global/zanata/security/auth-policy-names/openid");

            if (openid != null) {
                dbAuthType = "OPENID";
            } else {
                dbAuthType = "OTHER";
            }
        } catch (NameNotFoundException e) {
            dbAuthType = "OTHER";
        } catch (NamingException e) {
            throw new SetupException(e);
        } finally {
            if (initContext != null) {
                try {
                    initContext.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
