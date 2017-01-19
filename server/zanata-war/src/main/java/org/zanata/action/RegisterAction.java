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

import java.io.Serializable;
import javax.enterprise.inject.Model;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.Size;
import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.validator.constraints.NotEmpty;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.action.validator.NotDuplicateEmail;
import org.zanata.config.AllowPublicRegistration;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.AuthorizationException;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Named("register")
@GroupedConversationScoped
@Model
@Transactional
public class RegisterAction implements HasUserDetail, Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RegisterAction.class);

    private static final long serialVersionUID = -7883627570614588182L;
    @Inject
    private GroupedConversation conversation;
    @Inject
    private EntityManager entityManager;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    RegisterService registerServiceImpl;
    @Inject
    PersonDAO personDAO;
    @Inject
    EmailService emailServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    @AllowPublicRegistration
    private boolean allowRegistration;
    private String username;
    private String email;
    private String password;
    private String humanField;
    private boolean valid;
    private HPerson person;

    public String redirectIfLoggedIn() {
        if (identity.isLoggedIn()) {
            urlUtil.redirectToInternal(urlUtil.dashboardUrl());
        }
        return null;
    }

    public void checkRegistrationAvailability() {
        if (!allowRegistration) {
            throw new AuthorizationException(
                    "Public registration is disabled on this instance. Please contact admin to create your account");
        }
    }

    public HPerson getPerson() {
        if (person == null)
            person = new HPerson();
        return person;
    }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotDuplicateEmail(message = "This email address is already taken.")
    @Override
    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    // @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$",
    // message="Password is not secure enough!")

    @NotEmpty
    @Size(min = 6, max = 1024)
    public String getPassword() {
        return password;
    }

    @Size(min = 0, max = 0)
    public String getHumanField() {
        return humanField;
    }

    public void setHumanField(String humanField) {
        this.humanField = humanField;
    }

    public void validateUsername(String username) {
        try {
            entityManager
                    .createQuery("from HAccount a where a.username = :username")
                    .setParameter("username", username).getSingleResult();
            valid = false;
            facesMessages.addToControl("username",
                    "This username is not available");
        } catch (NoResultException e) {
            // pass
        }
    }

    public void validateHumanField() {
        if (humanField != null && humanField.length() > 0) {
            valid = false;
            facesMessages.addGlobal(SEVERITY_ERROR,
                    "You have filled a field that was not meant for humans.");
            humanField = null;
        }
    }

    public String register() {
        valid = true;
        validateUsername(getUsername());
        validateHumanField();
        if (!isValid()) {
            return null;
        }
        final String user = getUsername();
        final String pass = getPassword();
        final String email = getEmail();
        String key = registerServiceImpl.register(user, pass,
                getPerson().getName(), email);
        log.info("get register key:" + key);
        String message = emailServiceImpl.sendActivationEmail(user, email, key);
        facesMessages.addGlobal(message);
        conversation.close();
        return "/account/login.xhtml";
    }

    public boolean isValid() {
        return valid;
    }
}
