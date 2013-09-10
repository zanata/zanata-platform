package org.zanata.eventservice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class SequentialConnectionIdGeneratorTest
{
   private SequentialConnectionIdGenerator idGenerator;
   @Mock
   private HttpServletRequest request;
   @Mock
   private HttpSession session;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      idGenerator = new SequentialConnectionIdGenerator();
   }

   @Test
   public void testGenerateConnectionId() throws Exception
   {
      when(request.getSession(true)).thenReturn(session);
      when(session.getId()).thenReturn("sessionId");

      assertThat(idGenerator.generateConnectionId(request), Matchers.equalTo("sessionId-0"));
      assertThat(idGenerator.generateConnectionId(request), Matchers.equalTo("sessionId-1"));
      assertThat(idGenerator.generateConnectionId(request), Matchers.equalTo("sessionId-2"));
   }

   @Test
   public void testGetConnectionId() throws Exception
   {
      when(request.getParameter("id")).thenReturn("connectionId");

      String connectionId = idGenerator.getConnectionId(request);

      assertThat(connectionId, Matchers.equalTo("connectionId"));
   }
}
