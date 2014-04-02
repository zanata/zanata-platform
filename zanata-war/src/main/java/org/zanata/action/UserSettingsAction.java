/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.CredentialsDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.security.HCredentials;
import org.zanata.model.security.HOpenIdCredentials;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.openid.FedoraOpenIdProvider;
import org.zanata.security.openid.GoogleOpenIdProvider;
import org.zanata.security.openid.MyOpenIdProvider;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.security.openid.YahooOpenIdProvider;
import org.zanata.service.impl.EmailChangeService;

import javax.persistence.EntityManager;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.INFO;

/**
 * This is an action class that should eventually replace the
 * {@link org.zanata.action.ProfileAction} class as the UI controller for user
 * settings.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link org.zanata.action.ProfileAction}
 */
@Name("userSettingsAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class UserSettingsAction {

    @In(create = true)
    protected Renderer renderer;
    
    @In
    private EmailChangeService emailChangeService;
    
    @In
    private PersonDAO personDAO;

    @In
    private AccountDAO accountDAO;

    @In
    private IdentityManager identityManager;

    @In
    private AuthenticationManager authenticationManager;

    @In(value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;
    
    @Getter
    @Setter
    @Email
    @NotEmpty
    private String emailAddress;

    @Getter
    @Setter
    @Size(min = 6, max = 20)
    private String newPassword;

    @Getter
    @Setter
    private String oldPassword;

    @Getter
    @Setter
    private String openId;
    
    @Create
    public void onCreate() {
        emailAddress = authenticatedAccount.getPerson().getEmail();
    }
    
    public void updateEmail() {
        if(!isEmailAddressValid(emailAddress)) {
            FacesMessages.instance().addToControl("email",
                    "This email address is already taken");
            return;
        }

        HPerson person =
                personDAO.findById(authenticatedAccount.getPerson().getId(),
                        true);
        if (!authenticatedAccount.getPerson().getEmail().equals(emailAddress)) {
            String activationKey =
                    emailChangeService.generateActivationKey(person,
                            emailAddress);
            // setActivationKey(activationKey);
            renderer.render("/WEB-INF/facelets/email/email_validation.xhtml");
            FacesMessages
                    .instance()
                    .add("You will soon receive an email with a link to activate your email account change.");
        }
    }

    protected boolean isEmailAddressValid(String email) {
        HPerson person = personDAO.findByEmail(email);
        return person == null
                || person.getAccount().equals(authenticatedAccount);
    }

    public void changePassword() {
        if (isPasswordSet()
                && !identityManager.authenticate(
                authenticatedAccount.getUsername(), oldPassword)) {
            FacesMessages.instance().addToControl("oldPassword",
                    "Old password is incorrect, please check and try again.");
            return;
        }

        new RunAsOperation() {
            public void execute() {
                identityManager.changePassword(
                        authenticatedAccount.getUsername(), newPassword);
            }
        }.addRole("admin").run();

        FacesMessages.instance().add(
                "Your password has been successfully changed.");
    }

    public boolean isPasswordSet() {
        return authenticatedAccount.getPasswordHash() != null;
    }

    public List<HCredentials> getUserCredentials() {
        HAccount account = accountDAO.findById(authenticatedAccount.getId());
        return Lists.newArrayList(account.getCredentials());
    }

    /**
     * Valid Types:
     * google, yahoo, fedora, openid for everything else
     */
    public String getCredentialsType(HCredentials credentials) {
        if( new GoogleOpenIdProvider().accepts(credentials.getUser()) ) {
            return "google";
        }
        else if( new FedoraOpenIdProvider().accepts(credentials.getUser()) ) {
            return "fedora";
        }
        else if( new YahooOpenIdProvider().accepts(credentials.getUser()) ) {
            return "yahoo";
        }
        else {
            return "openid";
        }
    }

    public String getCredentialsTypeDisplayName(String type) {
        if(type.equals("google"))
            return "Google";
        else if(type.equals("fedora"))
            return "Fedora";
        if(type.equals("yahoo"))
            return "Yahoo";
        if(type.equals("openid"))
            return "Open Id";
        else
            return "Unknown";
    }

    public void remove(HCredentials toRemove) {
        HAccount account =
                accountDAO.findById(authenticatedAccount.getId(), false);
        account.getCredentials().remove(toRemove);
        //userCredentials = new ArrayList<HCredentials>(account.getCredentials()); // Reload
        // the
        // credentials
        accountDAO.makePersistent(account);
        accountDAO.flush();
    }

    public void verifyCredentials(String providerTypeStr) {
        OpenIdProviderType providerType =
                OpenIdProviderType.valueOf(providerTypeStr);
        HOpenIdCredentials newCreds = new HOpenIdCredentials();
        newCreds.setAccount(authenticatedAccount);
        if( providerType == OpenIdProviderType.Generic ) {
            authenticationManager.openIdAuthenticate(openId, providerType,
                    new CredentialsCreationCallback(newCreds));
        }
        else {
            authenticationManager.openIdAuthenticate(providerType,
                    new CredentialsCreationCallback(newCreds));
        }
    }

    /**
     * Callback for credential creation.
     */
    private static class CredentialsCreationCallback implements
            OpenIdAuthCallback, Serializable {
        private static final long serialVersionUID = 1L;
        private HCredentials newCredentials;

        private CredentialsCreationCallback(HCredentials newCredentials) {
            this.newCredentials = newCredentials;
        }

        @Override
        public void afterOpenIdAuth(OpenIdAuthenticationResult result) {
            // Save the credentials after a successful authentication
            if (result.isAuthenticated()) {
                this.newCredentials.setUser(result.getAuthenticatedId());
                this.newCredentials.setEmail(result.getEmail());
                // NB: Seam component injection won't work on callbacks
                EntityManager em =
                        (EntityManager) Component.getInstance("entityManager");
                CredentialsDAO credentialsDAO =
                        (CredentialsDAO) Component
                                .getInstance(CredentialsDAO.class);

                Conversation.instance().begin(true, false); // (To retain
                // messages)
                FacesMessages.instance().clear();

                if (credentialsDAO.findByUser(result.getAuthenticatedId()) != null) {
                    FacesMessages.instance().add(ERROR,
                            "jsf.identities.invalid.Duplicate", null,
                            "Duplicate identity",
                            "This Identity is already in use.");
                } else {
                    em.persist(this.newCredentials);
                    FacesMessages
                            .instance()
                            .add(INFO, "jsf.identities.IdentityAdded", null,
                                    "Identity Added",
                                    "Your new identity has been added to this account.");
                }
            }
        }

        @Override
        public String getRedirectToUrl() {
            return "/dashboard/settings?cid="
                    + Conversation.instance().getId(); // keep the same
            // conversation
        }
    }
}
