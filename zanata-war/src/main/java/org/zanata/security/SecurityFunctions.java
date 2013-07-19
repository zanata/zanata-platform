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

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;

/**
 * Contains static helper functions used inside the rules files.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class SecurityFunctions
{
   protected SecurityFunctions()
   {
   }

   public static boolean isUserAllowedAccess( HProject project )
   {
      if( project.isRestrictedByRoles() )
      {
         ZanataIdentity identity = getIdentity();

         if( identity != null )
         {
            for(HAccountRole role : project.getAllowedRoles())
            {
               if( identity.hasRole( role.getName() ) )
               {
                  return true;
               }
            }
         }

         // no access
         return false;
      }
      else
      {
         return true;
      }
   }

   public static boolean isUserTranslatorOfLanguage( HLocale lang )
   {
      HAccount authenticatedAccount = getAuthenticatedAccount();
      PersonDAO personDAO = (PersonDAO)Component.getInstance(PersonDAO.class);

      if( authenticatedAccount != null )
      {
         return personDAO.isUserInLanguageTeamWithRoles(authenticatedAccount.getPerson(), lang, true, null, null);
      }

      return false; // No authenticated user
   }
   
   public static boolean isUserReviewerOfLanguage( HLocale lang )
   {
      HAccount authenticatedAccount = getAuthenticatedAccount();
      PersonDAO personDAO = (PersonDAO)Component.getInstance(PersonDAO.class);

      if( authenticatedAccount != null )
      {
         return personDAO.isUserInLanguageTeamWithRoles( authenticatedAccount.getPerson(), lang, null, true, null );
      }

      return false; // No authenticated user
   }

   public static boolean isUserCoordinatorOfLanguage( HLocale lang )
   {
      HAccount authenticatedAccount = getAuthenticatedAccount();
      PersonDAO personDAO = (PersonDAO)Component.getInstance(PersonDAO.class);

      if( authenticatedAccount != null )
      {
         return personDAO.isUserInLanguageTeamWithRoles( authenticatedAccount.getPerson(), lang, null, null, true );
      }

      return false; // No authenticated user
   }

   private static final ZanataIdentity getIdentity()
   {
      return (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
   }

   private static final HAccount getAuthenticatedAccount()
   {
      return (HAccount) Component.getInstance(JpaIdentityStore.AUTHENTICATED_USER, ScopeType.SESSION);
   }
}
