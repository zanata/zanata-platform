package org.zanata.security;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.seam.security.EntitySecurityListener;
import org.jboss.seam.security.Identity;

/**
 * Overrides EntitySecurityListener to avoid calling
 * EntityPermissionChecker.instance() when security is disabled, which leads to
 * problems if there is no active Seam application context (in tests).
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class SmartEntitySecurityListener extends EntitySecurityListener
{
   @PostLoad
   public void postLoad(Object entity)
   {
      if (Identity.isSecurityEnabled())
         super.postLoad(entity);
   }

   @PrePersist
   public void prePersist(Object entity)
   {
      if (Identity.isSecurityEnabled())
         super.prePersist(entity);
   }

   @PreUpdate
   public void preUpdate(Object entity)
   {
      if (Identity.isSecurityEnabled())
         super.preUpdate(entity);
   }

   @PreRemove
   public void preRemove(Object entity)
   {
      if (Identity.isSecurityEnabled())
         super.preRemove(entity);
   }

}
