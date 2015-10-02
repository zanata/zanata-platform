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
import java.util.Set;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.ui.faces.FacesMessages;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

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
    private String name;

    @Getter
    private String userImageUrl;

    @Getter
    private String userLanguageTeams;

    @Inject
    ZanataIdentity identity;
    @Inject /* TODO [CDI] check this: migrated from @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER) */
    HAccount authenticatedAccount;
    @Inject
    PersonDAO personDAO;
    @Inject
    AccountDAO accountDAO;
    @Inject
    private GravatarService gravatarServiceImpl;
    @Inject
    Messages msgs;

    @Inject
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
        HPerson person =
                personDAO.findById(account.getPerson().getId());
        Set<HLocale> languageMemberships = person.getLanguageMemberships();
        name = person.getName();
        userImageUrl = gravatarServiceImpl
                .getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
                        person.getEmail());
        userLanguageTeams = Joiner.on(", ").skipNulls().join(
                Collections2.transform(languageMemberships,
                        new Function<HLocale, Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull HLocale locale) {
                                return locale.retrieveDisplayName();
                            }
                        }));
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
