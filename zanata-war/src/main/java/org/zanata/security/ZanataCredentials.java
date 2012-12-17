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
package org.zanata.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Credentials;
import org.zanata.security.openid.OpenIdProviderType;

/**
 * Overrides the default Seam credentials.
 * Adds app-specific security concepts like authentication mechanisms.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link Credentials}
 */
@Name("org.jboss.seam.security.credentials")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
public class ZanataCredentials extends Credentials
{
   private AuthenticationType authType;

   private OpenIdProviderType openIdProviderType;


   public AuthenticationType getAuthType()
   {
      return authType;
   }

   public void setAuthType(AuthenticationType authType)
   {
      this.authType = authType;
   }

   public OpenIdProviderType getOpenIdProviderType()
   {
      return openIdProviderType;
   }

   public void setOpenIdProviderType(OpenIdProviderType openIdProviderType)
   {
      this.openIdProviderType = openIdProviderType;
   }

   @Override
   public boolean isInvalid()
   {
      return false;
   }

   @Override
   public void clear()
   {
      super.clear();
      authType = null;
      openIdProviderType = null;
   }
}
