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
    private RateLimitManager rateLimitManager;
    @Mock
    private Runnable runnable;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        // so that we can verify its interaction
        rateLimitManager = spy(new RateLimitManager());
        processor =
                spy(new RateLimitingProcessor());

        doReturn(rateLimitManager).when(processor).getRateLimitManager();

    }

    @Test
    public void willFirstTryAcquire() throws InterruptedException, IOException,
            ServletException {
        int threads = 2;
        int maxConcurrent = 1;
        when(rateLimitManager.getMaxConcurrent()).thenReturn(maxConcurrent);
        when(rateLimitManager.getMaxActive()).thenReturn(maxConcurrent);
        final CountDownLatch countDownLatch = new CountDownLatch(threads);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                countDownLatch.countDown();
                return invocation.callRealMethod();
            }
        }).when(rateLimitManager).getLimiter(API_KEY);

        // two concurrent requests which will exceed the limit
        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                processor.process(API_KEY, response, runnable);
                return null;
            }
        };
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
        countDownLatch.await(1, TimeUnit.SECONDS);

        // one request will receive 429 and an error message
        verify(response, atLeastOnce()).sendError(eq(429), anyString());
        // one should go through
        verify(runnable).run();
    }
}
