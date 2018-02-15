/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.seam.security;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@SessionScoped
public class CurrentUserImpl implements CurrentUser {
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    @Authenticated
    private Instance<HAccount> accountInstance;
    private HAccount account;

    @PostConstruct
    private void resolve() {
        // Note: the Dependent-scoped HAccount will be destroyed when this
        // bean is destroyed (SessionScoped, thus when session is destroyed).
        // Ref: https://rmannibucau.wordpress.com/2015/03/02/cdi-and-instance-3-pitfalls-you-need-to-know/
        account = accountInstance.get();
    }

    /**
     * Note that the HAccount will be null if the user is not authenticated,
     * and will generally be disconnected from the Hibernate session.
     */
    @Nullable
    @Override
    public HAccount getAccount() {
        return account;
    }

}
