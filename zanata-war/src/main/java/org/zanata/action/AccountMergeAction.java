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

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;

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

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private AuthenticationManager authenticationManager;

    @In
    private RegisterService registerServiceImpl;

    @Getter
    @Setter
    private String openId = "http://";

    @In(required = false, scope = ScopeType.SESSION)
    @Out(required = false, scope = ScopeType.SESSION)
    @Getter
    private HAccount obsoleteAccount;

    private boolean accountsValid;

    public boolean getAccountsValid() {
        return accountsValid;
    }

    public void loginToMergingAccount(String provider) {
        if (provider.equalsIgnoreCase("Internal")) {
            // no implementation for internal account merging yet
        } else {
            OpenIdProviderType providerType;
            try {
                providerType = OpenIdProviderType.valueOf(provider);
            } catch (IllegalArgumentException e) {
                providerType = OpenIdProviderType.Generic;
            }

            if (providerType == OpenIdProviderType.Generic) {
                authenticationManager.openIdAuthenticate(openId, providerType,
                        new AccountMergeAuthCallback());
            } else {
                authenticationManager.openIdAuthenticate(providerType,
                        new AccountMergeAuthCallback());
            }
        }
    }

    public boolean isAccountSelected() {
        return obsoleteAccount != null;
    }

    public void validateAccounts() {
        boolean valid = true;

        // The account to merge in has been authenticated
        if (obsoleteAccount != null) {
            if (obsoleteAccount.getId() == null) {
                facesMessages.addGlobal(SEVERITY_ERROR,
                        "Could not find an account for that user.");
                valid = false;
            } else if (authenticatedAccount.getId().equals(
                    obsoleteAccount.getId())) {
                facesMessages.addGlobal(SEVERITY_ERROR,
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
        facesMessages.addGlobal("Your accounts have been merged.");
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
                        ServiceLocator.instance().getInstance(AccountDAO.class);
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
