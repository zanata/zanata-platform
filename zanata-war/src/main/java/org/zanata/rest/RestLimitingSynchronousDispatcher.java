package org.zanata.rest;

import java.io.IOException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.UnhandledException;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;
import org.zanata.limits.RateLimitingProcessor;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RestLimitingSynchronousDispatcher extends SynchronousDispatcher {
    static final String API_KEY_ABSENCE_WARNING =
            "You must have a valid API key. You can create one by logging in to Zanata and visiting the settings page.";

    public RestLimitingSynchronousDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        super(providerFactory);
    }

    @Override
    public void invoke(final HttpRequest request, final HttpResponse response) {

        String apiKey = Strings.nullToEmpty(HeaderHelper.getApiKey(request));

        try {
            // we are not validating api key but will rate limit any api key
            if (Strings.isNullOrEmpty(apiKey)
                    && !request.getUri().getPath().contains("/test/")) {
                response.sendError(
                        Response.Status.UNAUTHORIZED.getStatusCode(),
                        API_KEY_ABSENCE_WARNING);
                return;
            }

            Runnable taskToRun = new Runnable() {

                @Override
                public void run() {
                    RestLimitingSynchronousDispatcher.super.invoke(request,
                            response);
                }
            };
            RateLimitingProcessor processor =
                    createRateLimitingRequest(apiKey, response, taskToRun);
            processor.process();

        } catch (UnhandledException e) {
            Throwable cause = e.getCause();
            log.error("Failed to process REST request", cause);
            try {
                // see https://issues.jboss.org/browse/RESTEASY-411
                if (cause instanceof IllegalArgumentException
                        && cause.getMessage().contains(
                                "Failure parsing MediaType")) {
                    response.sendError(Response.Status.UNSUPPORTED_MEDIA_TYPE
                            .getStatusCode(), cause.getMessage());
                } else {
                    response.sendError(Response.Status.INTERNAL_SERVER_ERROR
                            .getStatusCode(), "Error processing Request");
                }

            } catch (IOException ioe) {
                log.error("Failed to send error on failed REST request", ioe);
            }
        } catch (Exception e) {
            log.error("error processing request", e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * Test override-able.
     */
    protected RateLimitingProcessor createRateLimitingRequest(String apiKey,
            HttpResponse response, Runnable runnable) {
        return new RateLimitingProcessor(apiKey, response, runnable);
    }
}
