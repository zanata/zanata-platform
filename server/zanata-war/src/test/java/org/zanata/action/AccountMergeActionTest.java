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

import java.io.Serializable;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.RegisterService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import org.jglue.cdiunit.InSessionScope;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zanata.action.AccountMergeAction.ObsoleteHolder;
import static org.zanata.action.AccountMergeAction.AccountMergeAuthCallback;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class AccountMergeActionTest implements Serializable {

    @Mock
    @Produces
    @Authenticated
    private HAccount authenticatedAccount;

    @Mock
    @Produces
    private FacesMessages facesMessages;

    @Mock
    @Produces
    private AuthenticationManager authenticationManager;

    @Mock
    @Produces
    private RegisterService registerServiceImpl;

    @Mock
    @Produces
    private ObsoleteHolder obsolete;

    @Mock
    @Produces
    ZanataIdentity zanataIdentity;

    @Mock
    @Produces
    private AccountDAO accountDAO;

    @Inject
    AccountMergeAction accountMergeAction;

    @Test
    public void testAuthCallback() throws Exception {
        HAccount fakeAccount = new HAccount();
        fakeAccount.setUsername("Aloy");
        fakeAccount.setId(1234567890L);
        OpenIdAuthenticationResult result = new OpenIdAuthenticationResult();
        result.setAuthenticatedId("aloy");
        when(accountDAO.getByCredentialsId("aloy")).thenReturn(fakeAccount);

        AccountMergeAuthCallback accountMergeAuthCallback = new AccountMergeAuthCallback(
                accountDAO, obsolete);
        accountMergeAuthCallback.afterOpenIdAuth(result);
        assertThat(obsolete.account.getUsername()).isEqualTo("Aloy");
        assertThat(accountMergeAuthCallback.getRedirectToUrl())
                .isEqualTo("/profile/merge_account.xhtml");
    }

    @Test
    public void validIfNotObsolete() throws Exception {
        accountMergeAction.validateAccounts();
        assertThat(accountMergeAction.getAccountsValid()).isTrue();
    }

    @Test
    public void invalidProviderTypeDefault() {
        String provider = "invalid";
        accountMergeAction.loginToMergingAccount(provider);
        verify(authenticationManager, times(1)).openIdAuthenticate(eq("http://"),
                eq(OpenIdProviderType.Generic), any(AccountMergeAuthCallback.class));
    }

    @Test
    public void validProviderType() {
        String provider = "Fedora";
        accountMergeAction.loginToMergingAccount(provider);
        verify(authenticationManager, times(1)).openIdAuthenticate(
                eq(OpenIdProviderType.Fedora), any(AccountMergeAuthCallback.class));
    }

    @Test
    public void invalidIfIdsAreNull() throws Exception {
        String error = "Could not find an account for that user.";
        obsolete.account = new HAccount();

        doNothing().when(zanataIdentity).checkLoggedIn();
        doNothing().when(facesMessages).addGlobal(SEVERITY_ERROR, error);
        accountMergeAction.validateAccounts();
        verify(facesMessages).addGlobal(SEVERITY_ERROR, error);
        assertThat(accountMergeAction.getAccountsValid()).isFalse();
    }

    @Test
    public void invalidIfIdsAreEqual() throws Exception {
        doCallRealMethod().when(authenticatedAccount).setId(anyLong());
        doCallRealMethod().when(authenticatedAccount).getId();
        String error = "You are attempting to merge the same account.";
        HAccount hAccount = new HAccount();
        hAccount.setId(1234567890L);
        obsolete.account = hAccount;
        authenticatedAccount.setId(1234567890L);

        doNothing().when(zanataIdentity).checkLoggedIn();
        doNothing().when(facesMessages).addGlobal(SEVERITY_ERROR, error);
        accountMergeAction.validateAccounts();
        verify(facesMessages).addGlobal(SEVERITY_ERROR, error);
        assertThat(accountMergeAction.getAccountsValid()).isFalse();
    }

    @Test
    public void validateOpenIDSetting() {
        accountMergeAction.setOpenId("xyz");
        assertThat(accountMergeAction.getOpenId()).isEqualTo("xyz");
    }

    @Test
    public void cancelMerge() {
        obsolete.account = new HAccount();
        assertThat(accountMergeAction.isAccountSelected()).isTrue();
        assertThat(accountMergeAction.cancel()).isEqualTo("cancel");
        assertThat(accountMergeAction.getObsoleteAccount()).isNull();
    }

    @Test
    public void testMergeAction() {
        HAccount account = new HAccount();
        account.setUsername("aloy");
        account.setId(1234567890L);
        obsolete.account = account;
        doNothing().when(registerServiceImpl).mergeAccounts(authenticatedAccount, account);
        accountMergeAction.mergeAccounts();

        assertThat(accountMergeAction.getObsoleteAccount()).isNull();
        verify(registerServiceImpl).mergeAccounts(authenticatedAccount, account);
        verify(facesMessages, times(1)).addGlobal("Your accounts have been merged.");
    }
}
