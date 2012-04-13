/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.service.VersionGroupService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHome")
public class VersionGroupHome extends SlugHome<HIterationGroup>
{
   private String slug;

   private EntityStatus status = EntityStatus.ACTIVE;

   @In
   private VersionGroupService versionGroupServiceImpl;

   @DataModel
   List<HIterationGroup> allVersionGroups;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;
   
   @Override
   protected HIterationGroup loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (HIterationGroup) session.createCriteria(getEntityClass()).add(Restrictions.naturalId().set("slug", getSlug())).setCacheable(true).uniqueResult();
   }

   public void loadAllActiveGroups()
   {
      allVersionGroups = versionGroupServiceImpl.getAllActiveversionGroups();
   }

   @Override
   public NaturalIdentifier getNaturalId()
   {
      return Restrictions.naturalId().set("slug", slug);
   }

   @Override
   public boolean isIdDefined()
   {
      return slug != null;
   }

   @Override
   public Object getId()
   {
      return slug;
   }

   public String getSlug()
   {
      return slug;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   public void validateSuppliedId()
   {
      getInstance(); // this will raise an EntityNotFound exception
      // when id is invalid and conversation will not
      // start
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
         getEntityManager().createQuery("from HIterationGroup g where g.slug = :slug").setParameter("slug", slug).getSingleResult();
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
      getInstance().setStatus(status);

      if (authenticatedAccount != null)
      {
         getInstance().addMaintainer(authenticatedAccount.getPerson());
      }
      
      return super.persist();
   }

   @Override
   public String update()
   {
      String state = super.update();
      return state;
   }
}
