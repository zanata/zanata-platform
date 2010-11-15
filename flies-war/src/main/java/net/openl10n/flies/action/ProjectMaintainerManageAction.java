package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.dao.ProjectDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HProject;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("projectMaintainerManageAction")
@Scope(ScopeType.PAGE)
public class ProjectMaintainerManageAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @DataModel
   List<HPerson> allList;
   @DataModelSelection
   HPerson selectedPerson;
   private String slug;
   @In
   ProjectDAO projectDAO;
   @In
   AccountDAO accountDAO;
   @Logger
   Log log;

   public void loadAllMaintainers()
   {
      allList = projectDAO.getProjectMaintainers(this.slug);
   }

   public HPerson getSelectedPerson()
   {
      return this.selectedPerson;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   public String getSlug()
   {
      return this.slug;
   }

   public void deleteMaintainer(HPerson person)
   {
      log.debug("try to delete maintainer {0} from slug {1}", person.getName(), this.slug);
      HProject project = projectDAO.getBySlug(this.slug);
      Set<HPerson> personList = project.getMaintainers();
      for (HPerson l : personList)
      {
         if (l.getEmail().equals(person.getEmail()))
         {
            log.debug("remove the person");
            project.getMaintainers().remove(l);
            break;
         }
      }

      projectDAO.makePersistent(project);
      projectDAO.flush();
   }

   public String addMaintainers(String account)
   {
      HAccount a = accountDAO.getByUsername(account);
      if (a != null && a.isEnabled())
      {
         HProject project = projectDAO.getBySlug(this.slug);
         Set<HPerson> personList = project.getMaintainers();
         personList.add(a.getPerson());
         projectDAO.makePersistent(project);
         projectDAO.flush();
         log.debug("add {0} into maintainers", account);
         return "success";
      }
      FacesMessages.instance().add("This account does not exist.");
      return "failure";
   }

}
