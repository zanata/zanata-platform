package org.zanata.database;

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
import org.junit.Test;
import org.zanata.ZanataJpaTest;

public class WrappedConnectionProviderTest extends ZanataJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(WrappedConnectionProviderTest.class);

    private Session session;

    @Before
    public void setup() {
        session = getSession();
    }

    @Test
    public void testWrapperWithNestedExecute() throws Exception {
        ScrollableResults scroll1 = streamQuery("from HTextFlow");
        try {
            try {
                log.warn(
                        "This test is about to trigger a StreamingResultSetSQLException");
                session.doWork(new Work() {

                    @Override
                    public void execute(Connection connection)
                            throws SQLException {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(
                                "delete from HTextFlow where 0=1");
                    }
                });
                concurrentResultSetNotDetected();
            } catch (JDBCException e) {
                checkExceptionType(e);
            }
        } finally {
            if (scroll1 != null) {
                scroll1.close();
            }
        }
    }

    @Test
    public void testWrapperWithNestedStreaming() throws Exception {
        ScrollableResults scroll1 = streamQuery("from HTextFlow");
        try {
            try {
                log.warn(
                        "This test is about to trigger a StreamingResultSetSQLException");
                ScrollableResults scroll2 = streamQuery("from HTextFlowTarget");
                try {
                    concurrentResultSetNotDetected();
                } finally {
                    if (scroll2 != null) {
                        scroll2.close();
                    }
                }
            } catch (JDBCException e) {
                checkExceptionType(e);
            }
        } finally {
            if (scroll1 != null) {
                scroll1.close();
            }
        }
    }

    @Test
    public void testWrapperWithNestedResults() throws Exception {
        ScrollableResults scroll1 = streamQuery("from HTextFlow");
        try {
            try {
                log.warn(
                        "This test is about to trigger a StreamingResultSetSQLException");
                ScrollableResults scroll2 = scrollQuery("from HTextFlowTarget");
                try {
                    concurrentResultSetNotDetected();
                } finally {
                    if (scroll2 != null) {
                        scroll2.close();
                    }
                }
            } catch (JDBCException e) {
                checkExceptionType(e);
            }
        } finally {
            if (scroll1 != null) {
                scroll1.close();
            }
        }
    }

    private void concurrentResultSetNotDetected() {
        Fail.fail(
                "Failed to detect concurrent ResultSet - is Wrapped*ConnectionProvider enabled in persistence.xml?");
    }

    private void checkExceptionType(JDBCException e) {
        if (!(e.getSQLException() instanceof StreamingResultSetSQLException)) {
            throw e;
        }
    }

    private ScrollableResults streamQuery(String queryString) {
        Query q = session.createQuery(queryString);
        q.setFetchSize(Integer.MIN_VALUE);
        ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY);
        return scroll;
    }

    private ScrollableResults scrollQuery(String queryString) {
        Query q = session.createQuery(queryString);
        ScrollableResults scroll = q.scroll();
        return scroll;
    }
}
