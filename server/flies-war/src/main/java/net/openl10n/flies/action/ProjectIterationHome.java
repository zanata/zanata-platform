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
package net.openl10n.flies.action;

import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

import net.openl10n.flies.dao.ProjectDAO;
import net.openl10n.flies.model.HIterationProject;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.service.LocaleService;

import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("projectIterationHome")
public class ProjectIterationHome extends SlugHome<HProjectIteration>
{

   private static final long serialVersionUID = 1L;
   private String slug;
   private String projectSlug;
   @In(required = false)
   Map<String, String> iterationCustomizedItems;
   @In(required = false)
   private Boolean iterationOverrideLocales;
   @In
   LocaleService localeServiceImpl;

   @Logger
   Log log;

   @In(create = true)
   ProjectDAO projectDAO;

   @Override
   protected HProjectIteration createInstance()
   {
      HProjectIteration iteration = new HProjectIteration();
      iteration.setProject((HIterationProject) projectDAO.getBySlug(projectSlug));
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
         FacesMessages.instance().addToControl(componentId, "This slug is not available");
         return false;
      }
      return true;
   }

   public boolean isSlugAvailable(String slug)
   {
      try
      {
         getEntityManager().createQuery("from HProjectIteration t where t.slug = :slug and t.project.slug = :projectSlug").setParameter("slug", slug).setParameter("projectSlug", projectSlug).getSingleResult();
         return false;
      }
      catch (NoResultException e)
      {
         // pass
      }
      return true;
   }

   @Override
   public String persist()
   {
      if (!validateSlug(getInstance().getSlug(), "slug"))
         return null;

      getInstance().setOverrideLocales(iterationOverrideLocales);
      if (iterationCustomizedItems != null && iterationOverrideLocales != null && iterationOverrideLocales.booleanValue())
      {
         Set<HLocale> locale = localeServiceImpl.convertCustomizedLocale(iterationCustomizedItems);
         getInstance().getCustomizedLocales().clear();
         getInstance().getCustomizedLocales().addAll(locale);
      }

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
      getInstance().setOverrideLocales(iterationOverrideLocales);
      if (iterationCustomizedItems != null && iterationOverrideLocales != null && iterationOverrideLocales.booleanValue())
      {
         Set<HLocale> locale = localeServiceImpl.convertCustomizedLocale(iterationCustomizedItems);
         getInstance().getCustomizedLocales().clear();
         getInstance().getCustomizedLocales().addAll(locale);
      }
      return super.update();
   }

}
