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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.rest.dto.DTOUtil;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.service.UserService;
import org.zanata.model.HAccount;
import org.zanata.seam.framework.EntityHome;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import java.io.Serializable;

/**
 * A simple bean to hold the currently authenticated account.
 */
@Named("authenticatedAccountHome")
@RequestScoped
@Model
@Transactional
public class AuthenticatedAccountHome extends EntityHome<HAccount>
        implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AuthenticatedAccountHome.class);

    /**
     */
    private static final long serialVersionUID = 1L;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private ZanataIdentity identity;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private UserService userService;

    @Override
    public Object getId() {
        if (authenticatedAccount == null) {
            return null;
        }
        return authenticatedAccount.getId();
    }

    /**
     * Produce json string of {@link org.zanata.rest.dto.User} for js module
     * (frontend). This allows js module to have basic information for any API
     * request. TODO: make caller to use UserService directly
     */
    public String getUser() {
        User user = userService.getUserInfo(authenticatedAccount, true);
        return DTOUtil.toJSON(user);
    }

    public Permission getUserPermission() {
        return userService.getUserPermission();
    }

    public String getUsername() {
        if (authenticatedAccount != null) {
            return authenticatedAccount.getUsername();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return identity.isLoggedIn() && authenticatedAccount != null
                && authenticatedAccount.isEnabled();
    }
}
