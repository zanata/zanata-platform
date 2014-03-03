package org.zanata.seam.interceptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.isomorphism.util.TokenBucket;
import org.jboss.seam.intercept.InvocationContext;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RateLimiterTest {
    private RateLimiter rateLimiter;
    @Mock
    private InvocationContext invocationContext;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        rateLimiter = Mockito.spy(new RateLimiter());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                TokenBucket tokenBucket =
                        (TokenBucket) invocation.getArguments()[1];
                if (!tokenBucket.tryConsume()) {
                    throw new RuntimeException("you can't consume!!");
                }
                return null;
            }
        }).when(rateLimiter).consumeFromTokenBucket(any(InvocationContextMeasurer.class), any(TokenBucket.class));
    }

    @Test
    public void canStartRateLimitLongRunningMethod() throws Exception {
        final String apiKey = "api";
        final InvocationContextMeasurer measurer = spy(
                InvocationContextMeasurer.of(invocationContext, apiKey, 1));
        // call it the first time should enable the rate limit
        doReturn(true).when(measurer).isTakingTooLong(anyLong());

        rateLimiter.consume(measurer);

        // subsequent calls will have rate limiting enabled and two concurrent call will have one failed
        Callable<Result> callable = new Callable<Result>() {

            @Override
            public Result call() throws Exception {
                try {
                    rateLimiter.consume(measurer);
                } catch (RuntimeException e) {
                    assertThat(e.getMessage(), Matchers.equalTo("you can't consume!!"));
                    return Result.Ratelimited;
                }
                return Result.Normal;
            }
        };

        final int threads = 2;
        List<Callable<Result>> tasks = Collections.nCopies(threads, callable);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Result>> futures = executorService.invokeAll(tasks);
        List<Result> result = Lists.transform(futures,
                new Function<Future<Result>, Result>() {
                    @Override
                    public Result apply(Future<Result> input) {
                        try {
                            return input.get();
                        }
                        catch (Exception e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });

        assertThat(result, Matchers.containsInAnyOrder(Result.Normal, Result.Ratelimited));

    }

    enum Result {
        Normal, Ratelimited
    }
}
