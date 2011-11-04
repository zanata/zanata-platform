package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.security.BaseSecurityChecker;

@Name("projectMaintainerManageAction")
@Scope(ScopeType.PAGE)
public class ProjectMaintainerManageAction extends BaseSecurityChecker implements Serializable
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
      allList = projectDAO.getProjectMaintainerBySlug(this.slug);
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

   public HProject getProject()
   {
      return projectDAO.getBySlug(this.slug);
   }

   @Restrict("#{projectMaintainerManageAction.checkPermission('update')}")
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

   @Restrict("#{projectMaintainerManageAction.checkPermission('update')}")
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

   public String cancel()
   {
      return "cancel";
   }
   
   @Override
   public Object getSecuredEntity()
   {
      return this.getProject();
   }

}
