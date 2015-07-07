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

import java.util.List;
import javax.faces.model.DataModel;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.EmailService;
import org.zanata.service.UserAccountService;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;

import lombok.Getter;
import lombok.Setter;
import org.zanata.ui.faces.FacesMessages;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * Extension of Seam management's UserAction class' behaviour.
 *
 * @see {@link org.jboss.seam.security.management.action.UserAction}
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("org.jboss.seam.security.management.userAction")
@Scope(CONVERSATION)
@Install(precedence = APPLICATION)
public class UserAction extends
        org.jboss.seam.security.management.action.UserAction {
    private static final long serialVersionUID = 1L;

    @In
    private IdentityManager identityManager;

    @In
    private EntityManager entityManager;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private Messages msgs;

    @In
    private UserAccountService userAccountServiceImpl;

    @In
    private EmailService emailServiceImpl;

    @In
    private PersonDAO personDAO;

    private boolean newUserFlag;

    private String originalUsername;

    @Getter
    private AbstractListFilter<String> userFilter =
            new AbstractListFilter<String>() {
                AccountDAO accountDAO =
                        (AccountDAO) Component.getInstance(AccountDAO.class);

                @Override
                protected List<String> fetchRecords(int start, int max,
                        String filter) {
                    return accountDAO.getUserNames(filter, start, max);
                }

                @Override
                protected long fetchTotalRecords(String filter) {
                    return accountDAO.getUserCount(filter);
                }
            };

    public void deleteUser(String userName) {
        try {
            identityManager.deleteUser(userName);
            // NB: Need to call flush here to be able to catch the persistence
            // exception, otherwise it would be caught by Seam.
            entityManager.flush();
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                facesMessages
                        .addFromResourceBundle(SEVERITY_ERROR,
                                "jsf.UserManager.delete.constraintViolation.error");
            }
        }
    }

    public String getEmail(String username) {
        return personDAO.findEmail(username);
    }

    public String getName(String username) {
        return personDAO.findByUsername(username).getName();
    }

    @Override
    @Begin
    public void createUser() {
        super.createUser();
        newUserFlag = true;
    }

    @Override
    @Begin
    public void editUser(String username) {
        super.editUser(username);
        newUserFlag = false;
        originalUsername = username;
    }

    @Override
    public String save() {
        boolean usernameChanged = false;
        String newUsername = getUsername();

        // Allow user name changes when editing
        if (!newUserFlag && !originalUsername.equals(newUsername)) {
            if (isNewUsernameValid(newUsername)) {
                userAccountServiceImpl.editUsername(originalUsername,
                        newUsername);
                usernameChanged = true;
            } else {
                facesMessages.addToControl("username",
                        msgs.format("jsf.UsernameNotAvailable",
                                getUsername()));
                setUsername(originalUsername); // reset the username field
                return "failure";
            }
        }

        String saveResult = super.save();

        if (usernameChanged) {
            String email = getEmail(newUsername);
            String message = emailServiceImpl.sendUsernameChangedEmail(
                    email, newUsername);
            facesMessages.addGlobal(message);
        }
        return saveResult;
    }

    /**
     * Validate that a user name is not already in the system, by another
     * account
     */
    private boolean isNewUsernameValid(String username) {
        try {
            entityManager
                    .createQuery("from HAccount a where a.username = :username")
                    .setParameter("username", username).getSingleResult();
            return false;
        } catch (NoResultException e) {
            // pass
            return true;
        }
    }
}
