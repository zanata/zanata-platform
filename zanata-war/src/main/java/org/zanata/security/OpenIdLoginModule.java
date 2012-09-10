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
package org.zanata.security;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;

public class OpenIdLoginModule implements LoginModule
{
   protected Set<String> roles = new HashSet<String>();

   protected Subject subject;
   protected Map<String, ?> options;
   protected CallbackHandler callbackHandler;

   protected String username;

   public boolean abort() throws LoginException
   {
      return true;
   }

   public boolean commit() throws LoginException
   {
      return true;
   }

   public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)
   {
      this.subject = subject;
      this.options = options;
      this.callbackHandler = callbackHandler;
   }

   public boolean login() throws LoginException
   {
      try
      {
         NameCallback cbName = new NameCallback("Enter username");
         PasswordCallback cbPassword = new PasswordCallback("Enter password", false);

         // Get the username and password from the callback handler
         callbackHandler.handle(new Callback[] { cbName, cbPassword });
         username = cbName.getName();
         ZanataOpenId openid = (ZanataOpenId) Component.getInstance(ZanataOpenId.class, ScopeType.SESSION);
         openid.login(username);
      }
      catch (Exception ex)
      {
         LoginException le = new LoginException(ex.getMessage());
         le.initCause(ex);
         throw le;
      }

      return false;
   }

   public boolean logout() throws LoginException
   {
      return true;
   }
}
