package org.zanata.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.database.ConnectionWrapper.getConnectionWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.assertj.core.api.Fail;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zanata.ZanataJpaTest;

public class ConnectionWrapperTransactionTest extends ZanataJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(ConnectionWrapperTransactionTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private Session session;

    @Override
    protected boolean rollbackBeforeClose() {
        return false;
    }

    @Before
    public void setup() {
        session = getSession();
    }

    @Test
    public void transactionNotActiveInitially() throws Exception {
        session.doWork(connection -> {
            assertThat(getConnectionWrapper(connection).transactionActive)
                    .isFalse();
        });
    }

    @Test
    public void emptyTransactionShouldBeOkay1() throws Exception {
        session.doWork(connection -> {
            // start a transaction
            connection.setAutoCommit(false);
            connection.close();
        });
    }

    @Test
    public void emptyTransactionShouldBeOkay2() throws Exception {
        session.doWork(connection -> {
            // start txn1
            connection.setAutoCommit(false);
            connection.createStatement().execute("");
            // stop txn1
            connection.setAutoCommit(true);
            // start txn2
            connection.setAutoCommit(false);
            connection.close();
        });
    }

    @Test
    public void commitTransactionShouldBeOkay() throws Exception {
        session.doWork(connection -> {
            // start a transaction
            connection.setAutoCommit(false);
            connection.createStatement().execute("");
            connection.commit();
            connection.close();
        });
    }

    @Test
    public void rollbackTransactionShouldBeOkay() throws Exception {
        session.doWork(connection -> {
            // start a transaction
            connection.setAutoCommit(false);
            connection.createStatement().execute("");
            connection.rollback();
            connection.close();
        });
    }

    @Test
    public void openTransactionShouldCauseException() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("transaction active");
        session.doWork(connection -> {
            // start a transaction
            connection.setAutoCommit(false);
            connection.createStatement().execute("");
            // not committing or rolling back,
            // so this should trigger an exception:
            connection.close();
        });
    }
}
