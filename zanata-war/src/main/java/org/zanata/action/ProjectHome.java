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

import static org.zanata.rest.dto.stats.TranslationStatistics.StatUnit.WORD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.EntityStatus;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;

@Name("projectHome")
@CachedMethods
@Scope(ScopeType.PAGE)
public class ProjectHome extends SlugHome<HIterationProject>
{
   private static final long serialVersionUID = 1L;

   public static final String PROJECT_UPDATE = "project.update";

   private String slug;

   @In
   private StatisticsResource statisticsServiceImpl;
   
   @In
   ZanataIdentity identity;

   @Logger
   Log log;

   @In
   private PersonDAO personDAO;
   
   @In
   private LocaleDAO localeDAO;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   /* Outjected from LocaleListAction */
   @In(required = false)
   Map<String, String> customizedItems;

   /* Outjected from LocaleListAction */
   @In(required = false)
   private Boolean overrideLocales;

   /* Outjected from ProjectRoleRestrictionAction */
   @In(required = false)
   private Set<HAccountRole> customizedProjectRoleRestrictions;

   /* Outjected from ProjectRoleRestrictionAction */
   @In(required = false)
   private Boolean restrictByRoles;

   @In
   private LocaleService localeServiceImpl;

   @In
   private SlugEntityService slugEntityServiceImpl;

   @In(create = true)
   private ProjectDAO projectDAO;
   
   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private EntityManager entityManager;

   private Set<String> renderedPanel = new HashSet<String>();
   private Set<Integer> rowsToUpdate = new HashSet<Integer>();

   private StatUnit statsOption = WORD;

   @Override
   protected HIterationProject loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (HIterationProject) session.createCriteria(getEntityClass()).add(Restrictions.naturalId().set("slug", getSlug())).setCacheable(true).uniqueResult();
   }

   public void validateSuppliedId()
   {
      HIterationProject ip = getInstance(); // this will raise an EntityNotFound
                                            // exception
      // when id is invalid and conversation will not
      // start

      if (ip.getStatus().equals(EntityStatus.OBSOLETE) && !checkViewObsolete())
      {
         throw new EntityNotFoundException();
      }
   }

   public void verifySlugAvailable(ValueChangeEvent e)
   {
      String slug = (String) e.getNewValue();
      validateSlug(slug, e.getComponent().getId());
   }

   public boolean validateSlug(String slug, String componentId)
   {
      if (!isSlugAvailable(slug))
      {
         FacesMessages.instance().addToControl(componentId, "This Project ID is not available");
         return false;
      }
      return true;
   }

   public boolean isSlugAvailable(String slug)
   {
      return slugEntityServiceImpl.isSlugAvailable(slug, HIterationProject.class);
   }

   @Override
   @Transactional
   public String persist()
   {
      String retValue = "";
      if (!validateSlug(getInstance().getSlug(), "slug"))
         return null;

      if (authenticatedAccount != null)
      {
         updateOverrideLocales();
         updateRoleRestrictions();
         getInstance().addMaintainer(authenticatedAccount.getPerson());
         retValue = super.persist();
         Events.instance().raiseEvent("projectAdded");
      }
      return retValue;
   }

   @CachedMethodResult
   public List<HProjectIteration> getIterations()
   {
      List<HProjectIteration> results = new ArrayList<HProjectIteration>();

      for (HProjectIteration iteration : getInstance().getProjectIterations())
      {
         if (iteration.getStatus() == EntityStatus.OBSOLETE && checkViewObsolete())
         {
            results.add(iteration);
         }
         else if (iteration.getStatus() != EntityStatus.OBSOLETE)
         {
            results.add(iteration);
         }
      }
      Collections.sort(results, new Comparator<HProjectIteration>()
      {
         @Override
         public int compare(HProjectIteration o1, HProjectIteration o2)
         {
            EntityStatus fromStatus = o1.getStatus();
            EntityStatus toStatus = o2.getStatus();
            
            if (fromStatus.equals(toStatus))
            {
               return 0;
            }

            if (fromStatus.equals(EntityStatus.ACTIVE))
            {
               return -1;
            }

            if (fromStatus.equals(EntityStatus.READONLY))
            {
               if (toStatus.equals(EntityStatus.ACTIVE))
               {
                  return 1;
               }
               return -1;
            }

            if (fromStatus.equals(EntityStatus.OBSOLETE))
            {
               return 1;
            }

            return 0;
         }
      });
      return results;
   }

   public EntityStatus getEffectiveIterationStatus(HProjectIteration iteration)
   {
      /**
       * Null pointer exception checking caused by unknown issues where
       * getEffectiveIterationStatus gets invoke before getIterations
       */
      if (iteration == null)
      {
         return null;
      }
      if (getInstance().getStatus() == EntityStatus.READONLY)
      {
         if (iteration.getStatus() == EntityStatus.ACTIVE)
         {
            return EntityStatus.READONLY;
         }
      }
      else if (getInstance().getStatus() == EntityStatus.OBSOLETE)
      {
         if (iteration.getStatus() == EntityStatus.ACTIVE || iteration.getStatus() == EntityStatus.READONLY)
         {
            return EntityStatus.OBSOLETE;
         }
      }
      return iteration.getStatus();
   }

   public String cancel()
   {
      return "cancel";
   }

   public String getSlug()
   {
      return slug;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   @Override
   public boolean isIdDefined()
   {
      return slug != null;
   }

   @Override
   public NaturalIdentifier getNaturalId()
   {
      return Restrictions.naturalId().set("slug", slug);
   }

   @Override
   public Object getId()
   {
      return slug;
   }

   @Override
   public String update()
   {
      updateOverrideLocales();
      updateRoleRestrictions();
      String state = super.update();
      Events.instance().raiseEvent(PROJECT_UPDATE, getInstance());

      if (getInstance().getStatus() == EntityStatus.READONLY)
      {
         for (HProjectIteration version : getInstance().getProjectIterations())
         {
            if (version.getStatus() == EntityStatus.ACTIVE)
            {
               version.setStatus(EntityStatus.READONLY);
               entityManager.merge(version);
               Events.instance().raiseEvent(ProjectIterationHome.PROJECT_ITERATION_UPDATE, version);
            }
         }
      }
      else if (getInstance().getStatus() == EntityStatus.OBSOLETE)
      {
         for (HProjectIteration version : getInstance().getProjectIterations())
         {
            if (version.getStatus() != EntityStatus.OBSOLETE)
            {
               version.setStatus(EntityStatus.OBSOLETE);
               entityManager.merge(version);
               Events.instance().raiseEvent(ProjectIterationHome.PROJECT_ITERATION_UPDATE, version);
            }
         }
      }

      return state;
   }

   private void updateOverrideLocales()
   {
      if (overrideLocales != null)
      {
         getInstance().setOverrideLocales(overrideLocales);
         if (!overrideLocales)
         {
            getInstance().getCustomizedLocales().clear();
         }
         else if (customizedItems != null)
         {
            Set<HLocale> locale = localeServiceImpl.convertCustomizedLocale(customizedItems);
            getInstance().getCustomizedLocales().clear();
            getInstance().getCustomizedLocales().addAll(locale);
         }
      }
   }

   private void updateRoleRestrictions()
   {
      if (restrictByRoles != null)
      {
         getInstance().setRestrictedByRoles(restrictByRoles);
         getInstance().getAllowedRoles().clear();

         if (restrictByRoles)
         {
            getInstance().getAllowedRoles().addAll(customizedProjectRoleRestrictions);
         }
      }
   }

   public boolean isProjectActive()
   {
      return getInstance().getStatus() == EntityStatus.ACTIVE;
   }

   public boolean isReadOnly(HProjectIteration iteration)
   {
      if (getInstance().getStatus() == EntityStatus.READONLY || (iteration != null && iteration.getStatus() == EntityStatus.READONLY))
      {
         return true;
      }
      return false;
   }

   public boolean isObsolete(HProjectIteration iteration)
   {
      if (getInstance().getStatus() == EntityStatus.OBSOLETE || (iteration != null && iteration.getStatus() == EntityStatus.OBSOLETE))
      {
         return true;
      }
      return false;
   }

   public boolean checkViewObsolete()
   {
      return identity != null && identity.hasPermission("HProject", "view-obsolete");
   }



   public void togglePanel(String versionSlug, Integer selectedRow)
   {
      rowsToUpdate.clear();
      rowsToUpdate.add(selectedRow);

      if (renderedPanel.contains(versionSlug))
      {
         renderedPanel.remove(versionSlug);
      }
      else
      {
         renderedPanel.add(versionSlug);
      }
   }

   public boolean checkIfRendered(String versionSlug)
   {
      return renderedPanel.contains(versionSlug);
   }
   
   public Set<HLocale> getIterationLocaleList()
   {
      HPerson person = personDAO.findByUsername(identity.getCredentials().getUsername());
      if (person != null)
      {
         return person.getLanguageMemberships();
      }
      return new HashSet<HLocale>();
   }
   
   public boolean isUserAllowedToTranslate(String versionSlug, HLocale localeId)
   {
      return isIterationActive(versionSlug) && identity != null && identity.hasPermission("add-translation", getInstance(), localeId);
   }
   

   
   @CachedMethodResult
   public TranslationStatistics getStats(String versionSlug, HLocale locale)
   {
      String[] localeIds = new String[1];
      localeIds[0] = locale.getLocaleId().getId();
      HProjectIteration iteration = projectIterationDAO.getBySlug(getSlug(), versionSlug);
      
      ContainerTranslationStatistics iterationStats = statisticsServiceImpl.getStatistics(getSlug(), versionSlug, false, true, localeIds);
      
      Long total;
      if (statsOption == WORD)
      {
         total = projectIterationDAO.getTotalWordCountForIteration(iteration.getId());
      }
      else
      {
         total = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      }
      
      TranslationStatistics stats = iterationStats.getStats(localeIds[0], statsOption);
      if (stats == null)
      {
         stats = new TranslationStatistics();
         stats.setUntranslated(total);
         stats.setTotal(total);
      }
      return stats;
   }

   private boolean isIterationActive(String versionSlug)
   {
      HProjectIteration version = projectIterationDAO.getBySlug(getSlug(), versionSlug);
      return getInstance().getStatus() == EntityStatus.ACTIVE || version.getStatus() == EntityStatus.ACTIVE;
   }

   public Set<Integer> getRowsToUpdate()
   {
      return rowsToUpdate;
   }

   public void setRowsToUpdate(Set<Integer> rowsToUpdate)
   {
      this.rowsToUpdate = rowsToUpdate;
   }
}
