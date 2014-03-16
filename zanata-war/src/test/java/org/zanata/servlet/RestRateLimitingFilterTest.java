package org.zanata.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Matchers;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import org.zanata.rest.HeaderHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RestRateLimitingFilterTest {

    private static final String API_KEY = "apiKey123";
    private RestRateLimitingFilter filter;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private HttpServletRequest request;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private RateLimitingProcessor processor;

    @BeforeMethod
    public void beforeMethod() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);

        when(request.getHeader(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn(
                API_KEY);
        when(request.getRequestURI()).thenReturn("/rest/in/peace");

        filter = spy(new RestRateLimitingFilter());

        // we need to override ContextualHttpServletRequest#run method otherwise
        // Seam will throw exception
        doReturn(processor).when(filter).createRateLimitingRequest(API_KEY, request, response, filterChain);
    }

    @Test
    public void willSkipIfNotHttpServletRequest()
            throws IOException, ServletException {
        filter.doFilter(mock(ServletRequest.class), mock(ServletResponse.class),
                filterChain);

        verifyZeroInteractions(processor);
    }

    @Test
    public void willSkipIfNotRestRequest() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/not/in/peace");

        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(processor);
    }

    @Test
    public void willSkipIfAPIkeyNotPresent()
            throws IOException, ServletException {
        when(request.getHeader(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn(null);
        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(401);
        verifyZeroInteractions(processor);
        assertThat(out.toString(), Matchers.equalTo("You must have a valid API key"));
    }

    @Test
    public void willCallRateLimitingProcessorIfAllConditionsAreMet()
            throws IOException, ServletException {
        filter.doFilter(request, response, filterChain);

        verify(processor).run();
    }
}
