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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.seam.security.management.PasswordHash;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Custom change to prevent an empty password from being in the system.
 *
 * All Authentication mechanisms allow for empty passwords in previous versions
 * but since authentication mechanisms can now be mixed, matched, and changed,
 * no empty passwords should ever be allowed. ('Empty' means the empty string
 * salted and hashed)
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @since 2.0.0
 */
public class ValidateAccountPasswords implements CustomTaskChange {
    private int emptyPasswordsFound = 0;

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection conn = (JdbcConnection) database.getConnection();

        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt =
                    conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                            ResultSet.CONCUR_UPDATABLE);
            rset =
                    stmt.executeQuery("select id, username, passwordHash from HAccount");

            while (rset.next()) {
                String username = rset.getString("username");
                String passwordHash = rset.getString("passwordHash");
                // Deprecated, but it's the same method used to generate
                // passwords on the rest of Zanata
                String emptyPasswordHash =
                        new PasswordHash().generateSaltedHash("", username,
                                PasswordHash.ALGORITHM_MD5);

                if (emptyPasswordHash.equals(passwordHash)) {
                    emptyPasswordsFound++;
                    rset.updateString("passwordHash", null);
                    rset.updateRow();
                }
            }
        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                // Ignore this one, already closed probably
            }
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Found " + emptyPasswordsFound
                + " accounts with the empty password. Corrected them.";
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
}
