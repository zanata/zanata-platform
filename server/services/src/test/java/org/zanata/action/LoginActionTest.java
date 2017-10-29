/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.*;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import static org.mockito.Mockito.*;
import static org.zanata.security.AuthenticationType.INTERNAL;
import static org.zanata.security.AuthenticationType.SAML2;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import com.google.common.collect.Sets;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class LoginActionTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Mock
    @Produces
    private ZanataIdentity identity;
    @Mock
    @Produces
    private ZanataCredentials credentials;
    @Mock
    @Produces
    private AuthenticationManager authenticationManager;
    @Mock
    @Produces
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    @Produces
    private AccountDAO accountDAO;
    @Mock
    @Produces
    private UserRedirectBean userRedirect;

    @Produces
    @Named("dswidParam")
    private String dswidParam = "";
    @Produces
    @Named("dswidQuery")
    String dswidQuery = "";
    @Produces
    @ServerPath
    private String serverPath = "/";
    @Produces @Mock
    private WindowContext windowContext;
    @Produces
    @ContextPath
    String contextPath = "";

    @Produces
    private AuthenticationType authenticationType = AuthenticationType.INTERNAL;

    @Mock
    @Produces
    private UrlUtil urlUtil;

    @Inject
    private LoginAction loginAction;

    @Test
    public void usesEmailWhenEmailIsValid() throws Exception {

        HAccount account = new HAccount();
        account.setUsername("aloy");
        when(accountDAO.getByEmail("aloy@test.com")).thenReturn(account);
        when(credentials.getAuthType()).thenReturn(AuthenticationType.INTERNAL);
        doCallRealMethod().when(credentials).setUsername(anyString());
        when(credentials.getUsername()).thenCallRealMethod();
        when(authenticationManager.internalLogin()).thenReturn("loggedIn");

        loginAction.setUsername("aloy@test.com");
        loginAction.setPassword("password");
        loginAction.login();

        verify(accountDAO, times(1)).getByEmail("aloy@test.com");
        verify(credentials, times(1)).setUsername("aloy");
    }
    @Test
    public void loginContinueToPreviousTest() {
        HAccount account = new HAccount();
        account.setUsername("aloy");
        String url = "/explore";

        when(accountDAO.getByEmail("aloy@test.com")).thenReturn(account);
        when(credentials.getAuthType()).thenReturn(AuthenticationType.INTERNAL);
        doCallRealMethod().when(credentials).setUsername(anyString());
        when(credentials.getUsername()).thenCallRealMethod();
        when(authenticationManager.internalLogin()).thenReturn("loggedIn");
        when(authenticationManager.isAuthenticated()).thenReturn(true);
        when(userRedirect.isRedirect()).thenReturn(true);
        when(userRedirect.getUrl()).thenReturn(url);

        loginAction.setUsername("aloy@test.com");
        loginAction.setPassword("password");
        loginAction.login();

        verify(urlUtil).redirectToInternalWithoutContextPath(url);
        verify(accountDAO, times(1)).getByEmail("aloy@test.com");
        verify(credentials, times(1)).setUsername("aloy");
    }

    @Test
    public void willRedirectIfSaml2IsTheOnlyAuthType() {
        when(applicationConfiguration.getAuthTypes()).thenReturn(
                Sets.newHashSet(SAML2));
        String ssoUrl = "/account/ssologin";
        when(urlUtil.singleSignOnPage()).thenReturn(ssoUrl);

        loginAction.redirectIfOnlySSOEnabled();

        verify(urlUtil).redirectToInternal(ssoUrl);
    }

    @Test
    public void willNotRedirectIfSaml2IsNotTheOnlyAuthType() {
        when(applicationConfiguration.getAuthTypes()).thenReturn(
                Sets.newHashSet(SAML2, INTERNAL));

        loginAction.redirectIfOnlySSOEnabled();

        verifyZeroInteractions(urlUtil);
    }
}
