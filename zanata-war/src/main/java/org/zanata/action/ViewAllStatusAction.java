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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityNotFoundException;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.TransUnitWords;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.*;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionGroupService;

@Name("viewAllStatusAction")
@Scope(ScopeType.PAGE)
public class ViewAllStatusAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   ZanataIdentity identity;

   @In
   ProjectIterationDAO projectIterationDAO;

   @In
   LocaleService localeServiceImpl;

   @In
   CopyTransService copyTransServiceImpl;

   @In
   Map<String, String> messages;

   @In
   private VersionGroupService versionGroupServiceImpl;

   private String iterationSlug;

   private String projectSlug;

   private String searchTerm;

   private boolean showAllLocales = false;

   private HProjectIteration projectIteration;

   private List<HIterationGroup> searchResults;

   public static class Status implements Comparable<Status>
   {
      private String locale;
      private String nativeName;
      private TransUnitWords words;
      private int per;
      private boolean userInLanguageTeam;

      public Status(String locale, String nativeName, TransUnitWords words, int per, boolean userInLanguageTeam)
      {
         this.locale = locale;
         this.nativeName = nativeName;
         this.words = words;
         this.per = per;
         this.userInLanguageTeam = userInLanguageTeam;
      }

      public String getLocale()
      {
         return locale;
      }

      public String getNativeName()
      {
         return nativeName;
      }

      public TransUnitWords getWords()
      {
         return words;
      }

      public double getPer()
      {
         return per;
      }

      public boolean isUserInLanguageTeam()
      {
         return userInLanguageTeam;
      }

      @Override
      public int compareTo(Status o)
      {
         return ((Double) o.getPer()).compareTo((Double) this.getPer());
      }

   }

   public void setProjectSlug(String slug)
   {
      this.projectSlug = slug;
   }

   public String getProjectSlug()
   {
      return this.projectSlug;
   }

   public void setIterationSlug(String slug)
   {
      this.iterationSlug = slug;
   }

   public String getIterationSlug()
   {
      return this.iterationSlug;
   }

   public void validateIteration()
   {
      if (this.getProjectIteration() == null)
      {
         throw new EntityNotFoundException(this.iterationSlug, HProjectIteration.class);
      }
   }

   public List<Status> getAllStatus()
   {
      List<Status> result = new ArrayList<Status>();
      HProjectIteration iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      Map<String, TransUnitWords> stats = projectIterationDAO.getAllWordStatsStatistics(iteration.getId());
      List<HLocale> locale = this.getDisplayLocales();
      Long total = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      for (HLocale var : locale)
      {
         TransUnitWords words = stats.get(var.getLocaleId().getId());
         if (words == null)
         {
            words = new TransUnitWords();
            words.set(ContentState.New, total.intValue());

         }
         int per;
         if (total.intValue() == 0)
         {
            per = 0;
         }
         else
         {
            per = (int) Math.ceil(100 * words.getApproved() / words.getTotal());

         }
         boolean isMember = authenticatedAccount != null ? authenticatedAccount.getPerson().isMember(var) : false;

         Status op = new Status(var.getLocaleId().getId(), var.retrieveNativeName(), words, per, isMember);
         result.add(op);
      }
      Collections.sort(result);
      return result;
   }

   public void performCopyTrans()
   {
      copyTransServiceImpl.copyTransForIteration(getProjectIteration());
      FacesMessages.instance().add(messages.get("jsf.iteration.CopyTrans.success"));
   }

   public boolean getShowAllLocales()
   {
      return showAllLocales;
   }

   public void setShowAllLocales(boolean showAllLocales)
   {
      this.showAllLocales = showAllLocales;
   }

   public HProjectIteration getProjectIteration()
   {
      if (this.projectIteration == null)
      {
         this.projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      }
      return this.projectIteration;
   }

   public HProject getProject()
   {
      return this.getProjectIteration().getProject();
   }

   public boolean isIterationReadOnly()
   {
      return this.getProjectIteration().getProject().getStatus() == EntityStatus.READONLY || this.getProjectIteration().getStatus() == EntityStatus.READONLY;
   }

   public boolean isIterationObsolete()
   {
      return this.getProjectIteration().getProject().getStatus() == EntityStatus.OBSOLETE || this.getProjectIteration().getStatus() == EntityStatus.OBSOLETE;
   }

   public boolean isUserAllowedToTranslate(String localeId)
   {
      return !isIterationReadOnly() && !isIterationObsolete() && identity.hasPermission("add-translation", getProject(), localeServiceImpl.getByLocaleId(localeId));
   }

   private List<HLocale> getDisplayLocales()
   {
      if (this.showAllLocales || authenticatedAccount == null)
      {
         return localeServiceImpl.getSupportedLangugeByProjectIteration(this.projectSlug, this.iterationSlug);
      }
      else
      {
         return localeServiceImpl.getTranslation(projectSlug, iterationSlug, authenticatedAccount.getUsername());
      }
   }

   public List<HIterationGroup> getSearchResults()
   {
      if (searchResults == null)
      {
         searchResults = new ArrayList<HIterationGroup>();
      }
      return searchResults;
   }

   public String getSearchTerm()
   {
      return searchTerm;
   }

   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }

   public void searchGroup()
   {
      searchResults = versionGroupServiceImpl.searchLikeSlug(searchTerm);
   }

   public boolean isVersionInGroup()
   {
      return versionGroupServiceImpl.isVersionInGroup(null, getProjectIteration().getId());
   }
}
