package org.zanata.limits;

import java.util.concurrent.TimeUnit;
import org.zanata.util.RunnableEx;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is used by RestLimitingSynchronousDispatcher to dispatch API calls
 * via the appropriate RestCallLimiter and have access to seam environment.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RateLimitingProcessor {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RateLimitingProcessor.class);
    // http://tools.ietf.org/html/rfc6585
    public static final int TOO_MANY_REQUEST = 429;
    private RateLimitManager rateLimitManager;
    // for seam to use

    public RateLimitingProcessor() {
        rateLimitManager = RateLimitManager.getInstance();
    }

    private final LeakyBucket logLimiter =
            new LeakyBucket(1, 5, TimeUnit.MINUTES);

    public void processForApiKey(String apiKey, HttpServletResponse response,
            RunnableEx taskToRun) throws Exception {
        process(RateLimiterToken.fromApiKey(apiKey), response, taskToRun);
    }

    public void processForToken(String token, HttpServletResponse response,
            RunnableEx taskToRun) throws Exception {
        process(RateLimiterToken.fromToken(token), response, taskToRun);
    }

    public void processForUser(String username, HttpServletResponse response,
            RunnableEx taskToRun) throws Exception {
        process(RateLimiterToken.fromUsername(username), response, taskToRun);
    }

    public void processForAnonymousIP(String ip, HttpServletResponse response,
            RunnableEx taskToRun) throws Exception {
        process(RateLimiterToken.fromIPAddress(ip), response, taskToRun);
    }

    private void process(RateLimiterToken key, HttpServletResponse response,
            RunnableEx taskToRun) throws Exception {
        RestCallLimiter rateLimiter = rateLimitManager.getLimiter(key);
        log.debug("check semaphore for {}", this);
        if (!rateLimiter.tryAcquireAndRun(taskToRun)) {
            if (logLimiter.tryAcquire()) {
                log.warn(
                        "{} has too many concurrent requests. Returning status 429",
                        key);
            }
            String errorMessage;
            if (key.getType().equals(RateLimiterToken.TYPE.API_KEY)) {
                errorMessage = String.format(
                        "Too many concurrent requests for client API key (maximum is %d)",
                        rateLimiter.getMaxConcurrentPermits());
            } else {
                errorMessage = String.format(
                        "Too many concurrent requests for client \'%s\' (maximum is %d)",
                        key.getValue(), rateLimiter.getMaxConcurrentPermits());
            }
            response.sendError(TOO_MANY_REQUEST, errorMessage);
        }
    }

    @java.beans.ConstructorProperties({ "rateLimitManager" })
    protected RateLimitingProcessor(final RateLimitManager rateLimitManager) {
        this.rateLimitManager = rateLimitManager;
    }
}
