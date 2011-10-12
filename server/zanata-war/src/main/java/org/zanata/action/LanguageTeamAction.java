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
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;

@Name("languageTeamAction")
@Scope(ScopeType.PAGE)
public class LanguageTeamAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @In
   private LanguageTeamService languageTeamServiceImpl;
   
   @In
   private LocaleDAO localeDAO;
   
   @In
   private PersonDAO personDAO;
   
   @In
   private LocaleService localeServiceImpl;
   
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;
   
   @Logger
   Log log;
   
   @In
   private List<HLocale> memberLanguage;

   private String language;
   private HLocale locale;
   private boolean contained;
   private String searchTerm;
   private List<HPerson> searchResults;

   public String getLanguage()
   {
      return language;
   }

   public void setLanguage(String language)
   {
      this.language = language;
   }
   
   public String getSearchTerm()
   {
      return searchTerm;
   }

   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }

   public List<HPerson> getSearchResults()
   {
      return searchResults;
   }

   public void initLocale()
   {
      contained = false;
      for (HLocale locale : this.memberLanguage)
      {
         if (locale.getLocaleId().getId().equals(language))
         {
            contained = true;
            break;
         }
      }
      locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
      log.debug("init language: {0}", locale.getLocaleId().getId());
      log.debug("init contained: {0}", contained);
   }

   public boolean getContained()
   {
      return contained;
   }

   public HLocale getLocale()
   {
      return locale;
   }

   @Transactional
   public void joinTribe()
   {
      log.debug("starting join tribe");
      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      try
      {
         languageTeamServiceImpl.joinLanguageTeam(this.language, authenticatedAccount.getPerson().getId());
         Events.instance().raiseEvent("personJoinedTribe");
         log.info("{0} joined tribe {1}", authenticatedAccount.getUsername(), this.language);
         FacesMessages.instance().add("You are now a member of the {0} language team", this.locale.retrieveNativeName());
      }
      catch (Exception e)
      {
         FacesMessages.instance().add(Severity.ERROR, e.getMessage());
      }
   }

   @Transactional
   public void leaveTribe()
   {
      log.debug("starting leave tribe");
      if (authenticatedAccount == null)
      {
         log.error("failed to load auth person");
         return;
      }
      languageTeamServiceImpl.leaveLanguageTeam(this.language, authenticatedAccount.getPerson().getId());
      Events.instance().raiseEvent("personLeftTribe");
      log.info("{0} left tribe {1}", authenticatedAccount.getUsername(), this.language);
      FacesMessages.instance().add("You have left the {0} language team", this.locale.retrieveNativeName());
   }
   
   public void saveTeamCoordinator( HLocaleMember member )
   {
      this.localeDAO.makePersistent(this.locale);
      this.localeDAO.flush();
      if( member.isCoordinator() )
      {
         FacesMessages.instance().add("{0} has been made a Team Coordinator", member.getPerson().getAccount().getUsername());
      }
      else
      {
         FacesMessages.instance().add("{0} has been removed from Team Coordinators", member.getPerson().getAccount().getUsername());
      }
   }
   
   public void addTeamMember( final Long personId )
   {
      this.languageTeamServiceImpl.joinLanguageTeam(this.language, personId);
      // reload the locale for changes
      this.locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
   }
   
   public void removeMembership( HLocaleMember member )
   {
      this.languageTeamServiceImpl.leaveLanguageTeam(this.language, member.getPerson().getId());
      this.locale.getMembers().remove(member);
   }
   
   public boolean isPersonInTeam( final Long personId )
   {
      for( HLocaleMember lm : this.locale.getMembers() )
      {
         if( lm.getPerson().getId().equals( personId ) )
         {
            return true;
         }
      }
      return false;
   }
   
   public void searchForTeamMembers()
   {
      this.searchResults = this.personDAO.findAllContainingName( this.searchTerm );
   }
   
   public boolean userIsCoordinator()
   {
      return this.authenticatedAccount != null && this.authenticatedAccount.getPerson().isCoordinator(this.locale);
   }

}
