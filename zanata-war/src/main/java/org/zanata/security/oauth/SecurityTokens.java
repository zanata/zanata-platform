/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.security.oauth;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.zanata.ApplicationConfiguration;
import org.zanata.config.SysConfig;
import org.zanata.events.LogoutEvent;
import org.zanata.model.HAccount;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.security.annotations.Authenticated;
import org.zanata.util.Introspectable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class SecurityTokens
        implements Serializable, RemovalListener<String, String>,
        Introspectable {
    private OAuthIssuerImpl oAuthIssuer =
            new OAuthIssuerImpl(new MD5Generator());
    /**
     * username -> [clientId : authorizationCode].
     * the entry will live as long as the user session. Once user session times
     * out or user logged out, it will be removed from the cache.
     */
    private Cache<String, Map<String, String>> authorizationCodes =
            CacheBuilder.newBuilder()
                    .build();

    /**
     * access token -> username.
     * stores the current valid access token which has an expiry time from
     * system property.
     */
    private Cache<String, String> accessTokens;


    /**
     * access token -> username.
     * stores the newly expired access token so that we can indicate the user
     * agent the access token has expired.
     */
    private Cache<String, String> expiredAccessToken =
            CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS)
                    .build();

    @Inject
    @SysConfig(ApplicationConfiguration.ACCESS_TOKEN_EXPIRES_IN_SECONDS)
    protected long tokenExpiresInSeconds;

    @PostConstruct
    public void setUp() {
        accessTokens = CacheBuilder.newBuilder()
                .expireAfterWrite(tokenExpiresInSeconds, TimeUnit.SECONDS)
                .removalListener(this)
                .build();
    }

    /**
     * This method listens to accessTokens cache entry expiration. Once an
     * access token expires, it will be put into expiredAccessToken cache so
     * that it can be recognized by next request.
     * @param notification the expired access token
     */
    @Override
    public void onRemoval(RemovalNotification<String, String> notification) {
        expiredAccessToken.put(notification.getKey(), notification.getValue());
    }

    /**
     * remove authorization code once session expires
     */
    public void onLogout(@Observes LogoutEvent event,
            @Authenticated HAccount authenticatedAccount) {
        authorizationCodes.invalidate(authenticatedAccount.getUsername());
    }

    String getAuthorizationCode(String username, String clientId)
            throws OAuthSystemException {
        Map<String, String> clientIdToAuthCode =
                authorizationCodes.getIfPresent(username);
        if (clientIdToAuthCode != null && clientIdToAuthCode.containsKey(clientId)) {
            return clientIdToAuthCode.get(clientId);
        }
        String authorizationCode = oAuthIssuer.authorizationCode();
        try {
            authorizationCodes.get(username,
                    () -> {
                        ConcurrentMap<String, String>
                                map = Maps.newConcurrentMap();
                        map.put(clientId, authorizationCode);
                        return map;
                    });
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
        return authorizationCode;
    }


    public Optional<String> findAuthorizationCode(String username, String clientId) {
        Map<String, String> clientIdToAuthCode =
                authorizationCodes.getIfPresent(username);
        if (clientIdToAuthCode != null && clientIdToAuthCode.containsKey(clientId)) {
            return Optional.of(clientIdToAuthCode.get(clientId));
        }
        return Optional.empty();
    }

    /**
     * Access token and refresh token must be generated before current session
     * expires (which will remove the short lived authorization code).
     *
     * see http://stackoverflow.com/questions/3487991/why-does-oauth-v2-have-both-access-and-refresh-tokens
     *
     * @param account account for a valid authorization code
     * @return OAuthToken which contains access token and refresh token
     * @throws OAuthSystemException
     */
    public OAuthToken generateAccessAndRefreshTokens(HAccount account)
            throws OAuthSystemException {

        // TODO enhancement: use a self contained access token like json web token (JWT)
        // http://stackoverflow.com/questions/12296017/how-to-validate-an-oauth-2-0-access-token-for-a-resource-server
        String accessToken = oAuthIssuer.accessToken();

        BasicOAuthToken oAuthToken =
                new BasicOAuthToken(
                        accessToken,
                        tokenExpiresInSeconds,
                        oAuthIssuer.refreshToken(), null
                );
        accessTokens.put(accessToken, account.getUsername());
        return oAuthToken;
    }

    /**
     * Generates only an access token. Refresh token is already generated in previous session.
     * @param account
     * @param refreshToken
     * @return
     * @throws OAuthSystemException
     */
    public OAuthToken generateAccessTokenOnly(HAccount account,
            String refreshToken) throws OAuthSystemException {
        String accessToken = oAuthIssuer.accessToken();
        BasicOAuthToken oAuthToken =
                new BasicOAuthToken(
                        accessToken,
                        tokenExpiresInSeconds,
                        refreshToken, null
                );
        accessTokens.put(accessToken, account.getUsername());
        return oAuthToken;
    }

    public Optional<String> findUsernameForAuthorizationCode(
            String authorizationCode) {
        Optional<Map.Entry<String, Map<String, String>>> found =
                authorizationCodes.asMap()
                        .entrySet().stream()
                        .filter(entry -> entry.getValue()
                                .containsValue(authorizationCode))
                        .findAny();
        if (found.isPresent()) {
            return found.flatMap(entry -> Optional.ofNullable(entry.getKey()));
        }
        return Optional.empty();
    }

    /**
     * Match accessToken to a username
     * @param accessToken access token for a user
     * @return optional username
     */
    public Optional<String> findUsernameByAccessToken(String accessToken) {
        return Optional.ofNullable(accessTokens.getIfPresent(accessToken));
    }

    public boolean isTokenExpired(String accessToken) {
        return expiredAccessToken.getIfPresent(accessToken) != null;
    }

    public OAuthToken refreshAccessToken(HAccount account, String refreshToken)
            throws OAuthSystemException {

        String accessToken = oAuthIssuer.accessToken();

        accessTokens.put(accessToken, account.getUsername());
        return new BasicOAuthToken(
                accessToken,
                tokenExpiresInSeconds,
                refreshToken, null
        );
    }

    /**
     * Generate full URl from OAuth redirect and authorization code.
     *
     * @param request
     *         original request
     * @param redirectUrl
     *         original redirect url
     * @param authorizationCode
     *         granted authorization code
     * @return full url with authorization code
     */
    public String getRedirectLocationUrl(HttpServletRequest request,
            String redirectUrl, String authorizationCode) {
        try {
            OAuthResponse resp = OAuthASResponse
                    .authorizationResponse(request,
                            HttpServletResponse.SC_FOUND)
                    .setCode(authorizationCode)
                    .location(redirectUrl)
                    .buildQueryMessage();
            return resp.getLocationUri();
        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getIntrospectableId() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public String getFieldValuesAsJSON() {

        Map<String, Map<String, String>> usernameToClientIdAuthCodeMap =
                ImmutableMap.copyOf(authorizationCodes.asMap());

        Map<String, String> accessTokenToUsername =
                ImmutableMap.copyOf(accessTokens.asMap());

        Tokens tokens = new Tokens();
        tokens.accessTokenToUsername = accessTokenToUsername;
        tokens.usernameToClientIdAuthCodeMap = usernameToClientIdAuthCodeMap;
        return DTOUtil.toJSON(tokens);
    }

    @Getter
    @Setter
    protected static class Tokens {
        private Map<String, Map<String, String>> usernameToClientIdAuthCodeMap;
        private Map<String, String> accessTokenToUsername;

    }
}
