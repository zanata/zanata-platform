package org.zanata.limits;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.spi.HttpResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used by RestLimitingSynchronousDispatcher to dispatch API calls
 * via the appropriate RestCallLimiter and have access to seam environment.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RateLimitingProcessor {
    // http://tools.ietf.org/html/rfc6585
    public static final int TOO_MANY_REQUEST = 429;
    private RateLimitManager rateLimitManager;

    // for seam to use
    public RateLimitingProcessor() {
        rateLimitManager = RateLimitManager.getInstance();
    }

    private final LeakyBucket logLimiter = new LeakyBucket(1, 5,
            TimeUnit.MINUTES);

    public void processApiKey(String apiKey, HttpResponse response,
            Runnable taskToRun)
                    throws Exception {
        process(apiKey, response, taskToRun);
    }

    private void process(String key, HttpResponse response,
            Runnable taskToRun) throws IOException {
        RestCallLimiter rateLimiter = rateLimitManager.getLimiter(key);

        log.debug("check semaphore for {}", this);

        if (!rateLimiter.tryAcquireAndRun(taskToRun)) {
            if (logLimiter.tryAcquire()) {
                log.warn(
                        "{} has too many concurrent requests. Returning status 429",
                        key);
            }
            String errorMessage =
                    String.format(
                            "Too many concurrent requests for this user (maximum is %d)",
                            rateLimiter.getMaxConcurrentPermits());
            response.sendError(TOO_MANY_REQUEST, errorMessage);
        }
    }

    public void processUsername(String username, HttpResponse response,
            Runnable taskToRun) throws IOException {
         process(username, response, taskToRun);
    }
}
