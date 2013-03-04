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

import static org.zanata.rest.dto.stats.TranslationStatistics.StatUnit.WORD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityNotFoundException;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.process.CopyTransProcessHandle;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.seam.scope.FlashScopeBean;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionGroupService;
import org.zanata.util.DateUtil;

@Name("viewAllStatusAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class ViewAllStatusAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final PeriodFormatterBuilder PERIOD_FORMATTER_BUILDER = new PeriodFormatterBuilder().appendDays().appendSuffix(" day", " days").appendSeparator(", ").appendHours().appendSuffix(" hour", " hours").appendSeparator(", ").appendMinutes().appendSuffix(" min", " mins");

   @Logger
   Log log;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   ZanataIdentity identity;

   @In
   ProjectIterationDAO projectIterationDAO;

   @In
   PersonDAO personDAO;

   @In
   LocaleService localeServiceImpl;

   @In
   CopyTransService copyTransServiceImpl;

   @In
   VersionGroupService versionGroupServiceImpl;

   @In
   StatisticsResource statisticsServiceImpl;

   @In
   CopyTransManager copyTransManager;

   @In
   FlashScopeBean flash;

   private String iterationSlug;

   private String projectSlug;

   private String searchTerm;

   private HProjectIteration projectIteration;

   private List<HIterationGroup> searchResults;

   private StatUnit statsOption = WORD;

   private Map<LocaleId, Status> statsMap = new HashMap<LocaleId, Status>();

   public static class Status implements Comparable<Status>
   {
      private String locale;
      private String nativeName;
      private TranslationStatistics stats;
      private boolean userInLanguageTeam;

      public Status(String locale, String nativeName, TranslationStatistics stats, boolean userInLanguageTeam)
      {
         this.locale = locale;
         this.nativeName = nativeName;
         this.stats = stats;
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

      public TranslationStatistics getStats()
      {
         return stats;
      }
      
      public void setStats(TranslationStatistics stats)
      {
         this.stats = stats;
      }
      
      public boolean isUserInLanguageTeam()
      {
         return userInLanguageTeam;
      }
      
      @Override
      public int compareTo(Status o)
      {
         int per = getStats().getTotal() == 0 ? 0 :
               (int) Math.ceil(100 * getStats().getTranslated() / getStats().getTotal());
         int comparePer = o.getStats().getTotal() == 0 ? 0 :
               (int) Math.ceil(100 * o.getStats().getTranslated() / o.getStats().getTotal());
         
         return Double.compare(comparePer, per);
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
   
   private String[] getLocaleIds(List<HLocale> locale)
   {
      String[] localeIds = new String[locale.size()];
      for (int i = 0, localeSize = locale.size(); i < localeSize; i++)
      {
         HLocale l = locale.get(i);
         localeIds[i] = l.getLocaleId().getId();
      }
      return localeIds;
   }

   /*
    * This method is needed as a non-cached version of ViewAllStatusAction#getAllStatus that performs less queries,
    * for the Statistic type drop down.
    * It modifies the statsMap variable, which in turn modifies a cached view of the same map.
    */
   public void refreshStatistic()
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      
      List<HLocale> locale = this.getDisplayLocales();
      String[] localeIds = getLocaleIds(locale);
      
      ContainerTranslationStatistics iterationStats = statisticsServiceImpl.getStatistics(this.projectSlug, this.iterationSlug, false, true, localeIds);

      Long total;
      if (statsOption == WORD)
      {
         total = projectIterationDAO.getTotalWordCountForIteration(iteration.getId());
      }
      else
      {
         total = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      }
      
      for (HLocale var : locale)
      {
         TranslationStatistics stats = iterationStats.getStats(var.getLocaleId().getId(), statsOption);
         if (stats == null)
         {
            stats = new TranslationStatistics();
            stats.setUntranslated(total);
            stats.setTotal(total);
         }

         if (statsMap.containsKey(var.getLocaleId()))
         {
            statsMap.get(var.getLocaleId()).setStats(stats);
         }
      }
   }

   @CachedMethodResult
   public List<Status> getAllStatus()
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);

      List<HLocale> locale = this.getDisplayLocales();
      String[] localeIds = getLocaleIds(locale);

      ContainerTranslationStatistics iterationStats = statisticsServiceImpl.getStatistics(this.projectSlug, this.iterationSlug, false, true, localeIds);

      Long total;
      if (statsOption == WORD)
      {
         total = projectIterationDAO.getTotalWordCountForIteration(iteration.getId());
      }
      else
      {
         total = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      }

      for (HLocale var : locale)
      {
         TranslationStatistics stats = iterationStats.getStats(var.getLocaleId().getId(), statsOption);
         if (stats == null)
         {
            stats = new TranslationStatistics();
            stats.setUntranslated(total);
            stats.setTotal(total);

            HTextFlowTarget lastTranslatedTarget = localeServiceImpl.getLastTranslated(projectSlug, iterationSlug, var.getLocaleId());

            StringBuilder lastTranslated = new StringBuilder();
            if (lastTranslatedTarget != null)
            {
               lastTranslated.append(DateUtil.formatShortDate(lastTranslatedTarget.getLastChanged()));
               if (lastTranslatedTarget.getLastModifiedBy() != null)
               {
                  lastTranslated.append(" by ");
                  lastTranslated.append(lastTranslatedTarget.getLastModifiedBy().getAccount().getUsername());
                  
               }
            }
            stats.setLastTranslated(lastTranslated.toString());
         }


         if (!statsMap.containsKey(var.getLocaleId()))
         {
            boolean isMember = authenticatedAccount != null ? personDAO.isMemberOfLanguageTeam(authenticatedAccount.getPerson(), var) : false;

            Status op = new Status(var.getLocaleId().getId(), var.retrieveNativeName(), stats, isMember);
            statsMap.put(var.getLocaleId(), op);
         }
         else
         {
            statsMap.get(var.getLocaleId()).setStats(stats);
         }
      }

      List<Status> result = new ArrayList<Status>(statsMap.values());
      Collections.sort(result);
      return result;
   }

   public HProjectIteration getProjectIteration()
   {
      if (this.projectIteration == null)
      {
         this.projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      }
      return this.projectIteration;
   }

   public String getProjectType()
   {
      ProjectType result = getProjectIteration().getProjectType();
      if (result != null)
      {
         return result.name();
      }
      return null;
   }

   public HProject getProject()
   {
      return this.getProjectIteration().getProject();
   }

   public boolean isCopyTransRunning()
   {
      return copyTransManager.isCopyTransRunning(getProjectIteration());
   }

   @Restrict("#{s:hasPermission(viewAllStatusAction.projectIteration, 'copy-trans')}")
   public void cancelCopyTrans()
   {
      if (isCopyTransRunning())
      {
         copyTransManager.cancelCopyTrans(getProjectIteration());
      }
   }

   public int getCopyTransProgress()
   {
      CopyTransProcessHandle handle = copyTransManager.getCopyTransProcessHandle(getProjectIteration());
      if (handle != null)
      {
         return handle.getCurrentProgress();
      }
      else
      {
         return Integer.MAX_VALUE; // Return the maximum so that the progress
                                   // bar stops polling
      }
   }

   public int getCopyTransMaxProgress()
   {
      CopyTransProcessHandle handle = copyTransManager.getCopyTransProcessHandle(getProjectIteration());
      if (handle != null)
      {
         return handle.getMaxProgress();
      }
      else
      {
         return 1;
      }
   }

   public String getCopyTransStartTime()
   {
      CopyTransProcessHandle handle = copyTransManager.getCopyTransProcessHandle(getProjectIteration());
      long durationSinceStart = 0;
      if (handle.isStarted())
      {
         durationSinceStart = (System.currentTimeMillis() - handle.getStartTime());
      }

      return formatTimePeriod(durationSinceStart);
   }

   public String getCopyTransEstimatedTimeLeft()
   {
      CopyTransProcessHandle handle = copyTransManager.getCopyTransProcessHandle(getProjectIteration());
      return formatTimePeriod(handle.getEstimatedTimeRemaining());
   }

   public String getCopyTransStatusMessage()
   {
      if (!isCopyTransRunning())
      {
         CopyTransProcessHandle recentProcessHandle = copyTransManager.getMostRecentlyFinished(getProjectIteration());
         StringBuilder message = new StringBuilder("Last Translation copy ");

         if (recentProcessHandle == null)
         {
            return null;
         }

         // cancelled
         if (recentProcessHandle.getCancelledBy() != null)
         {
            message.append("cancelled by ");

            // ... by the same user
            if (recentProcessHandle.getCancelledBy().equals(identity.getCredentials().getUsername()))
            {
               message.append("you ");
            }
            // .. by another user
            else
            {
               message.append(recentProcessHandle.getCancelledBy()).append(" ");
            }

            // when was it done
            message.append(formatTimePeriod(System.currentTimeMillis() - recentProcessHandle.getCancelledTime())).append(" ago.");
         }
         // completed
         else
         {
            message.append("completed by ");

            // ... by the same user
            if (recentProcessHandle.getTriggeredBy().equals(identity.getCredentials().getUsername()))
            {
               message.append("you ");
            }
            // .. by another user
            else
            {
               message.append(recentProcessHandle.getTriggeredBy()).append(" ");
            }

            // when was it done
            message.append(formatTimePeriod(System.currentTimeMillis() - recentProcessHandle.getFinishTime())).append(" ago.");
         }

         return message.toString();
      }
      return null;
   }

   public CopyTransProcessHandle getCopyTransProcessHandle()
   {
      return copyTransManager.getCopyTransProcessHandle(getProjectIteration());
   }

   private String formatTimePeriod(long durationInMillis)
   {
      PeriodFormatter formatter = PERIOD_FORMATTER_BUILDER.toFormatter();
      CopyTransProcessHandle handle = copyTransManager.getCopyTransProcessHandle(getProjectIteration());
      Period period = new Period(durationInMillis);

      if (period.toStandardMinutes().getMinutes() <= 0)
      {
         return "less than a minute"; // TODO Localize
      }
      else
      {
         return formatter.print(period.normalizedStandard());
      }
   }

   private List<HLocale> getDisplayLocales()
   {
      return localeServiceImpl.getSupportedLangugeByProjectIteration(this.projectSlug, this.iterationSlug);
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
      searchResults = versionGroupServiceImpl.searchLikeSlugAndName(searchTerm);
   }

   public boolean isGroupInVersion(String groupSlug)
   {
      return versionGroupServiceImpl.isGroupInVersion(groupSlug, getProjectIteration().getId());
   }

   public StatUnit getStatsOption()
   {
      return statsOption;
   }

   public void setStatsOption(StatUnit statsOption)
   {
      this.statsOption = statsOption;
   }
}
