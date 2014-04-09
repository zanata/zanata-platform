package org.zanata.limits;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.jboss.resteasy.spi.HttpResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

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

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        processor = new RateLimitingProcessor(rateLimitManager);
    }

    @Test
    public void restCallLimiterReturnsFalseWillCauseErrorResponse()
            throws Exception {
        RestCallLimiter restCallLimiter = mock(RestCallLimiter.class);
        when(restCallLimiter.tryAcquireAndRun(runnable)).thenReturn(false);
        doReturn(restCallLimiter).when(rateLimitManager).getLimiter(API_KEY);

        processor.process(API_KEY, response, runnable);

        verify(response).sendError(eq(429), anyString());
    }

    @Test
    public void restCallLimiterReturnsTrueWillNotReturnErrorResponse()
            throws Exception {
        RestCallLimiter restCallLimiter = mock(RestCallLimiter.class);
        when(restCallLimiter.tryAcquireAndRun(runnable)).thenReturn(true);
        doReturn(restCallLimiter).when(rateLimitManager).getLimiter(API_KEY);

        processor.process(API_KEY, response, runnable);

        verifyZeroInteractions(response);
    }
}
