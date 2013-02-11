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

import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;

@Name("projectIterationHome")
@Slf4j
public class ProjectIterationHome extends SlugHome<HProjectIteration>
{

   private static final long serialVersionUID = 1L;

   public static final String PROJECT_ITERATION_UPDATE = "project.iteration.update";

   private String slug;
   private String projectSlug;

   @In(required = false)
   Map<String, String> iterationCustomizedItems;

   @In(required = false)
   private Boolean iterationOverrideLocales;

   /* Outjected from VersionValidationOptionsAction */
   @In(required = false)
   private Boolean versionOverrideValidations;

   /* Outjected from VersionValidationOptionsAction */
   @In(required = false)
   private Set<String> versionCustomizedValidations;

   @In
   LocaleService localeServiceImpl;

   @In
   SlugEntityService slugEntityServiceImpl;

   @In(create = true)
   ProjectDAO projectDAO;

   @Override
   protected HProjectIteration createInstance()
   {
      HProjectIteration iteration = new HProjectIteration();
      HProject project = (HProject) projectDAO.getBySlug(projectSlug);
      project.addIteration(iteration);
      iteration.setProjectType(project.getDefaultProjectType());
      return iteration;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   public String getSlug()
   {
      return slug;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public void validateSuppliedId()
   {
      getInstance(); // this will raise an EntityNotFound exception
      // when id is invalid and conversation will not
      // start
   }

   public void validateProjectSlug()
   {
      if (projectDAO.getBySlug(projectSlug) == null)
      {
         throw new EntityNotFoundException("no entity with slug " + projectSlug);
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
         FacesMessages.instance().addToControl(componentId, "This Version ID has been used in this project");
         return false;
      }
      return true;
   }

   public boolean isSlugAvailable(String slug)
   {
      return slugEntityServiceImpl.isProjectIterationSlugAvailable(slug, projectSlug);
   }

   @Override
   public String persist()
   {
      if (!validateSlug(getInstance().getSlug(), "slug"))
         return null;

      updateOverrideLocales();
      updateOverrideValidations();

      return super.persist();
   }

   public void cancel()
   {
   }

   @Override
   public Object getId()
   {
      return projectSlug + "/" + slug;
   }

   @Override
   public NaturalIdentifier getNaturalId()
   {
      return Restrictions.naturalId().set("slug", slug).set("project", projectDAO.getBySlug(projectSlug));
   }

   @Override
   public boolean isIdDefined()
   {
      return slug != null && projectSlug != null;
   }

   @Override
   public String update()
   {
      updateOverrideLocales();
      updateOverrideValidations();
      String state = super.update();
      Events.instance().raiseEvent(PROJECT_ITERATION_UPDATE, getInstance());
      return state;
   }
   
   public boolean isProjectActive()
   {
      return getInstance().getProject().getStatus() == EntityStatus.ACTIVE;
   }
   
   private void updateOverrideLocales()
   {
      if (iterationOverrideLocales != null)
      {
         getInstance().setOverrideLocales(iterationOverrideLocales);
         if (!iterationOverrideLocales)
         {
            getInstance().getCustomizedLocales().clear();
         }
         else if (iterationCustomizedItems != null)
         {
            Set<HLocale> locale = localeServiceImpl.convertCustomizedLocale(iterationCustomizedItems);
            getInstance().getCustomizedLocales().clear();
            getInstance().getCustomizedLocales().addAll(locale);
         }
      }
   }

   private void updateOverrideValidations()
   {
      if (versionOverrideValidations != null)
      {
         getInstance().setOverrideValidations(versionOverrideValidations);
         getInstance().getCustomizedValidations().clear();

         if (versionOverrideValidations)
         {
            getInstance().getCustomizedValidations().addAll(versionCustomizedValidations);
         }

         if (versionCustomizedValidations.isEmpty())
         {
            getInstance().setOverrideValidations(false);
         }
      }
   }
}
