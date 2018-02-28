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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
public class CurrentUserImpl implements CurrentUser {
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    @Authenticated
    // Note: the Dependent-scoped HAccount bean is bound to the lifecycle of
    // this (RequestScoped) bean.
    // Ref: https://rmannibucau.wordpress.com/2015/03/02/cdi-and-instance-3-pitfalls-you-need-to-know/
    private HAccount account;

    /**
     * Note that the HAccount will be null if the user is not authenticated.
     */
    @Nullable
    @Override
    public HAccount getAccount() {
        return account;
    }

}
