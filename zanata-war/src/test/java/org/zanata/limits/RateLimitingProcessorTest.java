package org.zanata.limits;

import java.io.IOException;
import javax.servlet.FilterChain;

import org.jboss.resteasy.spi.HttpResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RateLimitingProcessorTest {
    public static final String API_KEY = "apiKey";
    private RateLimitingProcessor processor;
    @Mock
    private HttpResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private RateLimitManager rateLimitManager;
    @Mock
    private Runnable runnable;
    @Mock
    private RestCallLimiter restCallLimiter;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        processor = new RateLimitingProcessor(rateLimitManager);
    }

    @Test
    public void restCallLimiterReturnsFalseWillCauseErrorResponse()
            throws Exception {
        when(restCallLimiter.tryAcquireAndRun(runnable)).thenReturn(false);
        doReturn(restCallLimiter).when(rateLimitManager).getLimiter(
                RateLimiterToken.fromApiKey(API_KEY));

        processor.processForApiKey(API_KEY, response, runnable);

        verify(restCallLimiter).tryAcquireAndRun(runnable);
        verify(response).sendError(eq(429), anyString());
    }

    @Test
    public void restCallLimiterReturnsTrueWillNotReturnErrorResponse()
            throws Exception {
        when(restCallLimiter.tryAcquireAndRun(runnable)).thenReturn(true);
        doReturn(restCallLimiter).when(rateLimitManager).getLimiter(
            RateLimiterToken.fromApiKey(API_KEY));

        processor.processForApiKey(API_KEY, response, runnable);

        verify(restCallLimiter).tryAcquireAndRun(runnable);
        verifyZeroInteractions(response);
    }
}
