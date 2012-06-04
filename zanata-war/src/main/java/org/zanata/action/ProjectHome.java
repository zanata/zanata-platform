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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;

@Name("projectHome")
public class ProjectHome extends SlugHome<HIterationProject>
{
   private static final long serialVersionUID = 1L;

   public static final String PROJECT_UPDATE = "project.update";
   
   private String slug;

   @In
   Identity identity;

   @Logger
   Log log;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In(required = false)
   Map<String, String> customizedItems;

   @In(required = false)
   private Boolean overrideLocales;

   @In
   LocaleService localeServiceImpl;

   @In
   SlugEntityService slugEntityServiceImpl;

   @In(create = true)
   ProjectDAO projectDAO;

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
         getInstance().addMaintainer(authenticatedAccount.getPerson());
         retValue = super.persist();
         Events.instance().raiseEvent("projectAdded");
      }
      return retValue;
   }

   public List<HProjectIteration> getActiveIterations()
   {
      return projectDAO.getActiveIterations(slug);
   }

   public List<HProjectIteration> getReadOnlyIterations()
   {
      return projectDAO.getReadOnlyIterations(slug);
   }

   public List<HProjectIteration> getObsoleteIterations()
   {
      return projectDAO.getObsoleteIterations(slug);
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
      String state = super.update();
      Events.instance().raiseEvent(PROJECT_UPDATE, getInstance());
      return state;
   }

   private void updateOverrideLocales()
   {
      if (overrideLocales != null)
      {
         getInstance().setOverrideLocales(overrideLocales);
         if (!overrideLocales.booleanValue())
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
      return identity != null && identity.hasPermission("HProject", "view-obsolete", null);
   }
}
