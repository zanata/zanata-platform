package org.zanata.rest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.interception.HeaderDecoratorPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@HeaderDecoratorPrecedence
@ServerInterceptor
@Slf4j
public class ZanataRestRateLimiterInterceptor implements PreProcessInterceptor,
        PostProcessInterceptor {
    private ThreadLocal<String> apiKeys = new ThreadLocal<String>();

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
            throws Failure, WebApplicationException {
        String apiKey = HeaderHelper.getApiKey(request);
        if (apiKey == null) {
            return null;
        }
        apiKeys.set(apiKey);

        RateLimiterHolder rateLimiterHolder = getRateLimiterHolder();
        final RestRateLimiter.RateLimitConfig limitConfig =
                rateLimiterHolder.getLimitConfig();

        RestRateLimiter rateLimiter;
        try {
            rateLimiter = rateLimiterHolder.get(apiKey, new Callable<RestRateLimiter>() {
                @Override
                public RestRateLimiter call() throws Exception {
                    return new RestRateLimiter(limitConfig);
                }
            });
        }
        catch (ExecutionException e) {
            throw new WebApplicationException(e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        rateLimiterHolder.put(apiKey, rateLimiter);
        ContextInfo contextInfo = ContextInfo.of(apiKey, request.getUri().getPath(), rateLimiter);
        log.debug("check semaphore for {}", contextInfo);
        if (!rateLimiter.tryAcquireConcurrentPermit()) {
            // throw exception instead of return status code directly.
            // exception will prevent postProcess being called and
            // semaphore.release will add additional permit to it.
            Response response = Response.status(
                    Response.Status.FORBIDDEN)
                    .entity("too many concurrent requests").build();
            throw new WebApplicationException(response);
        }
        rateLimiter.acquire();
        log.debug("finish blocking for {}", contextInfo);
        return null;
    }

    private static RateLimiterHolder getRateLimiterHolder() {
        return (RateLimiterHolder) Component
                .getInstance(RateLimiterHolder.class);
    }

    @Override
    public void postProcess(ServerResponse response) {
        String apiKey = apiKeys.get();
        if (apiKey != null) {
            apiKeys.remove();
            RestRateLimiter rateLimiter = getRateLimiterHolder().getIfPresent(apiKey);
            log.debug("releasing semaphore for:{} - {}", apiKey, rateLimiter);
            rateLimiter.release();
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    @ToString
    private static class ContextInfo {
        private final String apiKey;
        private final String requestPath;
        private final RestRateLimiter rateLimiter;
    }

}
