package org.fedorahosted.flies.action;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.model.HCommunity;
import org.fedorahosted.flies.model.HPerson;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

@Name("communityHome")
public class CommunityHome extends SlugHome<HCommunity>
{

   private String slug;

   // @In(required=false, value=JpaIdentityStore.AUTHENTICATED_USER)
   // HAccount authenticatedAccount;

   @In(required = false)
   HPerson authenticatedPerson;

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
         getEntityManager().createQuery("from HCommunity c where c.slug = :slug").setParameter("slug", slug).getSingleResult();
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
      if (authenticatedPerson != null)
      {
         HPerson currentPerson = getEntityManager().find(HPerson.class, authenticatedPerson.getId());
         if (currentPerson != null)
            getInstance().setOwner(currentPerson);
      }
      return super.persist();
   }

   public void cancel()
   {
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

   public String getSlug()
   {
      return slug;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   @Override
   public Object getId()
   {
      return slug;
   }
}
