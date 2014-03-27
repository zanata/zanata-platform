package org.zanata.limits;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;

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

    @Mock
    private Runnable runntable;

    @BeforeClass
    public void beforeClass() {
        // set logging to debug
        // testeeLogger.setLevel(Level.DEBUG);
        testLogger.setLevel(Level.DEBUG);
    }

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(
                        maxConcurrent, maxActive, 1000.0));
        // the first time this method is executed it seems to cause 10-30ms
        // overhead by itself (moving things to heap and register classes maybe)
        // This will reduce that overhead for actual tests
        limiter.tryAcquireAndRun(runntable);
    }

    @Test
    public void canOnlyHaveMaximumNumberOfConcurrentRequest()
            throws InterruptedException, ExecutionException {
        // we don't limit active requests
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(
                        maxConcurrent, maxConcurrent, 1000.0));

        // to ensure threads are actually running concurrently
        runnableWillTakeTime(20);

        Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return limiter.tryAcquireAndRun(runntable);
            }
        };
        int numOfThreads = maxConcurrent + 1;
        List<Boolean> result =
                submitConcurrentTasksAndGetResult(task, numOfThreads);
        log.debug("result: {}", result);
        // requests that are within the max concurrent limit should get permit
        Iterable<Boolean> successRequest =
                Iterables.filter(result, new Predicate<Boolean>() {
                    @Override
                    public boolean apply(Boolean input) {
                        return input;
                    }
                });
        assertThat(successRequest,
                Matchers.<Boolean> iterableWithSize(maxConcurrent));
        // last request which exceeds the limit will fail to get permit
        assertThat(result, Matchers.hasItem(false));
    }

    static <T> List<T> submitConcurrentTasksAndGetResult(Callable<T> task,
            int numOfThreads) throws InterruptedException, ExecutionException {
        List<Callable<T>> tasks = Collections.nCopies(numOfThreads, task);


        ExecutorService service = Executors.newFixedThreadPool(numOfThreads);

        List<Future<T>> futures = service.invokeAll(tasks);
        return Lists.transform(futures, new Function<Future<T>, T>() {
            @Override
            public T apply(Future<T> input) {
                try {
                    return input.get();
                }
                catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

    @Test
    public void canOnlyHaveMaxActiveConcurrentRequest()
            throws InterruptedException, ExecutionException {
        // Given: each thread will take some time to do its job
        final int timeSpentDoingWork = 20;
        runnableWillTakeTime(timeSpentDoingWork);

        // When: max concurrent threads are accessing simultaneously
        Callable<Long> callable =
                taskToAcquireAndMeasureTime();

        // Then: only max active threads will be served immediately while others
        // will block until them finish
        List<Long> timeBlockedInMillis =
                submitConcurrentTasksAndGetResult(callable, maxConcurrent);
        log.debug("result: {}", timeBlockedInMillis);
        Iterable<Long> blocked =
                Iterables.filter(timeBlockedInMillis, new Predicate<Long>() {
                    @Override
                    public boolean apply(Long input) {
                        return input >= timeSpentDoingWork * 2;
                    }
                });
        assertThat(blocked,
                Matchers.<Long> iterableWithSize(maxConcurrent - maxActive));
    }

    void runnableWillTakeTime(final int timeSpentDoingWork) {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Uninterruptibles.sleepUninterruptibly(timeSpentDoingWork,
                        TimeUnit.MILLISECONDS);
                return null;
            }
        }).when(runntable).run();
    }

    @Test
    public void activeRequestWillBeRateLimited() throws InterruptedException {
        // Given: I am within max active threads count and can do my job RIGHT
        // NOW and permits per second is 1
        double permitsPerSecond = 1;
        limiter.changeRateLimitPermitsPerSecond(permitsPerSecond);

        // When: I start working on heavy duty stuff
        Callable<Long> callable = taskToAcquireAndMeasureTime();
        List<Callable<Long>> tasks = Collections.nCopies(maxActive, callable);
        ExecutorService executorService =
                Executors.newFixedThreadPool(maxActive);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: I get rate limited (＃｀д´)ﾉ
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new Predicate<Long>() {

                    @Override
                    public boolean apply(Long input) {
                        return input > 0;
                    }
                });
        assertThat(blocked, Matchers.<Long> iterableWithSize(1));
    }

    @Test
    private void changeMaxConcurrentLimitWillTakeEffectImmediately()
            throws ExecutionException, InterruptedException {
        runnableWillTakeTime(10);

        // we start off with only 1 concurrent permit
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(1, 10,
                        1));
        Callable<Boolean> task = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return limiter.tryAcquireAndRun(runntable);
            }
        };

        int numOfThreads = 2;
        List<Boolean> result =
                submitConcurrentTasksAndGetResult(task, numOfThreads);
        assertThat(result, Matchers.containsInAnyOrder(true, false));
        assertThat(limiter.availableConcurrentPermit(), Matchers.is(1));

        // change permit to match number of threads
        limiter.changeConcurrentLimit(1, numOfThreads);

        List<Boolean> resultAfterChange =
                submitConcurrentTasksAndGetResult(task, numOfThreads);
        assertThat(resultAfterChange, Matchers.contains(true, true));

        assertThat(limiter.availableConcurrentPermit(),
                Matchers.is(numOfThreads));
    }

    @Test
    public void changeMaxActiveLimitWhenNoBlockedThreads() {
        limiter =
                new RestCallLimiter(new RestCallLimiter.RateLimitConfig(3, 3,
                        1000));
        limiter.tryAcquireAndRun(runntable);

        limiter.changeActiveLimit(3, 2);
        // change won't happen until next request comes in
        limiter.tryAcquireAndRun(runntable);
        assertThat(limiter.availableActivePermit(), Matchers.is(2));

        limiter.changeActiveLimit(2, 1);

        limiter.tryAcquireAndRun(runntable);
        assertThat(limiter.availableActivePermit(), Matchers.is(1));
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
        final int timeSpentDoingWork = 20;
        runnableWillTakeTime(timeSpentDoingWork);
        Callable<Long> callable =
                taskToAcquireAndMeasureTime();
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
                return tryAcquireAndMeasureTime();
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
        // the update request will change the semaphore so new requests will be
        // operating on new semaphore object
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);

        log.info("result: {}", timeUsedInMillis);
        // initial blocked thread's release will operate on old semaphore which
        // was thrown away
        assertThat(limiter.availableActivePermit(), Matchers.is(3));
    }

    @Test
    public void willReleaseSemaphoreWhenThereIsException() throws IOException,
            ServletException {
        doThrow(new RuntimeException("bad")).when(runntable).run();

        try {
            limiter.tryAcquireAndRun(runntable);
        } catch (Exception e) {
            // I know
        }

        assertThat(limiter.availableConcurrentPermit(),
                Matchers.equalTo(maxConcurrent));
        assertThat(limiter.availableActivePermit(), Matchers.equalTo(maxActive));
    }

    @Test
    public void zeroPermitMeansNoLimit() {
        limiter =
                new RestCallLimiter(
                        new RestCallLimiter.RateLimitConfig(0, 0, 0));

        assertThat(limiter.tryAcquireAndRun(runntable), Matchers.is(true));
        assertThat(limiter.tryAcquireAndRun(runntable), Matchers.is(true));
        assertThat(limiter.tryAcquireAndRun(runntable), Matchers.is(true));
    }

    /**
     * it will measure acquire blocking time and return it.
     */
    private Callable<Long> taskToAcquireAndMeasureTime() {
        return new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return tryAcquireAndMeasureTime();
            }
        };
    }

    private long tryAcquireAndMeasureTime() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        limiter.tryAcquireAndRun(runntable);
        stopwatch.stop();
        long timeSpent = stopwatch.elapsedMillis();
        log.debug("real time try acquire and run task takes: {}", timeSpent);
        return roundToTens(timeSpent);
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
        return arg / 10 * 10;
    }

}
