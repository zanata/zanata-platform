package org.zanata.database;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.zanata.ZanataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.database.ConnectionWrapper.getConnectionWrapper;

public class ConnectionWrapperTransactionTest extends ZanataJpaTest {

    private static final Logger log = org.slf4j.LoggerFactory
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
            assertThat(getConnectionWrapper(connection).transactionActive).isFalse();
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
