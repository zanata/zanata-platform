package org.zanata.rest;

import java.io.IOException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.UnhandledException;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.limits.RateLimitingProcessor;
import org.zanata.model.HAccount;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.zanata.util.ServiceLocator;

/**
 * This class extends RESTEasy's SynchronousDispatcher to limit API calls per
 * API key (via RateLimitingProcessor and RateLimitManager) before dispatching
 * requests.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class RestLimitingSynchronousDispatcher extends SynchronousDispatcher {
    static final String API_KEY_ABSENCE_WARNING =
            "You must have a valid API key. You can create one by logging in to Zanata and visiting the settings page.";
    private final RateLimitingProcessor processor;

    public RestLimitingSynchronousDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        super(providerFactory);
        processor = new RateLimitingProcessor();
    }

    @VisibleForTesting
    RestLimitingSynchronousDispatcher(ResteasyProviderFactory providerFactory,
            RateLimitingProcessor processor) {
        super(providerFactory);
        this.processor = processor;
    }

    @Override
    public void invoke(final HttpRequest request, final HttpResponse response) {

        HAccount authenticatedUser = getAuthenticatedUser();
        String apiKey = HeaderHelper.getApiKey(request);

        try {
            // we are not validating api key but will rate limit any api key
            if (authenticatedUser == null && Strings.isNullOrEmpty(apiKey)) {
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

            if (authenticatedUser == null) {
                processor.processApiKey(apiKey, response, taskToRun);
            } else if (!Strings.isNullOrEmpty(authenticatedUser.getApiKey())) {
                processor.processApiKey(authenticatedUser.getApiKey(),
                        response, taskToRun);
            } else {
                processor.processUsername(authenticatedUser.getUsername(),
                        response, taskToRun);
            }

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

    @VisibleForTesting
    protected HAccount getAuthenticatedUser() {
        return ServiceLocator.instance().getInstance(
                JpaIdentityStore.AUTHENTICATED_USER, HAccount.class);
    }
}
