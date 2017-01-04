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

package org.zanata.rest;

import java.util.Optional;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.model.HAccount;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.HttpUtil;
import org.zanata.util.IServiceLocator;

import com.google.common.collect.Lists;

import static org.apache.oltu.oauth2.common.OAuth.HeaderType.AUTHORIZATION;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ZanataRestSecurityInterceptorTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private SecurityTokens securityTokens;
    private ZanataRestSecurityInterceptor securityInterceptor;
    @Mock
    private ContainerRequestContext context;
    @Mock
    private ZanataIdentity identity;
    private MultivaluedHashMap<String, String> headers;
    @Mock
    private ZanataCredentials credential;
    @Mock
    private IServiceLocator serviceLocator;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private Provider<Boolean> allowAnonymousAccessProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        securityInterceptor =
                new ZanataRestSecurityInterceptor(request, securityTokens, identity,
                        true, serviceLocator, allowAnonymousAccessProvider);
        headers = new MultivaluedHashMap<>();

        // some common set up
        when(context.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/rest/some/resource");
        when(context.getHeaders()).thenReturn(headers);
        when(identity.getCredentials()).thenReturn(credential);
        when(allowAnonymousAccessProvider.get()).thenReturn(true);
    }

    @Test
    public void willPassIfAuthenticatedAccountIsNotNull() throws Exception {
        when(serviceLocator
                .getInstance(HAccount.class, new AuthenticatedLiteral()))
                .thenReturn(new HAccount());

        securityInterceptor.filter(context);

        verifyZeroInteractions(request);
    }

    @Test
    public void canAuthenticateUsingValidApiKey() throws Exception {
        headers.put(HttpUtil.USERNAME_HEADER_NAME, Lists.newArrayList("admin"));
        headers.put(HttpUtil.API_KEY_HEADER_NAME, Lists.newArrayList("validApi"));

        when(identity.isLoggedIn()).thenReturn(true);

        securityInterceptor.filter(context);

        verify(credential).setUsername("admin");
        verify(identity).setApiKey("validApi");
        verify(identity).tryLogin();

        verify(context, never()).abortWith(any(Response.class));
    }

    @Test
    public void willAbortIfUsingInvalidApiKey() throws Exception {
        headers.put(HttpUtil.USERNAME_HEADER_NAME, Lists.newArrayList("admin"));
        headers.put(HttpUtil.API_KEY_HEADER_NAME, Lists.newArrayList("invalidApi"));

        when(identity.isLoggedIn()).thenReturn(false);

        securityInterceptor.filter(context);

        verify(credential).setUsername("admin");
        verify(identity).setApiKey("invalidApi");
        verify(identity).tryLogin();

        verify(context).abortWith(any(Response.class));
    }

    @Test
    public void willAbortIfAuthorizationCodeIsInvalid() throws Exception {
        when(request.getParameter(OAuth.OAUTH_CODE)).thenReturn("invalidAuthCode");
        when(securityTokens.findUsernameForAuthorizationCode("invalidAuthCode")).thenReturn(
                Optional.empty());

        securityInterceptor.filter(context);

        verify(identity, never()).tryLogin();

        verify(context).abortWith(any(Response.class));
    }

    @Test
    public void canAuthenticateUsingAccessToken() throws Exception {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer abc123");
        when(securityTokens.findUsernameByAccessToken("abc123")).thenReturn(
                Optional.of("admin"));
        when(identity.isLoggedIn()).thenReturn(true);

        securityInterceptor.filter(context);

        verify(credential).setUsername("admin");
        verify(identity).setRequestUsingOAuth(true);
        verify(identity).tryLogin();

        verify(context, never()).abortWith(any(Response.class));
    }

    @Test
    public void willAbortIfAccessTokenIsInvalid() throws Exception {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer invalid");
        when(securityTokens.findUsernameByAccessToken("invalid")).thenReturn(
                Optional.empty());

        securityInterceptor.filter(context);

        verify(identity, never()).tryLogin();

        verify(context).abortWith(any(Response.class));
    }

    @Test
    public void willAllowAnonymousAccessToReadOnlyResources() throws Exception {
        when(allowAnonymousAccessProvider.get()).thenReturn(true);
        when(context.getMethod()).thenReturn("GET");

        securityInterceptor.filter(context);

        verify(identity, never()).tryLogin();

        verify(context, never()).abortWith(any(Response.class));
    }

    @Test
    public void willAbortAnonymousAccessToNotReadOnlyResources() throws Exception {
        when(context.getMethod()).thenReturn("PUT");

        securityInterceptor.filter(context);

        verify(context).abortWith(any(Response.class));
    }
}
