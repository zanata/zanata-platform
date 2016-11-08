package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class RemoteLoggingHandlerTest extends ZanataTest {
    @Inject @Any
    private RemoteLoggingHandler handler;

    @Produces @Mock
    private ZanataIdentity identity;

    @Before
    public void setUp() throws Exception {
        when(identity.getCredentials()).thenReturn(new ZanataCredentials());
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        handler.execute(new RemoteLoggingAction("blah"), null);

        verify(identity).checkLoggedIn();
        verify(identity).getCredentials();
    }

    @Test
    @InRequestScope
    public void testExecuteWithoutLoggedIn() throws Exception {
        doThrow(new RuntimeException("not logged in")).when(identity)
                .checkLoggedIn();

        NoOpResult result =
                handler.execute(new RemoteLoggingAction("blah"), null);

        assertThat(result, Matchers.notNullValue());
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(new RemoteLoggingAction("blow"), new NoOpResult(),
                null);
    }
}
