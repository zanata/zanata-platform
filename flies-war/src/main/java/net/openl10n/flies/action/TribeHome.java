package net.openl10n.flies.action;

import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HFliesLocale;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HTribe;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("tribeHome")
@Scope(ScopeType.EVENT)
public class TribeHome extends EntityHome<HTribe>
{

   private static final long serialVersionUID = 5139154491040234980L;

   private int maxNumberOfTribeMemberships = Integer.MAX_VALUE;

   @Override
   protected HTribe loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (HTribe) session.createCriteria(getEntityClass()).add(Restrictions.naturalId().set("locale", getEntityManager().find(HFliesLocale.class, getId()))).setCacheable(true).uniqueResult();
   }

   public void validateSuppliedId()
   {
      getInstance(); // this will raise an EntityNotFound exception
      // when id is invalid and conversation will not
      // start
   }

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Transactional
   public void joinTribe()
   {

      if (authenticatedAccount == null)
      {
         getLog().error("failed to load auth person");
         return;
      }
      HPerson currentPerson = getEntityManager().find(HPerson.class, authenticatedAccount.getPerson().getId());

      if (!getInstance().getMembers().contains(currentPerson))
      {
         if (currentPerson.getTribeMemberships().size() >= maxNumberOfTribeMemberships)
         {
            FacesMessages.instance().add(Severity.ERROR, "You can only be a member of up to " + maxNumberOfTribeMemberships + " tribes at one time.");
         }
         else
         {
            getInstance().getMembers().add(currentPerson);
            getEntityManager().flush();
            Events.instance().raiseEvent("personJoinedTribe", currentPerson, getInstance());
            getLog().info("{0} joined tribe {1}", authenticatedAccount.getUsername(), getId());
            FacesMessages.instance().add("You are now a member of the {0} tribe", getInstance().getLocale().getNativeName());
         }
      }
   }

   @Transactional
   public void leaveTribe()
   {

      if (authenticatedAccount == null)
      {
         getLog().error("failed to load auth person");
         return;
      }
      HPerson currentPerson = getEntityManager().find(HPerson.class, authenticatedAccount.getPerson().getId());

      if (getInstance().getMembers().contains(currentPerson))
      {
         getInstance().getMembers().remove(currentPerson);
         getEntityManager().flush();
         Events.instance().raiseEvent("personLeftTribe", currentPerson, getInstance());
         getLog().info("{0} left tribe {1}", authenticatedAccount.getUsername(), getId());
         FacesMessages.instance().add("You have left the {0} tribe", getInstance().getLocale().getNativeName());
      }
   }

   public void cancel()
   {
   }

}
