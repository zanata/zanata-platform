package org.zanata.rest;

import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.zanata.config.SysConfig;
import org.zanata.config.SystemPropertyConfigStore;
import org.zanata.model.HAccount;
import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.HttpUtil;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.googlecode.totallylazy.Either;
import lombok.extern.slf4j.Slf4j;

@Provider
@PreMatching
@SecurityPrecedence
@Slf4j
public class ZanataRestSecurityInterceptor implements ContainerRequestFilter {
    @Context
    private HttpServletRequest request;

    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private ZanataIdentity zanataIdentity;

    @Inject
    @SysConfig(SystemPropertyConfigStore.KEY_SUPPORT_OAUTH)
    private boolean isOAuthEnabled;
    private IServiceLocator serviceLocator = ServiceLocator.instance();


    @SuppressWarnings("unused")
    public ZanataRestSecurityInterceptor() {
    }

    @VisibleForTesting
    protected ZanataRestSecurityInterceptor(HttpServletRequest request,
            SecurityTokens securityTokens, ZanataIdentity zanataIdentity,
            boolean isOAuthEnabled, IServiceLocator serviceLocator) {
        this.request = request;
        this.securityTokens = securityTokens;
        this.zanataIdentity = zanataIdentity;
        this.isOAuthEnabled = isOAuthEnabled;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {
        if (hasAuthenticatedAccount()) {
            // request come from the same browser and the user has logged in
            return;
        }

        Tokens tokens = new Tokens(context, request, isOAuthEnabled);

        if (!tokens.canAuthenticate() &&
                SecurityFunctions.doesRestPathAllowAnonymousAccess(
                        context.getMethod(), context.getUriInfo().getPath())) {
            // if we don't have any information to authenticate but the
            // requesting API allows anonymous access, we let it go
            return;
        }

        if (tokens.canAuthenticateUsingApiKey()) {
            // if apiKey presents, we use apiKey for security check
            zanataIdentity.getCredentials().setUsername(tokens.username.get());
            zanataIdentity.setApiKey(tokens.apiKey.get());
            zanataIdentity.tryLogin();
        } else if (tokens.canAuthenticateUsingOAuth()) {
            Either<String, Response> usernameOrError =
                getAuthenticatedUsernameOrError();
            if (usernameOrError.isRight()) {
                context.abortWith(usernameOrError.right());
                return;
            }
            String username = usernameOrError.left();
            zanataIdentity.getCredentials().setUsername(username);
            zanataIdentity.setRequestUsingOAuth(true);
            zanataIdentity.tryLogin();
        }

        if (!SecurityFunctions.canAccessRestPath(zanataIdentity,
                context.getMethod(), context.getUriInfo().getPath())) {
            log.info("can not authenticate REST request: {}", tokens);
            context.abortWith(Response.status(Status.UNAUTHORIZED).build());
        }
    }

    private Either<String, Response> getAuthenticatedUsernameOrError() {

        String authorizationCode = request.getParameter(OAuth.OAUTH_CODE);

        Optional<String> usernameOpt;
        Optional<String> accessTokenOpt = Optional.empty();
        if (StringUtils.isNotEmpty(authorizationCode)) {
            usernameOpt =
                    securityTokens.findUsernameForAuthorizationCode(authorizationCode);
        } else {
            accessTokenOpt = OAuthUtil.getAccessTokenFromHeader(request);
            usernameOpt = accessTokenOpt.flatMap(
                    token -> securityTokens.findUsernameByAccessToken(token));
        }

        if (!usernameOpt.isPresent()) {
            if (accessTokenOpt.isPresent() && securityTokens.isTokenExpired(accessTokenOpt.get())) {
                log.info("access token [{}] expired", accessTokenOpt.get());
                return Either.right(buildUnauthorizedResponse("access token expired"));
            }
            log.info(
                    "Bad OAuth request, invalid or missing tokens: authorization code: {}, access token: {}",
                    authorizationCode, accessTokenOpt);
            return Either.right(buildUnauthorizedResponse("failed authorization"));
        }
        String username = usernameOpt.get();
        return Either.left(username);

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

    private static class Tokens {

        private final Optional<String> username;
        private final Optional<String> apiKey;
        private final Optional<String> accessToken;
        private final Optional<String> authorizationCode;

        Tokens(ContainerRequestContext context, HttpServletRequest request,
                boolean isOAuthSupported) {
            String username = HttpUtil.getUsername(context.getHeaders());
            String apiKey = HttpUtil.getApiKey(context.getHeaders());
            this.username = optionalNotEmptyString(username);
            this.apiKey = optionalNotEmptyString(apiKey);
            if (isOAuthSupported) {
                accessToken = OAuthUtil.getAccessTokenFromHeader(request);
                String authorizationCode = request.getParameter(OAuth.OAUTH_CODE);
                this.authorizationCode = optionalNotEmptyString(authorizationCode);
            } else {
                accessToken = Optional.empty();
                authorizationCode = Optional.empty();
            }
        }

        private static Optional<String> optionalNotEmptyString(String value) {
            return StringUtils.isNotEmpty(value) ? Optional.of(value) : Optional.empty();
        }

        boolean canAuthenticateUsingApiKey() {
            return username.isPresent() && apiKey.isPresent();
        }

        boolean canAuthenticateUsingOAuth() {
            return authorizationCode.isPresent() || accessToken.isPresent();
        }

        boolean canAuthenticate() {
            return canAuthenticateUsingApiKey() || authorizationCode.isPresent() || accessToken.isPresent();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("username", username)
                    .add("apiKey", apiKey)
                    .add("accessToken", accessToken)
                    .add("authorizationCode", authorizationCode)
                    .toString();
        }
    }
}
