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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionGroupService.SelectableHIterationProject;

@Name("versionGroupJoinAction")
@Scope(ScopeType.PAGE)
public class VersionGroupJoinAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private VersionGroupService versionGroupServiceImpl;

   @In
   private ProjectDAO projectDAO;

   @In
   private SendEmailAction sendEmail;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Logger
   Log log;

   private List<SelectableHIterationProject> maintainedProjectVersions;

   private HIterationGroup group;

   private String slug;

   public void init()
   {
      group = versionGroupServiceImpl.getBySlug(slug);

      Set<HProject> maintainedProjects = authenticatedAccount.getPerson().getMaintainerProjects();
      for (HProject project : maintainedProjects)
      {
         for (HProjectIteration projectIteration : projectDAO.getAllIterations(project.getSlug()))
         {
            getMaintainedProjectVersions().add(new SelectableHIterationProject(projectIteration, false));
         }
      }
   }

   public List<SelectableHIterationProject> getMaintainedProjectVersions()
   {
      if (maintainedProjectVersions == null)
      {
         maintainedProjectVersions = new ArrayList<SelectableHIterationProject>();
      }
      return maintainedProjectVersions;
   }

   public boolean isVersionInGroup(Long projectIterationId)
   {
      return versionGroupServiceImpl.isVersionInGroup(group, projectIterationId);
   }

   public String cancel()
   {
      return sendEmail.cancel();
   }

   public String send()
   {
      boolean isAnyVersionSelected = false;
      for (SelectableHIterationProject projectVersion : getMaintainedProjectVersions())
      {
         if (projectVersion.isSelected())
         {
            isAnyVersionSelected = true;
         }
      }

      if(isAnyVersionSelected)
      {
         group = versionGroupServiceImpl.getBySlug(slug);

         List<HPerson> maintainers = new ArrayList<HPerson>();
         for (HPerson maintainer : group.getMaintainers())
         {
            maintainers.add(maintainer);
         }
         return sendEmail.sendToVersionGroupMaintainer(maintainers);
      }
      else
      {
         FacesMessages.instance().add("#{messages['jsf.NoProjectVersionSelected']}");
         return "success";
      }

   }

   public HIterationGroup getGroup()
   {
      return group;
   }

   public String getSlug()
   {
      return slug;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   public String getQuery()
   {
      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append(slug);
      queryBuilder.append("/");
      if (!getMaintainedProjectVersions().isEmpty())
      {
         queryBuilder.append("?");

         for(int i = 0; i < getMaintainedProjectVersions().size(); i++)
         {
            SelectableHIterationProject projectVersion =  getMaintainedProjectVersions().get(i);
            if (projectVersion.isSelected())
            {
               if(i != 0)
               {
                  queryBuilder.append("&");
               }
               queryBuilder.append("slugParam=");
               queryBuilder.append(projectVersion.getProjectIteration().getProject().getSlug());
               queryBuilder.append(":");
               queryBuilder.append(projectVersion.getProjectIteration().getSlug());
            }
         }

//         for (SelectableHIterationProject projectVersion : getMaintainedProjectVersions())
//         {
//            if (projectVersion.isSelected())
//            {
//               queryBuilder.append("slugParam=");
//               queryBuilder.append(projectVersion.getProjectIteration().getProject().getSlug());
//               queryBuilder.append(":");
//               queryBuilder.append(projectVersion.getProjectIteration().getSlug());
//               queryBuilder.append("&");
//            }
//         }
      }
      return queryBuilder.toString();
   }
}
