package org.zanata.limits;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class LeakyBucketTest {

    private LeakyBucket bucket;
    private final int refillDuration = 20;
    private final TimeUnit refillTimeUnit = TimeUnit.MILLISECONDS;
    private final long timeOverRefillDuration = TimeUnit.NANOSECONDS.convert(
            refillDuration, refillTimeUnit);
    @Mock
    private LeakyBucket.TimeTracker timeTracker;

    @BeforeMethod
    public void beforeMethod() {
        LogManager.getLogger(LeakyBucket.class.getPackage().getName())
                .setLevel(Level.DEBUG);
        MockitoAnnotations.initMocks(this);
        bucket =
                new LeakyBucket(1, refillDuration, refillTimeUnit, timeTracker);
    }

    @Test
    public void willWaitUntilRefill() throws InterruptedException {

        assertThat(bucket.tryAcquire(), Matchers.is(true));
        assertThat(bucket.tryAcquire(), Matchers.is(false));

        when(timeTracker.timePassed()).thenReturn(timeOverRefillDuration);
        assertThat(bucket.tryAcquire(), Matchers.is(true));
    }

    @Test
    public void concurrentTest() throws InterruptedException {
        Callable<Boolean> callable = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return bucket.tryAcquire();
            }
        };

        int threads = 3;
        List<Callable<Boolean>> callables =
                Collections.nCopies(threads, callable);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> futures = executorService.invokeAll(callables);

        List<Boolean> result = getFutureResult(futures);
        assertThat(result, Matchers.containsInAnyOrder(true, false, false));

        // here we simulate that we have waited enough time and try again
        when(timeTracker.timePassed()).thenReturn(timeOverRefillDuration, 0L,
                0L);

        List<Future<Boolean>> callAgain = executorService.invokeAll(callables);
        assertThat(getFutureResult(callAgain),
                Matchers.containsInAnyOrder(true, false, false));
    }

    private static List<Boolean> getFutureResult(List<Future<Boolean>> futures) {
        return Lists.transform(futures,
                new Function<Future<Boolean>, Boolean>() {
                    @Override
                    public Boolean apply(Future<Boolean> input) {
                        try {
                            return input.get();
                        } catch (Exception e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });
    }

    @Test
    public void willMakeUpTheRefillWhenTimePassed() throws InterruptedException {
        LeakyBucket bucket = new LeakyBucket(2, 20, TimeUnit.MILLISECONDS);

        assertThat(bucket.tryAcquire(), Matchers.is(true));
        assertThat(bucket.tryAcquire(), Matchers.is(true));
        assertThat(bucket.tryAcquire(), Matchers.is(false));

        // refill rate is 1 per 100 ms so after 40 ms it should've filled up.
        Thread.sleep(40);

        assertThat(bucket.tryAcquire(), Matchers.is(true));
        assertThat(bucket.tryAcquire(), Matchers.is(true));
    }
}
