package org.zanata.rest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
import org.zanata.model.HAccount;
import org.zanata.util.HttpUtil;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

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
    private HAccount authenticatedUser;
    @Mock
    private HttpServletRequest servletRequest;

    private String clienIP = "255.255.255.0.1";

    @BeforeMethod
    public void beforeMethod() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);
        when(request.getHttpHeaders().getRequestHeaders())
                .thenReturn(headers);
        when(request.getHttpMethod()).thenReturn("GET");
        when(headers.getFirst(HttpUtil.X_AUTH_TOKEN_HEADER)).thenReturn(
            API_KEY);

        dispatcher =
                spy(new RestLimitingSynchronousDispatcher(providerFactory,
                        processor));

        // this way we can verify the task actually called super.invoke()
        doReturn(servletRequest).when(dispatcher).getServletRequest();
        doReturn(superInvoker).when(dispatcher).getInvoker(request);
        doNothing().when(dispatcher).invoke(request, response, superInvoker);
        authenticatedUser = null;
        doReturn(authenticatedUser).when(dispatcher).getAuthenticatedUser();
    }

    @Test
    public void willUseAuthenticatedUserApiKeyIfPresent() throws Exception {
        authenticatedUser = new HAccount();
        authenticatedUser.setApiKey("apiKeyInAuth");
        doReturn(authenticatedUser).when(dispatcher).getAuthenticatedUser();

        dispatcher.invoke(request, response);

        verify(processor).processForApiKey(same("apiKeyInAuth"), same(response),
            taskCaptor.capture());
    }

    @Test
    public void willUseUsernameIfNoApiKeyButAuthenticated() throws Exception {
        authenticatedUser = new HAccount();
        authenticatedUser.setUsername("admin");
        doReturn(authenticatedUser).when(dispatcher).getAuthenticatedUser();

        dispatcher.invoke(request, response);

        verify(processor).processForUser(same("admin"), same(response),
            taskCaptor.capture());
    }

    @Test
    public void willThrowErrorWithPOSTAndNoApiKey() throws Exception {
        when(request.getHttpMethod()).thenReturn("POST");
        when(headers.getFirst(HttpUtil.X_AUTH_TOKEN_HEADER)).thenReturn(
            null);
        when(request.getUri().getPath()).thenReturn("/rest/in/peace");
        doReturn(null).when(dispatcher).getAuthenticatedUser();

        dispatcher.invoke(request, response);

        verify(response).setStatus(401);
        verify(response).getOutputStream();
        verifyZeroInteractions(processor);
    }

    @Test
    public void willProcessAnonymousWithGETAndNoApiKey() throws Exception {
        when(headers.getFirst(HttpUtil.X_AUTH_TOKEN_HEADER)).thenReturn(null);
        when(request.getUri().getPath()).thenReturn("/rest/in/peace");
        when(servletRequest.getRemoteAddr()).thenReturn(clienIP);
        doReturn(null).when(dispatcher).getAuthenticatedUser();

        dispatcher.invoke(request, response);

        verify(processor).processForAnonymousIP(same(clienIP), same(response),
            taskCaptor.capture());

        // verify task is calling super.invoke
        Runnable task = taskCaptor.getValue();
        task.run();
        verify(dispatcher).getInvoker(request);
    }
}
