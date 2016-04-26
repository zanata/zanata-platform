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

import java.io.IOException;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.zanata.ApplicationConfiguration;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.service.UserService;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
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
@Named("profileHome")
@javax.faces.bean.ViewScoped
@Slf4j
public class ProfileHome implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;

    @Getter
    private User user;

    @Inject
    private ZanataIdentity identity;
    @Inject @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private Messages msgs;
    @Inject
    private UserService userService;
    @Inject
    private FacesMessages jsfMessages;
    @Inject
    private ApplicationConfiguration applicationConfiguration;

    private void init() {
        HAccount account = accountDAO. getByUsername(username);
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
        user = userService.transferToUser(account, displayEmail());
    }

    private boolean displayEmail() {
        return (applicationConfiguration.isDisplayUserEmail()
                && identity.isLoggedIn()) || identity.hasRole("admin");
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
        if(authenticatedAccount == null) {
            return new User();
        }
        //This is to get self information, email should be visible
        return userService.transferToUser(authenticatedAccount, true);
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

    public Permission getUserPermission() {
        Permission permission = new Permission();
        boolean authenticated = authenticatedAccount != null;
        permission.put("authenticated", authenticated);
        return permission;
    }

    public String convertToJSON(User user) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(user);
        } catch (IOException e) {
            return this.getClass().getName() + "@"
                + Integer.toHexString(this.hashCode());
        }
    }
}
