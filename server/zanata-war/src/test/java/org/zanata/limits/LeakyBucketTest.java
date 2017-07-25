package org.zanata.limits;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LeakyBucketTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LeakyBucketTest.class);

    private LeakyBucket bucket;
    private final int refillDuration = 20;
    private final TimeUnit refillTimeUnit = TimeUnit.MILLISECONDS;
    private final long timeOverRefillDuration =
            TimeUnit.NANOSECONDS.convert(refillDuration, refillTimeUnit);
    @Mock
    private LeakyBucket.TimeTracker timeTracker;

    @Before
    public void beforeMethod() {
        // LogManager.getLogger(LeakyBucket.class.getPackage().getName())
        // .setLevel(Level.DEBUG);
        MockitoAnnotations.initMocks(this);
        bucket = new LeakyBucket(1, refillDuration, refillTimeUnit,
                timeTracker);
    }

    @Test
    public void willWaitUntilRefill() throws InterruptedException {
        assertThat(bucket.tryAcquire()).isTrue();
        assertThat(bucket.tryAcquire()).isFalse();
        when(timeTracker.timePassed()).thenReturn(timeOverRefillDuration);
        assertThat(bucket.tryAcquire()).isTrue();
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
        assertThat(result).contains(true, false, false);
        // here we simulate that we have waited enough time and try again
        when(timeTracker.timePassed()).thenReturn(timeOverRefillDuration, 0L,
                0L);
        List<Future<Boolean>> callAgain = executorService.invokeAll(callables);
        assertThat(getFutureResult(callAgain)).contains(true, false, false);
    }

    private static List<Boolean>
            getFutureResult(List<Future<Boolean>> futures) {
        return Lists.transform(futures,
                new Function<Future<Boolean>, Boolean>() {

                    @Override
                    public Boolean apply(Future<Boolean> input) {
                        try {
                            return input.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
}
