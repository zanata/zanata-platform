/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;

import javax.faces.application.FacesMessage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.rest.editor.dto.User;
import org.zanata.rest.editor.service.UserService;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.security.ZanataIdentity;
import org.zanata.ui.faces.FacesMessages;

import com.google.common.base.Strings;

import static org.apache.commons.lang.StringUtils.abbreviate;

/**
 * User profile page backing bean.
 *
 * @see ProfileAction for edit user profile form page
 * @see NewProfileAction for new user profile form page
 *
 */
@Name("profileHome")
@Scope(ScopeType.PAGE)
@Slf4j
public class ProfileHome implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;

    @Getter
    private User user;

    @In
    ZanataIdentity identity;
    @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;
    @In
    PersonDAO personDAO;
    @In
    AccountDAO accountDAO;
    @In
    Messages msgs;
    @In(value = "editor.userService", create = true)
    private UserService userService;
    @In
    private FacesMessages jsfMessages;

    private void init() {
        HAccount account;
        account = accountDAO.getByUsername(username);
        if (account == null) {
            jsfMessages.clear();
            jsfMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    msgs.format("jsf.UsernameNotAvailable", abbreviate(username,
                            24)));
            account = useAuthenticatedAccount();
            if (account == null) {
                // user not logged in and username not found
                return;
            }
        }
        user = userService.transferToUser(account);
    }

    private HAccount useAuthenticatedAccount() {
        if (identity.isLoggedIn()) {
            HAccount account;
            username = authenticatedAccount.getUsername();
            account = authenticatedAccount;
            return account;
        }
        return null;
    }

    public User getAuthenticatedUser() {
        User authenticatedUser = new User();
        if(authenticatedAccount == null) {
            return authenticatedUser;
        }
        return userService.transferToUser(authenticatedAccount);
    }

    public String getUsername() {
        if (Strings.isNullOrEmpty(username) && identity.isLoggedIn()) {
            username = authenticatedAccount.getUsername();
            init();
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        init();
    }
}
