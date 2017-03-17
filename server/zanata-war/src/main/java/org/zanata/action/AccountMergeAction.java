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
import javax.annotation.Nullable;
import javax.enterprise.context.SessionScoped;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.Synchronized;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("accountMergeAction")
@ViewScoped
@Model
@Transactional
public class AccountMergeAction implements Serializable {

    @SessionScoped
    @Synchronized
    static class ObsoleteHolder implements Serializable {

        private static final long serialVersionUID = 1L;
        @Nullable
        HAccount account;
    }

    private static final long serialVersionUID = 1L;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private AuthenticationManager authenticationManager;
    @Inject
    private RegisterService registerServiceImpl;
    private String openId = "http://";
    @Inject
    private ObsoleteHolder obsolete;
    private boolean accountsValid;
    @Inject
    private ZanataIdentity zanataIdentity;

    @Inject
    private AccountDAO accountDAO;
    @Inject
    private ObsoleteHolder obsoleteHolder;

    @Nullable
    public HAccount getObsoleteAccount() {
        return obsolete.account;
    }

    private void setObsoleteAccount(@Nullable HAccount obsoleteAccount) {
        obsolete.account = obsoleteAccount;
    }

    @PostConstruct
    public void onCreate() {
        zanataIdentity.checkLoggedIn();
    }

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
                        new AccountMergeAuthCallback(accountDAO, obsoleteHolder));
            } else {
                authenticationManager.openIdAuthenticate(providerType,
                        new AccountMergeAuthCallback(accountDAO, obsoleteHolder));
            }
        }
    }

    public boolean isAccountSelected() {
        return getObsoleteAccount() != null;
    }

    public void validateAccounts() {
        boolean valid = true;
        // The account to merge in has been authenticated
        HAccount obsoleteAccount = getObsoleteAccount();
        if (obsoleteAccount != null) {
            if (obsoleteAccount.getId() == null) {
                facesMessages.addGlobal(SEVERITY_ERROR,
                        "Could not find an account for that user.");
                valid = false;
            } else if (authenticatedAccount.getId()
                    .equals(obsoleteAccount.getId())) {
                facesMessages.addGlobal(SEVERITY_ERROR,
                        "You are attempting to merge the same account.");
                valid = false;
            }
        }
        this.accountsValid = valid;
    }

    public void mergeAccounts() {
        registerServiceImpl.mergeAccounts(authenticatedAccount,
                getObsoleteAccount());
        setObsoleteAccount(null); // reset the obsolete account
        facesMessages.addGlobal("Your accounts have been merged.");
    }

    public String cancel() {
        // see faces-config.xml
        setObsoleteAccount(null);
        return "cancel";
    }

    static class AccountMergeAuthCallback
            implements OpenIdAuthCallback, Serializable {

        private static final long serialVersionUID = 1L;
        private AccountDAO accountDAO;
        private ObsoleteHolder obsoleteHolder;

        AccountMergeAuthCallback(AccountDAO accountDAO, ObsoleteHolder obsoleteHolder) {
            this.accountDAO = accountDAO;
            this.obsoleteHolder = obsoleteHolder;
        }

        @Override
        public void afterOpenIdAuth(OpenIdAuthenticationResult result) {
            if (result.isAuthenticated()) {
                obsoleteHolder.account = ObjectUtils.firstNonNull(accountDAO
                        .getByCredentialsId(result.getAuthenticatedId()),
                        new HAccount());
            }
        }

        @Override
        public String getRedirectToUrl() {
            return "/profile/merge_account.xhtml";
        }
    }

    public String getOpenId() {
        return this.openId;
    }

    public void setOpenId(final String openId) {
        this.openId = openId;
    }
}
