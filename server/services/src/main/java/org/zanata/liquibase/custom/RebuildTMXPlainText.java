/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.zanata.tmx.TMXUtil;
import org.zanata.util.HashUtil;
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

/**
 * Rebuilds the plain text segments of all TransMemoryUnitVariant
 * to apply an encoding fix - https://zanata.atlassian.net/browse/ZNTA-1740
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class RebuildTMXPlainText implements CustomTaskChange {
    private long segmentCount = 0;
    private long modifiedCount = 0;

    @Override
    public void execute(Database database) throws CustomChangeException {
        // get the Logger as late as possible, to give Liquibase a chance to initialise
        Logger log = LogFactory.getInstance().getLog();

        // apparently we don't need to close this
        JdbcConnection conn = (JdbcConnection) database.getConnection();

        try (Statement stmt = conn
                .createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE)) {
            String countSql =
                    "select count(*) from TransMemoryUnitVariant";
            try (ResultSet rsCount = stmt.executeQuery(countSql)) {
                rsCount.next();
                this.segmentCount = rsCount.getLong(1);
            }
            log.info("RebuildTMXPlainText: visiting " + segmentCount
                    + " rows");
            // NB primary key is needed for updatable ResultSet
            String updatableSql =
                    "select id, lastChanged, tagged_segment, " +
                            "plain_text_segment, plain_text_segment_hash " +
                            "from TransMemoryUnitVariant";
            long rowsVisited = 0;
            try (ResultSet rsUpdatable = stmt.executeQuery(updatableSql)) {
                while (rsUpdatable.next()) {
                    String taggedSegment = rsUpdatable.getString(3);
                    String oldPlainSegment = rsUpdatable.getString(4);
                    String oldPlainSegHash = rsUpdatable.getString(5);
                    String newPlainSegment =
                            TMXUtil.removeFormattingMarkup(taggedSegment);
                    String newPlainSegHash =
                            HashUtil.generateHash(newPlainSegment);
                    // if the hashes match, the segments probably will, but it won't hurt to check
                    if (!newPlainSegHash.equals(oldPlainSegHash) ||
                            !newPlainSegment.equals(oldPlainSegment)) {
                        // update timestamp to avoid breaking etag caching
                        rsUpdatable.updateTimestamp(2,
                                new Timestamp(System.currentTimeMillis()));
                        rsUpdatable.updateString(4, newPlainSegment);
                        rsUpdatable.updateString(5, newPlainSegHash);
                        rsUpdatable.updateRow();
                        ++modifiedCount;
                    }
                    if (++rowsVisited % 10_000 == 0) {
                        log.info("RebuildTMXPlainText: visited "
                                + rowsVisited + "/" + segmentCount);
                    }
                }
            }
            log.info("RebuildTMXPlainText: finished");
            if (modifiedCount != 0) {
                log.warning("RebuildTMXPlainText: " + modifiedCount + " segments modified. Please reindex TransMemoryUnit in the Manage Search page.");
            }
            if (rowsVisited != segmentCount) {
                log.warning("RebuildTMXPlainText: expected " + segmentCount + "rows but visited " + rowsVisited);
            }
        } catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "RebuildTMXPlainText: Finished rebuilding " + segmentCount
                + " segments. " + modifiedCount + " modified.";
    }

    @Override
    public void setUp() throws SetupException {
        // nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // nothing
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
