package org.zanata.rest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import javaslang.control.Either;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.config.AllowAnonymousAccess;
import org.zanata.config.SupportOAuth;
import org.zanata.model.HAccount;
import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.annotations.NoSecurityCheck;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.HttpUtil;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;

/**
 * This class is responsible for checking for all REST requests: a) valid
 * authentication presents when accessing protected resources (e.g. no open to
 * anonymous access) b) no authentication when accessing resource that allows
 * anonymous access This class will not do specific access control for
 * individual end-points. Individual REST services can and should control their
 * specific access rules by using annotation or identity.checkPermission etc.
 * <p>
 * NOTE: This security filter is for REST call only and work on the HTTP request
 * level. We've also registered {@code org.apache.deltaspike.security.impl.extension.SecurityInterceptor}
 * as a CDI interceptor in beans.xml. It will handle CDI bean level security on
 * method level.
 *
 * @see org.zanata.security.Identity
 * @see org.zanata.security.annotations.CheckRole
 * @see org.zanata.security.annotations.CheckLoggedIn
 */
// TODO rename this class to Filter since it's no longer a seam JAX-RS interceptor
@ApplicationScoped
public class ZanataRestSecurityInterceptor implements ContainerRequestFilter {
    private static final Logger log =
            LoggerFactory.getLogger(ZanataRestSecurityInterceptor.class);
    private HttpServletRequest request;

    private SecurityTokens securityTokens;

    private ZanataIdentity zanataIdentity;

    private boolean isOAuthEnabled;
    private IServiceLocator serviceLocator = ServiceLocator.instance();

    private Provider<Boolean> allowAnonymousAccessProvider;


    @SuppressWarnings("unused")
    public ZanataRestSecurityInterceptor() {
    }

    @Inject
    protected ZanataRestSecurityInterceptor(@DeltaSpike HttpServletRequest request,
            SecurityTokens securityTokens, ZanataIdentity zanataIdentity,
            @SupportOAuth boolean isOAuthEnabled, IServiceLocator serviceLocator,
            @AllowAnonymousAccess Provider<Boolean> allowAnonymousAccessProvider) {
        this.request = request;
        this.securityTokens = securityTokens;
        this.zanataIdentity = zanataIdentity;
        this.isOAuthEnabled = isOAuthEnabled;
        this.serviceLocator = serviceLocator;
        this.allowAnonymousAccessProvider = allowAnonymousAccessProvider;
    }

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {
        if (hasAuthenticatedAccount()) {
            // request come from the same browser and the user has logged in
            return;
        }

        RestCredentials restCredentials = new RestCredentials(context, request, isOAuthEnabled);

        if (restCredentials.hasApiKey()) {
            // if apiKey presents, we use apiKey for security check
            zanataIdentity.getCredentials().setUsername(restCredentials.username.get());
            zanataIdentity.setApiKey(restCredentials.apiKey.get());
            zanataIdentity.tryLogin();
            if (!SecurityFunctions.canAccessRestPath(zanataIdentity,
                    context.getUriInfo().getPath())) {
                String message = InvalidApiKeyUtil
                        .getMessage(restCredentials.username.get(),
                                restCredentials.apiKey.get());
                log.info("can not authenticate REST request: {}", message);
                context.abortWith(Response.status(Status.UNAUTHORIZED)
                        .entity(message)
                        .build());
            }
        } else if (restCredentials.hasOAuthToken()) {
            Either<Response, String> usernameOrError =
                getAuthenticatedUsernameOrError();
            if (usernameOrError.isLeft()) {
                context.abortWith(usernameOrError.getLeft());
                return;
            }
            String username = usernameOrError.get();
            zanataIdentity.getCredentials().setUsername(username);
            zanataIdentity.setRequestUsingOAuth(true);
            // login will always success since the check was done above
            // here the tryLogin() will just set up the correct system state
            zanataIdentity.tryLogin();
        } else if (!allowAnonymousAccessProvider.get() ||
                !HttpUtil.isReadMethod(context.getMethod())){
            // special cases for path such as '/test/' or '/oauth/' are now
            // handled by having annotation @NoSecurityCheck on those API
            // methods/classes. ZanataRestSecurityBinder will ensure that this
            // ContainerRequestFilter will not be called for those annotated
            // services.

            // if we don't have any information to authenticate and the
            // requesting API does NOT allow anonymous access
            log.info("can not authenticate REST request: {}", restCredentials);
            context.abortWith(Response.status(Status.UNAUTHORIZED)
                    .header("Content-Type", MediaType.TEXT_PLAIN)
                    .entity("User authentication required for REST request")
                    .build());
        }
    }

    private Either<Response, String> getAuthenticatedUsernameOrError() {

        Optional<String> usernameOpt;
        Optional<String> accessTokenOpt =
                OAuthUtil.getAccessTokenFromHeader(request);
        usernameOpt = accessTokenOpt.flatMap(
                token -> securityTokens.findUsernameByAccessToken(token));

        if (!usernameOpt.isPresent()) {
            log.info(
                    "Bad OAuth request, invalid or expired tokens: access token: {}",
                    accessTokenOpt);
            return Either.left(buildUnauthorizedResponse(
                    "Bad OAuth request, invalid or expired tokens: access token [" +
                            accessTokenOpt + "]"));
        }
        String username = usernameOpt.get();
        return Either.right(username);
    }

    private Response buildUnauthorizedResponse(String message) {
        OAuthResponse oauthResponse = null;
        try {
            oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .buildHeaderMessage();
        } catch (OAuthSystemException e1) {
            return buildServerErrorResponse(message);
        }

        return Response
                .status(oauthResponse.getResponseStatus()).header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(
                        OAuth.HeaderType.WWW_AUTHENTICATE)).build();
    }

    private Response buildServerErrorResponse(String message) {
        return Response.serverError().entity(message).build();
    }

    private boolean hasAuthenticatedAccount() {
        return serviceLocator
                .getInstance(HAccount.class, new AuthenticatedLiteral()) !=
                null;
    }

    /**
     * Encapsulate all possible authentication values from a REST request.
     */
    private static class RestCredentials {

        private final Optional<String> username;
        private final Optional<String> apiKey;
        private final Optional<String> accessToken;

        RestCredentials(ContainerRequestContext context, HttpServletRequest request,
                boolean isOAuthSupported) {
            String username = HttpUtil.getUsername(context.getHeaders());
            String apiKey = HttpUtil.getApiKey(context.getHeaders());
            this.username = optionalNotEmptyString(username);
            this.apiKey = optionalNotEmptyString(apiKey);
            if (isOAuthSupported) {
                accessToken = OAuthUtil.getAccessTokenFromHeader(request);
            } else {
                accessToken = Optional.empty();
            }
        }

        private static Optional<String> optionalNotEmptyString(String value) {
            return StringUtils.isNotEmpty(value) ? Optional.of(value) : Optional.empty();
        }

        boolean hasApiKey() {
            return username.isPresent() && apiKey.isPresent();
        }

        boolean hasOAuthToken() {
            return accessToken.isPresent();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("username", username)
                    .add("apiKey", apiKey)
                    .add("accessToken", accessToken)
                    .toString();
        }
    }

    /**
     * This will use the {@code NoSecurityCheck} annotation and only apply
     * security request filter to endpoints that don't have that annotation.
     *
     * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
     */
    @javax.ws.rs.ext.Provider
    @PreMatching
    public static class ZanataRestSecurityBinder implements DynamicFeature {
        @Inject
        private ZanataRestSecurityInterceptor securityInterceptor;

        @Override
        public void configure(ResourceInfo resourceInfo,
                FeatureContext featureContext) {
            Class<?> clazz = resourceInfo.getResourceClass();
            Method method = resourceInfo.getResourceMethod();
            if (!method.isAnnotationPresent(NoSecurityCheck.class)
                    && !clazz.isAnnotationPresent(NoSecurityCheck.class)) {
                featureContext.register(securityInterceptor);
            }
        }
    }
}
