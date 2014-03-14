package org.zanata.servlet;

import java.io.IOException;
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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import org.zanata.rest.HeaderHelper;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.zanata.seam.SeamAutowire.getComponentName;
import static org.zanata.seam.SeamAutowire.instance;

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
    private RateLimiterHolder rateLimiterHolder;
    @Mock
    private HttpServletRequest request;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() throws ServletException, IOException {
        MockitoAnnotations.initMocks(this);

        when(request.getHeader(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn(
                API_KEY);
        when(request.getRequestURI()).thenReturn("/rest/in/peace");

        rateLimiterHolder = spy(new RateLimiterHolder());
        RestRateLimitingFilter limitingReleaseFilter =
                instance()
                        .reset()
                        .use(getComponentName(ApplicationConfiguration.class),
                                applicationConfiguration)
                        // to ensure it's application scoped a.k.a singleton
                        .use(getComponentName(RateLimiterHolder.class),
                                rateLimiterHolder)
                        .autowire(RestRateLimitingFilter.class);
        filter = spy(limitingReleaseFilter);

        // we need to override ContextualHttpServletRequest#run method otherwise
        // Seam will throw exception
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // this is to ensure we return a new instance for each thread
                return new RestRateLimitingFilter.RateLimitingRequest(API_KEY,
                        request) {
                    @Override
                    public void run() throws ServletException, IOException {
                        try {
                            process();
                        } catch (Exception e) {
                            throw Throwables.propagate(e);
                        }
                    }
                };
            }
        }).when(filter).createRateLimitingRequest(request, API_KEY);
    }

    @Test
    public void willSkipIfNotRestRequest() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/not/in/peace");

        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(rateLimiterHolder);
    }

    @Test
    public void willSkipIfRateLimitSwitchIsOff() throws IOException,
            ServletException {

        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(rateLimiterHolder);
    }

    @Test
    public void willFirstTryAcquire() throws InterruptedException, IOException,
            ServletException {

        when(rateLimiterHolder.getLimitConfig()).thenReturn(
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
                filter.doFilter(request, response, filterChain);
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
        assertThat(rateLimiterHolder.getIfPresent(API_KEY)
                .availableConcurrentPermit(), Matchers.equalTo(1));
    }

    @Test
    public void havingExceptionWillReleaseSemaphore() throws IOException,
            ServletException {
        when(rateLimiterHolder.getLimitConfig()).thenReturn(
                new RestRateLimiter.RateLimitConfig(1, 1, 100.0));
        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(true);
        doThrow(new RuntimeException("bad")).when(filterChain).doFilter(
                request, response);

        try {
            filter.doFilter(request, response, filterChain);
        } catch (Exception e) {
            // I know
        }

        assertThat(rateLimiterHolder.getIfPresent(API_KEY)
                .availableConcurrentPermit(), Matchers.equalTo(1));
    }
}
