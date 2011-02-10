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

import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HIterationProject;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HProjectIteration;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("projectHome")
public class ProjectHome extends SlugHome<HIterationProject>
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String slug;
   @Logger
   Log log;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Override
   protected HIterationProject loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (HIterationProject) session.createCriteria(getEntityClass()).add(Restrictions.naturalId().set("slug", getSlug())).setCacheable(true).uniqueResult();
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
         getEntityManager().createQuery("from HProject p where p.slug = :slug").setParameter("slug", slug).getSingleResult();
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
      String retValue = "";
      if (!validateSlug(getInstance().getSlug(), "slug"))
         return null;

      if (authenticatedAccount != null)
      {
         HPerson currentPerson = getEntityManager().find(HPerson.class, authenticatedAccount.getPerson().getId());
         if (currentPerson != null)
         {
            getInstance().getMaintainers().add(currentPerson);
            retValue = super.persist();
            Events.instance().raiseEvent("projectAdded");
         }
      }

      return retValue;
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getActiveIterations()
   {
      return getEntityManager().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.active = true").setParameter("projectSlug", slug).getResultList();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getRetiredIterations()
   {
      return getEntityManager().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.active = false").setParameter("projectSlug", slug).getResultList();
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

}
