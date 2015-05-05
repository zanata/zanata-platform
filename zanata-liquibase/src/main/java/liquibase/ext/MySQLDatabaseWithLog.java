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
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;

import java.io.StringWriter;
import java.util.List;


/**
 * A slightly modified MySQLDatabase which simply logs each change before
 * executing.
 * <p>
 * Note: SqlGenerator implementations must live in package
 * "liquibase.ext" (or similar) to be automatically registered.
 * </p>
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@SuppressWarnings("unused")
public class MySQLDatabaseWithLog extends MySQLDatabase {

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
            Logger log = LogFactory.getLogger();
            log.info("Executing " + change.getClass().getSimpleName());
        }
        super.executeStatements(change, changeLog, sqlVisitors);
    }
}
