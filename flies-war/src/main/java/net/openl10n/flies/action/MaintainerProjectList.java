package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HProject;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("maintainerProjectList")
@Scope(ScopeType.SESSION)
public class MaintainerProjectList implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   PersonDAO personDAO;

   @Logger
   Log log;

   private List<HProject> maintainerProjects;

   @Create
   public void onCreate()
   {
      fetchMaintainerProjects();
   }

   public List<HProject> getMaintainerProjects()
   {
      return maintainerProjects;
   }

   @Observer("projectAdded")
   synchronized public void fetchMaintainerProjects()
   {
      log.debug("refreshing projects...");
      if (authenticatedAccount == null)
      {
         maintainerProjects = Collections.emptyList();
         return;
      }

      maintainerProjects = personDAO.getMaintainerProjects(authenticatedAccount.getUsername());
      log.debug("now listing {0} projects", maintainerProjects.size());
   }
}
