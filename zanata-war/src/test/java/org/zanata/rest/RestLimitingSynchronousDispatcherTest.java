package org.zanata.rest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.limits.RateLimitingProcessor;

import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RestLimitingSynchronousDispatcherTest {
    private RestLimitingSynchronousDispatcher dispatcher;

    private static final String API_KEY = "apiKey123";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpRequest request;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpResponse response;
    @Mock
    private RateLimitingProcessor processor;
    @Mock
    private SeamResteasyProviderFactory providerFactory;
    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;
    @Mock
    private ResourceInvoker superInvoker;
    @Mock
    private MultivaluedMap<String, String> headers;

    @BeforeMethod
    public void beforeMethod() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);

        when(request.getHttpHeaders().getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn(
                API_KEY);

        dispatcher =
                spy(new RestLimitingSynchronousDispatcher(providerFactory,
                        processor));

        // this way we can verify the task actually called super.invoke()
        doReturn(superInvoker).when(dispatcher).getInvoker(request);
        doNothing().when(dispatcher).invoke(request, response, superInvoker);
    }

    @Test
    public void willSkipIfAPIkeyNotPresent() throws IOException,
            ServletException {
        when(headers.getFirst(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn(
                null);
        when(request.getUri().getPath()).thenReturn("/rest/in/peace");

        dispatcher.invoke(request, response);

        verify(response).sendError(401,
                RestLimitingSynchronousDispatcher.API_KEY_ABSENCE_WARNING);
        verifyZeroInteractions(processor);
    }

    @Test
    public void willCallRateLimitingProcessorIfAllConditionsAreMet()
            throws Exception {
        dispatcher.invoke(request, response);

        verify(processor).process(same(API_KEY), same(response), taskCaptor.capture());

        // verify task is calling super.invoke
        Runnable task = taskCaptor.getValue();
        task.run();
        verify(dispatcher).getInvoker(request);

    }
}
