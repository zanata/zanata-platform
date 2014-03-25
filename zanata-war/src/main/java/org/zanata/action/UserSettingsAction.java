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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.service.impl.EmailChangeService;

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

    @In(value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;
    
    @Getter
    @Setter
    @Email
    @NotEmpty
    private String emailAddress;
    
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
}
