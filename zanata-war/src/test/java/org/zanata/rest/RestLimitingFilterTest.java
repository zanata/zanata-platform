package org.zanata.rest;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataTest;
import org.zanata.limits.RateLimitingProcessor;
import org.zanata.model.HAccount;
import org.zanata.util.HttpUtil;
import org.zanata.util.RunnableEx;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RestLimitingFilterTest extends ZanataTest {
    private RestLimitingFilter filter;

    private static final String API_KEY = "apiKey123";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest request;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletResponse response;
    @Mock
    private RateLimitingProcessor processor;
    @Captor
    private ArgumentCaptor<RunnableEx> taskCaptor;
    @Mock
    private FilterChain filterChain;
    private HAccount authenticatedUser;

    private String clientIP = "255.255.0.1";

    @Before
    public void beforeMethod() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);
        when(request.getMethod()).thenReturn("GET");

        filter = spy(new RestLimitingFilter(processor));

        // this way we can verify the task actually called super.invoke()
        doNothing().when(filterChain).doFilter(request, response);
        authenticatedUser = null;
        doReturn(authenticatedUser).when(filter).getAuthenticatedUser();
    }

    @Test
    public void willUseApiKeyIfPresent() throws Exception {
        when(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(
                API_KEY);

        filter.doFilter(request, response, filterChain);

        verify(processor).processForApiKey(same(API_KEY), same(response),
            taskCaptor.capture());

        // verify task is calling filter chain
        RunnableEx task = taskCaptor.getValue();
        task.run();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void willUseUsernameIfNoApiKeyButAuthenticated() throws Exception {
        authenticatedUser = new HAccount();
        authenticatedUser.setUsername("admin");
        doReturn(authenticatedUser).when(filter).getAuthenticatedUser();

        filter.doFilter(request, response, filterChain);

        verify(processor).processForUser(same("admin"), same(response),
            taskCaptor.capture());

        // verify task is calling filter chain
        RunnableEx task = taskCaptor.getValue();
        task.run();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void willUseAuthorizationCodeIfItPresents()
            throws Exception {
        String authCode = "abc123";
        when(request.getParameter(OAuth.OAUTH_CODE)).thenReturn(
                authCode);

        filter.doFilter(request, response, filterChain);

        verify(processor).processForToken(same(authCode), same(response),
                taskCaptor.capture());

        // verify task is calling filter chain
        RunnableEx task = taskCaptor.getValue();
        task.run();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void willUseAccessTokenIfItPresents()
            throws Exception {
        when(request.getHeader(OAuth.HeaderType.AUTHORIZATION)).thenReturn(
                "Bearer abc123");

        filter.doFilter(request, response, filterChain);

        verify(processor).processForToken(eq("abc123"), same(response),
                taskCaptor.capture());

        // verify task is calling filter chain
        RunnableEx task = taskCaptor.getValue();
        task.run();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void willProcessAnonymousWithGETAndNoApiKey() throws Exception {
        when(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/rest/in/peace");
        when(request.getRemoteAddr()).thenReturn(clientIP);
        doReturn(null).when(filter).getAuthenticatedUser();

        filter.doFilter(request, response, filterChain);

        verify(processor).processForAnonymousIP(same(clientIP), same(response),
            taskCaptor.capture());

        // verify task is calling filter chain
        RunnableEx task = taskCaptor.getValue();
        task.run();
        verify(filterChain).doFilter(request, response);
    }
}
