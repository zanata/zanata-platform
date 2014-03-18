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
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.limits.RestCallLimiter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
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
public class RestCallLimiterTest {
    private RestCallLimiter limiter;
    private int maxConcurrent = 4;
    private int maxActive = 2;
    private Logger testLogger = LogManager.getLogger(getClass());
    private Logger testeeLogger = LogManager.getLogger(RestCallLimiter.class);

    @Rule
    public TestRule retryOnceRule = new TestRule() {
        @Override
        public Statement apply(final Statement base,
                final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    boolean failed = false;
                    try {
                        base.evaluate();
                    } catch (Throwable throwable) {
                        log.warn(
                                "{} execution failed. Will retry with debug logger",
                                description.getDisplayName());
                        failed = true;
                    }
                    if (failed) {
                        testeeLogger.setLevel(Level.DEBUG);
                        base.evaluate();
                    }
                }
            };

        }
    };

    @BeforeClass
    public void beforeClass() {
        // set logging to debug
        testLogger.setLevel(Level.DEBUG);
    }

    @BeforeMethod
    public void beforeMethod() {
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(
                        maxConcurrent, maxActive, 1000.0));
    }

    @Test
    public void canOnlyHaveMaximumNumberOfConcurrentRequest() {
        // we don't limit active requests
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(
                        maxConcurrent, maxConcurrent, 1000.0));
        for (int i = 0; i < maxConcurrent; i++) {
            assertThat(limiter.tryAcquire(), Matchers.is(true));
        }
        assertThat(limiter.tryAcquire(), Matchers.is(false));
    }

    @Test
    public void canOnlyHaveMaxActiveConcurrentRequest()
            throws InterruptedException {
        // Given: each thread will take 15ms to do its job
        final int timeSpendDoingWork = 15;

        // When: max concurrent threads are accessing simultaneously
        Callable<Long> callable = taskAcquireThenRelease(timeSpendDoingWork);
        List<Callable<Long>> tasks =
                Collections.nCopies(maxConcurrent, callable);
        ExecutorService executorService =
                Executors.newFixedThreadPool(maxConcurrent);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: only max active threads will be served immediately while others
        // will block until them finish
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);
        log.info("result: {}", timeUsedInMillis);
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new BlockedPredicate());
        assertThat(blocked,
                Matchers.<Long> iterableWithSize(maxConcurrent - maxActive));
    }

    @Test
    public void activeRequestWillBeRateLimited() throws InterruptedException {
        // Given: I am within max active threads count and can do my job RIGHT
        // NOW and permits per second is 1
        final int timeSpendDoingWork = 0;
        double permitsPerSecond = 1;
        limiter.changeRateLimitPermitsPerSecond(permitsPerSecond);

        // When: I start working on heavy duty stuff
        Callable<Long> callable = taskAcquireThenRelease(timeSpendDoingWork);
        List<Callable<Long>> tasks = Collections.nCopies(maxActive, callable);
        ExecutorService executorService =
                Executors.newFixedThreadPool(maxActive);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: I get rate limited (＃｀д´)ﾉ
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new BlockedPredicate());
        assertThat(blocked, Matchers.<Long> iterableWithSize(1));
    }

    @Test
    public void changeMaxConcurrentLimitWillTakeEffectImmediately() {
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(1, 10,
                        1));
        assertThat(limiter.tryAcquire(), Matchers.is(true));
        assertThat(limiter.tryAcquire(), Matchers.is(false));
        limiter.changeConcurrentLimit(2);
        assertThat(limiter.tryAcquire(), Matchers.is(true));
    }

    @Test
    public void changeMaxActiveLimitWhenNoBlockedThreads() {
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(3, 3,
                        1000));
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        limiter.release();

        limiter.changeActiveLimit(3, 2);
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        limiter.release();

        limiter.changeActiveLimit(2, 1);
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        assertThat(limiter.availableActivePermit(), Matchers.is(0));
    }

    @Test
    public void changeMaxActiveLimitWhenHasBlockedThreads()
            throws InterruptedException {
        // Given: only 2 active requests allowed
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(10, 2,
                        1000));

        // When: below requests are fired simultaneously
        // 3 requests (each takes 20ms) and 1 request should block
        Callable<Long> callable = taskAcquireThenRelease(20);
        List<Callable<Long>> requests = Collections.nCopies(3, callable);
        // 1 task to update the active permit with 5ms delay
        // (so that it will happen while there is a blocked request)
        Callable<Long> changeTask = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                // to ensure it happens when there is a blocked request
                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
                limiter.changeActiveLimit(2, 3);
                return -10L;
            }
        };
        // 2 delayed request that will try to acquire after the change
        // (while there is still a request blocking)
        Callable<Long> delayRequest = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                // ensure this happen after change limit took place
                Uninterruptibles
                        .sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
                long blockedTime = acquireAndMeasureBlockedTime();
                limiter.release();
                return blockedTime;
            }
        };
        List<Callable<Long>> delayedRequests =
                Collections.nCopies(2, delayRequest);
        List<Callable<Long>> allTasks = Lists.newArrayList(requests);
        allTasks.add(changeTask);
        allTasks.addAll(delayedRequests);
        ExecutorService executorService =
                Executors.newFixedThreadPool(allTasks.size());
        List<Future<Long>> futures = executorService.invokeAll(allTasks);

        // Then: at the beginning 1 request should be blocked meanwhile change
        // active limit will happen
        // the update request will wait until the blocked request finish and
        // change the limit
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);

        log.info("result: {}", timeUsedInMillis);
        // initial blocked thread's release will happen BEFORE change takes
        // effect (thus permit won't be added to changed semaphore)
        assertThat(limiter.availableActivePermit(), Matchers.is(3));
        // 2 request with no block, 1 update request (indicated as -10),
        // 1 initially blocked request, 2 delay requests blocked in which 1 is
        // responsible for making the change
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new BlockedPredicate());
        assertThat(blocked, Matchers.<Long> iterableWithSize(3));
    }

    // it will measure acquire blocking time and return it
    private Callable<Long> taskAcquireThenRelease(
            final int timeSpendDoingWorkInMillis) {
        return new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                long blockedTime = acquireAndMeasureBlockedTime();
                // spend some time doing some real work
                Uninterruptibles.sleepUninterruptibly(
                        timeSpendDoingWorkInMillis, TimeUnit.MILLISECONDS);
                limiter.release();
                return blockedTime;
            }
        };
    }

    private long acquireAndMeasureBlockedTime() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        limiter.tryAcquire();
        stopwatch.stop();
        long blockedTime = stopwatch.elapsedMillis();
        log.info("blocked: {}", blockedTime);
        return roundToTens(blockedTime);
    }

    private static List<Long> getTimeUsedInMillisRoundedUpToTens(
            List<Future<Long>> futures) {
        return Lists.transform(futures, new Function<Future<Long>, Long>() {
            @Override
            public Long apply(Future<Long> input) {
                try {
                    return roundToTens(input.get());
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

    private static long roundToTens(long arg) {
        return Math.round(arg / 10.0) * 10;
    }

    private static class BlockedPredicate implements Predicate<Long> {
        @Override
        public boolean apply(Long input) {
            return input > 0;
        }
    }
}
