package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RemoteLoggingHandlerTest
{
   private RemoteLoggingHandler handler;

   @Mock
   private ZanataIdentity identity;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .ignoreNonResolvable()
            .autowire(RemoteLoggingHandler.class);
      // @formatter:on

      when(identity.getCredentials()).thenReturn(new ZanataCredentials());
   }

   @Test
   public void testExecute() throws Exception
   {
      handler.execute(new RemoteLoggingAction("blah"), null);

      verify(identity).checkLoggedIn();
      verify(identity).getCredentials();
   }

   @Test
   public void testExecuteWithoutLoggedIn() throws Exception
   {
      doThrow(new RuntimeException("not logged in")).when(identity).checkLoggedIn();

      NoOpResult result = handler.execute(new RemoteLoggingAction("blah"), null);

      assertThat(result, Matchers.notNullValue());
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(new RemoteLoggingAction("blow"), new NoOpResult(), null);
   }
}
