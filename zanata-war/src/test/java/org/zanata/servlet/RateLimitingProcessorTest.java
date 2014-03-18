package org.zanata.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

import static org.hamcrest.MatcherAssert.assertThat;
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
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    private RateLimitManager rateLimitManager;
    private StringWriter responseOut;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        // so that we can verify its interaction
        rateLimitManager = spy(new RateLimitManager());
        processor =
                spy(new RateLimitingProcessor(API_KEY, request, response,
                        filterChain));

        doReturn(applicationConfiguration).when(processor)
                .getApplicationConfiguration();
        doReturn(rateLimitManager).when(processor).getRateLimiterHolder();

        responseOut = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseOut));
    }

    @Test
    public void willSkipIfRateLimitSwitchIsOff() throws Exception {
        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(false);

        processor.process();

        verify(filterChain).doFilter(request, response);
        verifyZeroInteractions(rateLimitManager);
    }

    @Test
    public void willFirstTryAcquire() throws InterruptedException, IOException,
            ServletException {

        when(rateLimitManager.getLimitConfig()).thenReturn(
                new RestRateLimiter.RateLimitConfig(1, 1, 100.0));
        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // to ensure release won't happen immediately before another
                // concurrent request try acquire
                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
                return null;
            }
        }).when(filterChain).doFilter(request, response);

        // two concurrent requests which will exceed the limit
        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                processor.process();
                return null;
            }
        };
        int threads = 2;
        List<Callable<Void>> callables = Collections.nCopies(threads, callable);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Void>> futures = executorService.invokeAll(callables);
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }
        }

        // one request will receive 429
        verify(response, atLeastOnce()).setStatus(429);
        // one should go through
        verify(filterChain).doFilter(request, response);
        // semaphore is released
        assertThat(rateLimitManager.getIfPresent(API_KEY)
                .availableConcurrentPermit(), Matchers.equalTo(1));
    }

    @Test
    public void willReleaseSemaphoreWhenThereIsException() throws IOException,
            ServletException {
        when(rateLimitManager.getLimitConfig()).thenReturn(
                new RestRateLimiter.RateLimitConfig(1, 1, 100.0));
        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(true);
        doThrow(new RuntimeException("bad")).when(filterChain).doFilter(
                request, response);

        try {
            processor.process();
        } catch (Exception e) {
            // I know
        }

        assertThat(rateLimitManager.getIfPresent(API_KEY)
                .availableConcurrentPermit(), Matchers.equalTo(1));
    }
}
