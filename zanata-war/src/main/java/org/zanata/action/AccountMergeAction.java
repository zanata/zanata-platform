/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.RegisterService;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("accountMergeAction")
@Scope(ScopeType.PAGE)
public class AccountMergeAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In(value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private AuthenticationManager authenticationManager;

    @In
    private RegisterService registerServiceImpl;

    @Getter
    @Setter
    private String username;

    @In(required = false, scope = ScopeType.SESSION)
    @Out(required = false, scope = ScopeType.SESSION)
    @Getter
    private HAccount obsoleteAccount;

    private OpenIdProviderType providerType;

    private boolean accountsValid;

    public String getProviderType() {
        return providerType != null ? providerType.toString() : "";
    }

    public void setProviderType(String providerType) {
        try {
            this.providerType = OpenIdProviderType.valueOf(providerType);
        } catch (IllegalArgumentException e) {
            this.providerType = OpenIdProviderType.Generic;
        }
    }

    public boolean getAccountsValid() {
        return accountsValid;
    }

    public void loginToMergingAccount() {
        authenticationManager.openIdAuthenticate(this.providerType,
                new AccountMergeAuthCallback());
    }

    public boolean isAccountSelected() {
        return obsoleteAccount != null;
    }

    public void validateAccounts() {
        boolean valid = true;

        // The account to merge in has been authenticated
        if (obsoleteAccount != null) {
            if (obsoleteAccount.getId() == null) {
                FacesMessages.instance().add(ERROR,
                        "Could not find an account for that user.");
                valid = false;
            } else if (authenticatedAccount.getId().equals(
                    obsoleteAccount.getId())) {
                FacesMessages.instance().add(ERROR,
                        "You are attempting to merge the same account.");
                valid = false;
            }
        }

        this.accountsValid = valid;
    }

    public void mergeAccounts() {
        registerServiceImpl
                .mergeAccounts(authenticatedAccount, obsoleteAccount);
        obsoleteAccount = null; // reset the obsolete account
        FacesMessages.instance().add("Your accounts have been merged.");
    }

    public void cancel() {
        // see pages.xml
        obsoleteAccount = null;
    }

    private static class AccountMergeAuthCallback implements
            OpenIdAuthCallback, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public void afterOpenIdAuth(OpenIdAuthenticationResult result) {
            if (result.isAuthenticated()) {
                AccountDAO accountDAO =
                        (AccountDAO) Component.getInstance(AccountDAO.class);
                HAccount account =
                        accountDAO.getByCredentialsId(result
                                .getAuthenticatedId());
                if (account == null) {
                    account = new HAccount(); // In case an account is not found
                }
                Contexts.getSessionContext().set("obsoleteAccount", account); // Outject
                                                                              // the
                                                                              // account
            }
        }

        @Override
        public String getRedirectToUrl() {
            return "/profile/merge_account.seam";
        }
    }
}
