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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.action.validator.NotDuplicateEmail;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HPerson;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.ui.faces.FacesMessages;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Name("register")
@Scope(ScopeType.CONVERSATION)
@Slf4j
public class RegisterAction implements Serializable {

    private static final long serialVersionUID = -7883627570614588182L;

    @In
    private EntityManager entityManager;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    RegisterService registerServiceImpl;

    @In
    PersonDAO personDAO;

    @In
    EmailService emailServiceImpl;

    private String username;
    private String email;
    private String password;
    private String humanField;

    private boolean valid;

    private HPerson person;

    @Begin(join = true)
    public HPerson getPerson() {
        if (person == null)
            person = new HPerson();
        return person;
    }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username;
    }

    @NotEmpty
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z\\d_]{3,20}$",
            message = "{validation.username.constraints}")
    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotEmpty
    @Email
    @NotDuplicateEmail(message = "This email address is already taken.")
    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotEmpty
    @Size(min = 6, max = 20)
    // @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$",
    // message="Password is not secure enough!")
            public
            String getPassword() {
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

    @End
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
        String key =
                registerServiceImpl.register(user, pass, getPerson().getName(),
                        email);
        log.info("get register key:" + key);

        String message =
                emailServiceImpl.sendActivationEmail(user, email, key);
        facesMessages.addGlobal(message);

        return "/home.xhtml";
    }

    public boolean isValid() {
        return valid;
    }

}
