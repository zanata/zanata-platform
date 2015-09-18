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
package liquibase.ext;

import liquibase.change.Change;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.structure.DatabaseObject;

import java.util.List;

/**
 * A slightly modified PostgresDatabase with a a few extensions:
 * 1. Logs each change before executing it.
 * 2. Prevents quoting object names, unless they are a reserved word.
 *
 * <p>
 * Note: SqlGenerator implementations must live in package "liquibase.ext" (or
 * similar) to be automatically registered.
 * </p>
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CustomPostgresDatabase extends PostgresDatabase {
    @Override
    public int getPriority() {
        // Higher priority than the built-ins
        return PRIORITY_DATABASE;
    }

    @Override
    public void executeStatements(Change change, DatabaseChangeLog changeLog,
            List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        if (getConnection() != null) {
            // don't log if running offline
            Logger log = LogFactory.getInstance().getLog();
            log.info("Executing " + change.getClass().getSimpleName());
        }
        super.executeStatements(change, changeLog, sqlVisitors);
    }

    @Override
    public String escapeObjectName(String objectName,
            Class<? extends DatabaseObject> objectType) {
        if (objectName != null && isReservedWord(objectName)) {
            return "\"" + objectName + "\"";
        } else {
            // This is the same code as in AbstractJdbcDatabase
            // it's basically "grandfathered" code
            if (objectName != null) {
                objectName = objectName.trim();
                if (mustQuoteObjectName(objectName, objectType)) {
                    return quoteObject(objectName, objectType);
                } else if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
                    return quoteObject(objectName, objectType);
                }
                objectName = objectName.trim();
            }
            return objectName;
        }
    }
}
