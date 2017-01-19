package org.zanata.limits;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zanata.util.RunnableEx;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.zanata.util.RunnableEx.runnable;
// each test will be run INVOCATIONS times

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(Parameterized.class)
public class RestCallLimiterTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RestCallLimiterTest.class);

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[INVOCATIONS][0]);
    }

    // set true for shorter timeouts while debugging
    private static final boolean DEBUG = false;
    private RestCallLimiter limiter;
    private static final int INVOCATIONS = 20;
    private static final int N_THREADS = 20;
    private static final int maxConcurrent = 4;
    private static final int maxActive = 2;
    private ExecutorService threadPool;
    // most invocations run in far less than 200ms,
    // but not the first (due to init costs)
    private static final long DEBUG_TIMEOUT = 200;
    private static final long SAFE_TIMEOUT = 10000;
    private static final long TIMEOUT = DEBUG ? DEBUG_TIMEOUT : SAFE_TIMEOUT;
    private static final TimeUnit UNIT = MILLISECONDS;
    private static final RunnableEx nullRunnable = () -> {
    };
    private volatile CountDownLatch awakenLatch;
    // @BeforeClass
    // public static void beforeClass() {
    // // set logging to debug
    // LogManager.getLogger(getClass()).setLevel(Level.DEBUG);
    // LogManager.getLogger(RestCallLimiter.class).setLevel(Level.DEBUG);
    // }

    @Before
    public void beforeMethod() {
        limiter = new RestCallLimiter(maxConcurrent, maxActive);
        awakenLatch = new CountDownLatch(1);
        threadPool = Executors.newFixedThreadPool(N_THREADS);
    }

    @After
    public void afterMethod() throws InterruptedException {
        awakenBlockedRunnables();
        threadPool.shutdown();
        threadPool.awaitTermination(TIMEOUT, UNIT);
    }

    private void submitTasks(int numTasks, final CountDownLatch execsStarted,
            final CountDownLatch expectedRejects,
            final CountDownLatch execsFinished, final String testName) {
        final RunnableEx blockingTask = () -> {
            log.debug("execution started");
            execsStarted.countDown();
            blockUntilAwoken(testName);
            log.debug("execution finished");
        };
        for (int i = 0; i < numTasks; i++) {
            final int jobNum = i;
            threadPool.submit(runnable(() -> {
                if (limiter.tryAcquireAndRun(blockingTask)) {
                    log.debug("request #" + jobNum + ": acquired and executed");
                    execsFinished.countDown();
                } else {
                    log.debug("request #" + jobNum + ": rejected");
                    expectedRejects.countDown();
                }
            }));
        }
    }

    private void blockUntilAwoken(String testName) {
        try {
            awakenLatch.await(TIMEOUT, UNIT);
        } catch (Exception e) {
            // don't throw an exception, or it becomes difficult
            // to diagnose failing tests (at least in TestNG)
            log.warn("Exception in Runnable for test " + testName);
        }
    }

    private void awakenBlockedRunnables() {
        // tell blocking Runnables to wake up
        awakenLatch.countDown();
    }

    @Test
    public void shouldRejectRequestsAboveMaxConcurrent() throws Exception {
        String testName = "shouldRejectRequestsAboveMaxConcurrent";
        // we don't limit active requests
        limiter = new RestCallLimiter(maxConcurrent, maxConcurrent);
        int excessRequests = 3;
        int numOfThreads = maxConcurrent + excessRequests;
        final CountingLatch execsStarted =
                new CountingLatch(maxConcurrent, "execs started");
        final CountingLatch expectedRejects =
                new CountingLatch(excessRequests, "rejected requests");
        final CountingLatch execsFinished =
                new CountingLatch(maxConcurrent, "execs finished");
        submitTasks(numOfThreads, execsStarted, expectedRejects, execsFinished,
                testName);
        SoftAssertions softly = new SoftAssertions();
        // last requests which exceed the limit will fail to get permit
        expectedRejects.awaitAndVerify(softly);
        // requests that are within the max concurrent limit should get permit
        execsStarted.awaitAndVerify(softly);
        // accepted jobs should eventually finish
        awakenBlockedRunnables();
        execsFinished.awaitAndVerify(softly);
        softly.assertAll();
    }

    @Test
    public void shouldBlockRequestsAboveMaxActive() throws Exception {
        String testName = "shouldBlockRequestsAboveMaxActive";
        limiter = new RestCallLimiter(maxConcurrent, maxActive);
        int expectedBlocksNum = maxConcurrent - maxActive;
        CountingLatch expectedBlocks =
                new CountingLatch(expectedBlocksNum, "blocks");
        BlockCountingSemaphore blockCountingSemaphore =
                new BlockCountingSemaphore(maxActive, expectedBlocks);
        limiter.changeActiveSemaphore(blockCountingSemaphore);
        // Given: each thread will take some time to do its job
        // When: max concurrent threads are accessing simultaneously
        // Then: only max active threads will be served immediately while others
        // will block until they finish
        int numTasks = maxConcurrent;
        final CountingLatch execsStarted =
                new CountingLatch(maxActive, "execs started");
        final CountingLatch expectedRejects =
                new CountingLatch(0, "rejected requests");
        final CountingLatch execsFinished =
                new CountingLatch(numTasks, "execs finished");
        submitTasks(numTasks, execsStarted, expectedRejects, execsFinished,
                testName);
        SoftAssertions softly = new SoftAssertions();
        execsStarted.awaitAndVerify(softly);
        expectedBlocks.awaitAndVerify(softly);
        expectedRejects.awaitAndVerify(softly);
        execsFinished.assertEquals(softly, 0);
        softly.assertThat(blockCountingSemaphore.numOfBlockedThreads())
                .as("blocked threads").isEqualTo(expectedBlocksNum);
        softly.assertAll();
        awakenBlockedRunnables();
        execsFinished.awaitAndVerify();
    }

    @Test
    public void shouldChangeMaxConcurrent() throws Exception {
        String testName = "shouldChangeMaxConcurrent";
        int maxConcurrent = 1;
        int maxActive = 10;
        // we start off with only 1 concurrent permit
        limiter = new RestCallLimiter(maxConcurrent, maxActive);
        int numOfThreads = 2;
        final CountingLatch execsStarted =
                new CountingLatch(maxConcurrent, "1st execs started");
        int numRejects = numOfThreads - maxConcurrent;
        final CountingLatch expectedRejects =
                new CountingLatch(numRejects, "1st rejected requests");
        final CountingLatch execsFinished =
                new CountingLatch(maxConcurrent, "1st execs finished");
        submitTasks(numOfThreads, execsStarted, expectedRejects, execsFinished,
                testName);
        execsStarted.awaitAndVerify();
        // the one and only concurrent permit should be in use
        assertThat(limiter.availableConcurrentPermit()).isEqualTo(0);
        expectedRejects.awaitAndVerify();
        awakenBlockedRunnables();
        execsFinished.awaitAndVerify();
        // all concurrent permits returned
        assertThat(limiter.availableConcurrentPermit())
                .isEqualTo(maxConcurrent);
        // ensure that second round of jobs will hold permits simultaneously
        awakenLatch = new CountDownLatch(1);
        // change permit to match number of threads
        int newMaxConcurrent = 2;
        limiter.setMaxConcurrent(newMaxConcurrent);
        final CountingLatch secondExecsStarted =
                new CountingLatch(newMaxConcurrent, "2nd execs started");
        final CountingLatch secondExecsFinished =
                new CountingLatch(newMaxConcurrent, "2nd execs finished");
        final CountingLatch secondExpectedRejects =
                new CountingLatch(0, "2nd rejected requests");
        submitTasks(numOfThreads, secondExecsStarted, secondExpectedRejects,
                secondExecsFinished, testName);
        secondExecsStarted.awaitAndVerify();
        secondExpectedRejects.awaitAndVerify();
        // all concurrent permits in use
        assertThat(limiter.availableConcurrentPermit()).isEqualTo(0);
        awakenBlockedRunnables();
        secondExecsFinished.awaitAndVerify();
        assertThat(limiter.availableConcurrentPermit())
                .isEqualTo(newMaxConcurrent);
    }

    @Test
    public void shouldChangeMaxActiveWhenNoThreadsAreBlocked()
            throws Exception {
        limiter = new RestCallLimiter(3, 3);
        limiter.tryAcquireAndRun(nullRunnable);
        assertThat(limiter.availableActivePermit()).isEqualTo(3);
        limiter.setMaxActive(2);
        // change may not happen until next request comes in
        limiter.tryAcquireAndRun(nullRunnable);
        assertThat(limiter.availableActivePermit()).isEqualTo(2);
        limiter.setMaxActive(1);
        limiter.tryAcquireAndRun(nullRunnable);
        assertThat(limiter.availableActivePermit()).isEqualTo(1);
    }

    @Test
    public void shouldChangeMaxActiveWhenThreadsAreBlocked()
            throws InterruptedException {
        String testName = "shouldChangeMaxActiveWhenThreadsAreBlocked";
        // Given: only 2 active requests allowed
        int maxConcurrent = 10;
        int maxActive = 2;
        limiter = new RestCallLimiter(maxConcurrent, maxActive);
        int expectedBlocksNum = 1;
        CountingLatch expectedBlocks =
                new CountingLatch(expectedBlocksNum, "blocks");
        BlockCountingSemaphore blockCountingSemaphore =
                new BlockCountingSemaphore(maxActive, expectedBlocks);
        limiter.changeActiveSemaphore(blockCountingSemaphore);
        int numTasks = maxActive + expectedBlocksNum;
        final CountingLatch execsStarted =
                new CountingLatch(maxActive, "1st execs started");
        final CountingLatch execsFinished =
                new CountingLatch(numTasks, "1st execs finished");
        final CountingLatch expectedRejects =
                new CountingLatch(0, "1st rejected requests");
        submitTasks(numTasks, execsStarted, expectedRejects, execsFinished,
                testName);
        // When: below requests are fired simultaneously
        // 3 requests, 1 request should block
        execsStarted.awaitAndVerify();
        expectedRejects.awaitAndVerify();
        expectedBlocks.awaitAndVerify();
        int newMaxActive = 3;
        limiter.setMaxActive(newMaxActive);
        // 2 delayed request that will try to acquire after the change
        // (while there is still a request blocking)
        int secondNumTasks = 2;
        final CountingLatch secondExecsStarted =
                new CountingLatch(secondNumTasks, "2nd execs started");
        final CountingLatch secondExecsFinished =
                new CountingLatch(secondNumTasks, "2nd execs finished");
        final CountingLatch secondExpectedRejects =
                new CountingLatch(0, "2nd rejected requests");
        submitTasks(secondNumTasks, secondExecsStarted, expectedRejects,
                secondExecsFinished, testName);
        // Then: at the beginning 1 request should be blocked meanwhile change
        // active limit will happen
        // the update request will change the semaphore so new requests will be
        // operating on new semaphore object
        // ensure that all requests (old and new) have finished
        awakenBlockedRunnables();
        execsFinished.awaitAndVerify();
        secondExecsStarted.awaitAndVerify();
        secondExecsFinished.awaitAndVerify();
        secondExpectedRejects.awaitAndVerify();
        // initial blocked thread's release will operate on old semaphore which
        // was thrown away
        assertThat(limiter.availableActivePermit()).isEqualTo(newMaxActive);
    }

    @Test
    public void shouldReleaseSemaphoreWhenRunnableThrowsException()
            throws Exception {
        RunnableEx runnable = () -> {
            throw new RuntimeException("bad");
        };
        try {
            limiter.tryAcquireAndRun(runnable);
            fail("RuntimeException should have been propagated");
        } catch (Exception e) {
            assertThat(limiter.availableConcurrentPermit())
                    .isEqualTo(maxConcurrent);
            assertThat(limiter.availableActivePermit()).isEqualTo(maxActive);
        }
    }

    @Test
    public void shouldNotRejectWhenLimitsAreDisabled()
            throws InterruptedException {
        String testName = "shouldNotRejectWhenLimitsAreDisabled";
        limiter = new RestCallLimiter(0, 0);
        int numTasks = 12;
        final CountingLatch execsStarted =
                new CountingLatch(numTasks, "execs started");
        final CountingLatch expectedRejects =
                new CountingLatch(0, "rejected requests");
        final CountingLatch execsFinished =
                new CountingLatch(numTasks, "execs finished");
        submitTasks(numTasks, execsStarted, expectedRejects, execsFinished,
                testName);
        execsStarted.awaitAndVerify();
        expectedRejects.awaitAndVerify();
        awakenBlockedRunnables();
        execsFinished.awaitAndVerify();
    }

    private static class BlockCountingSemaphore extends Semaphore {
        private static final long serialVersionUID = 1L;
        private final AtomicInteger blockCounter = new AtomicInteger(0);
        private final CountDownLatch blockLatch;

        public BlockCountingSemaphore(int permits,
                CountDownLatch expectedBlocksLatch) {
            super(permits);
            blockLatch = expectedBlocksLatch;
        }

        @Override
        public boolean tryAcquire() {
            boolean got = super.tryAcquire();
            if (!got) {
                blockCounter.incrementAndGet();
                blockLatch.countDown();
            }
            return got;
        }

        @Override
        public boolean tryAcquire(long timeout, TimeUnit unit)
                throws InterruptedException {
            // check for instant permit, and count as a block if unavailable:
            boolean got = this.tryAcquire();
            if (got) {
                return true;
            } else {
                return super.tryAcquire(timeout, unit);
            }
        }

        public int numOfBlockedThreads() {
            return blockCounter.get();
        }
    }

    private static class CountingLatch extends CountDownLatch {
        private final int expectedCount;
        private final AtomicInteger actualCount;
        private String name;

        public CountingLatch(int expectedCount, String name) {
            super(expectedCount);
            this.expectedCount = expectedCount;
            this.actualCount = new AtomicInteger(0);
            this.name = name;
        }

        @Override
        public void countDown() {
            actualCount.incrementAndGet();
            super.countDown();
        }

        private void awaitOrTimeout() throws InterruptedException {
            if (!super.await(TIMEOUT, UNIT)) {
                throw new RuntimeException(
                        "Timed out waiting for \'" + name + "\' latch " + this);
            }
        }

        public void awaitAndVerify() throws InterruptedException {
            awaitOrTimeout();
            assertThat(actualCount.get()).as(name).isEqualTo(expectedCount);
        }

        public void awaitAndVerify(SoftAssertions softly)
                throws InterruptedException {
            awaitOrTimeout();
            softly.assertThat(actualCount.get()).as(name)
                    .isEqualTo(expectedCount);
        }

        public void assertEquals(SoftAssertions softly, int expected)
                throws InterruptedException {
            softly.assertThat(actualCount.get()).as(name).isEqualTo(expected);
        }
    }
}
