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

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.seam.framework.EntityHome;

/**
 * A simple bean to hold the currently authenticated account.
 */
@Name("authenticatedAccountHome")
@Scope(ScopeType.CONVERSATION)
@Slf4j
public class AuthenticatedAccountHome extends EntityHome<HAccount> {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;

    @Override
    public Object getId() {
        if (authenticatedAccount == null) {
            return null;
        }
        return authenticatedAccount.getId();
    }

    public HPerson getAuthenticatedPerson() {
        return getInstance().getPerson();
    }
}
