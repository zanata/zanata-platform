package org.zanata.limits;

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

    public void
            process(String apiKey, HttpResponse response, Runnable taskToRun)
                    throws Exception {
        RestCallLimiter rateLimiter = rateLimitManager.getLimiter(apiKey);

        log.debug("check semaphore for {}", this);

        if (!rateLimiter.tryAcquireAndRun(taskToRun)) {
            if (logLimiter.tryAcquire()) {
                log.warn(
                        "{} has too many concurrent requests. Returning status 429",
                        apiKey);
            }
            String errorMessage =
                    String.format(
                            "Too many concurrent request for this API key (maximum is %d)",
                            rateLimiter.getMaxConcurrentPermits());
            response.sendError(TOO_MANY_REQUEST, errorMessage);
        }
    }

}
