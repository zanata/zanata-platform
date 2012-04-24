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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.service.LanguageTeamService;

@Name("memberLanguage")
@Scope(ScopeType.SESSION)
public class MemberLanguageList implements Serializable
{

   private static final long serialVersionUID = 1L;

   @In
   private LanguageTeamService languageTeamServiceImpl;

   @Logger
   Log log;

   private List<HLocale> memberTribes;

   @Create
   public void onCreate()
   {
      fetchMemberTribes();
   }

   @Unwrap
   public List<HLocale> getMemberTribes()
   {
      return memberTribes;
   }

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Observer(create = false, value = { "personJoinedTribe", "personLeftTribe", "disableLanguage", "enableLanguage", JpaIdentityStore.EVENT_USER_AUTHENTICATED })
   synchronized public void fetchMemberTribes()
   {
      log.debug("refreshing languages...");
      if (authenticatedAccount == null)
      {
         memberTribes = Collections.emptyList();
         return;
      }

      memberTribes = languageTeamServiceImpl.getLanguageMemberships(authenticatedAccount.getUsername());
      log.info("now listing {0} languages", memberTribes.size());
   }


}
