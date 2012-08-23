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

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.zanata.security.openid.OpenIdProviderManager;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("loginAction")
public class LoginAction
{
   @In
   private Identity identity;

   @In
   private OpenIdProviderManager openIdProviderManager;

   @In
   private Credentials credentials;

   public String logInWithGoogle()
   {
      // NB: Credentials' user name must be set to something or else login will fail. The real user name will be asked
      // by the provider
      credentials.setUsername("google");
      openIdProviderManager.useGoogleProvider();
      String loginResult = identity.login();

      // Clear out the credentials again
      credentials.setUsername("");

      return loginResult;
   }
}
