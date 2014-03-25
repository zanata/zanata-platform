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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class LeakyBucketTest {
    @BeforeMethod
    public void beforeMethod() {
//        LogManager.getLogger(LeakyBucket.class.getPackage().getName())
//                .setLevel(Level.DEBUG);
    }

    @Test
    public void willWaitUntilRefill() {
        int refillDuration = 20;
        TimeUnit refillTimeUnit = TimeUnit.MILLISECONDS;
        LeakyBucket bucket = new LeakyBucket(1, refillDuration, refillTimeUnit);

        assertThat(bucket.tryAcquire(), Matchers.is(true));
        assertThat(bucket.tryAcquire(), Matchers.is(false));

        Uninterruptibles.sleepUninterruptibly(refillDuration, refillTimeUnit);

        assertThat(bucket.tryAcquire(), Matchers.is(true));
    }

    @Test
    public void concurrentTest() throws InterruptedException {
        int refillDuration = 10;
        TimeUnit refillTimeUnit = TimeUnit.MILLISECONDS;
        final LeakyBucket bucket =
                new LeakyBucket(1, refillDuration, refillTimeUnit);
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

        // wait enough time and try again
        Uninterruptibles.sleepUninterruptibly(refillDuration, refillTimeUnit);
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
}
