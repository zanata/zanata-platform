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

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
            throws Failure, WebApplicationException {
        String apiKey = HeaderHelper.getApiKey(request);
        if (apiKey == null) {
            return null;
        }
        ActiveApiKeys apiKeys = ActiveApiKeys.getInstance();
        apiKeys.setApiKeyForCurrentThread(apiKey);

        RateLimiterHolder rateLimiterHolder = RateLimiterHolder.getInstance();
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


    @Override
    public void postProcess(ServerResponse response) {
        RateLimiterHolder.getInstance().releaseSemaphoreForCurrentThread();
    }

    @RequiredArgsConstructor(staticName = "of")
    @ToString
    private static class ContextInfo {
        private final String apiKey;
        private final String requestPath;
        private final RestRateLimiter rateLimiter;
    }

}
