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
package org.zanata.service;

import org.zanata.model.HAccount;
import org.zanata.model.security.HCredentials;
import org.zanata.security.AuthenticationType;

public interface RegisterService
{
   String register(final String username, final String password, String name, String email);

   /**
    * Use this for external authentication.
    */
   String register(final String username, final String externalId, AuthenticationType authType, String name, String email);

   /**
    * Merge two accounts together. One of the accounts will be rendered inactive while the other will inherit all
    * security permissions.
    *
    * @param active This account will retain all roles, permissions and credentials it already had, plus all the
    *               ones from the obsolete account.
    * @param obsolete This account will be disabled, and stripped of all credentials and permissions.
    */
   void mergeAccounts( HAccount active, HAccount obsolete );
}
