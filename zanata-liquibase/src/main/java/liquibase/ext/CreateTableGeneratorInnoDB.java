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

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

/**
 * A create table statement generator to force the use of the InnoDB engine
 * on MySQL/MariaDB, unless the engine has already been specified by other
 * means (eg modifySql).
 * <p>
 * Note: SqlGenerator implementations must live in package
 * "liquibase.ext" (or similar) to be automatically registered.
 * </p>
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@SuppressWarnings("unused")
public class CreateTableGeneratorInnoDB extends CreateTableGenerator {

    @Override
    public boolean supports(CreateTableStatement statement, Database database) {
        // NB this will include MariaDB (with Liquibase 3.4+)
        return database instanceof MySQLDatabase;
    }

    @Override
    public int getPriority() {
        // Higher priority than the built-ins
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public ValidationErrors validate(CreateTableStatement createTableStatement,
            Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.validate(createTableStatement, database);
    }

    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        Logger log = LogFactory.getLogger();

        boolean foundCreateTable = false;
        Sql[] sqls = sqlGeneratorChain.generateSql(statement, database).clone();

        // Append " ENGINE=INNODB" to CREATE TABLE unless ENGINE already
        // specified. This is clumsy, but Liquibase doesn't have seem
        // to have any better hooks for this.
        for (int i = 0; i < sqls.length; i++) {
            Sql sql = sqls[i];
            String sqlText = sql.toSql();
            String sqlUpper = sqlText.trim().toUpperCase();
            if (sqlUpper.startsWith("CREATE TABLE ")) {
                foundCreateTable = true;

                if (!(sqlUpper.contains(" ENGINE ") || sqlUpper
                        .contains(" ENGINE="))) {
                    log.info("Adding ENGINE=INNODB to generated SQL for table "
                            + statement.getTableName());
                    sqlText += " ENGINE=INNODB ";
                    DatabaseObject[] objects = toArray(
                            sql.getAffectedDatabaseObjects());
                    sqls[i] =
                            new UnparsedSql(sqlText, sql.getEndDelimiter(),
                                    objects);
                } else {
                    log.info("SQL for CREATE TABLE already contains ENGINE for table "
                            + statement.getTableName());
                }
            }
        }
        if (!foundCreateTable) {
            throw new UnexpectedLiquibaseException(
                    "CREATE TABLE not found; unable to ensure ENGINE=INNODB for table "
                            + statement.getTableName());
        }
        return sqls;
    }

    private DatabaseObject[] toArray(
            Collection<? extends DatabaseObject> objects) {
        return objects.toArray(
                new DatabaseObject[objects.size()]);
    }

}
