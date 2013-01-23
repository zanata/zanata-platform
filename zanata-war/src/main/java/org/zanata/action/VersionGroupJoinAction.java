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

import org.apache.commons.lang.StringUtils;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.*;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionGroupService.SelectableHProject;

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
   private ProjectIterationDAO projectIterationDAO;

   @In(create=true)
   private SendEmailAction sendEmail;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Logger
   Log log;

   private List<SelectableHProject> projectVersions;

   private HIterationGroup group;

   private String slug;

   private String iterationSlug;

   private String projectSlug;

   public void searchMaintainedProjectVersion()
   {
      Set<HProject> maintainedProjects = authenticatedAccount.getPerson().getMaintainerProjects();
      for (HProject project : maintainedProjects)
      {
         for (HProjectIteration projectIteration : projectDAO.getAllIterations(project.getSlug()))
         {
            getProjectVersions().add(new SelectableHProject(projectIteration, false));
         }
      }
   }

   public void searchProjectVersion()
   {
      if(StringUtils.isNotEmpty(iterationSlug) && StringUtils.isNotEmpty(projectSlug))
      {
          HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
          if(projectIteration != null)
          {
            getProjectVersions().add(new SelectableHProject(projectIteration, true));
          }

      }
   }

   public List<SelectableHProject> getProjectVersions()
   {
      if (projectVersions == null)
      {
         projectVersions = new ArrayList<SelectableHProject>();
      }
      return projectVersions;
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
      for (SelectableHProject projectVersion : getProjectVersions())
      {
         if (projectVersion.isSelected())
         {
            isAnyVersionSelected = true;
         }
      }
      if (isAnyVersionSelected)
      {
         List<HPerson> maintainers = new ArrayList<HPerson>();
         for (HPerson maintainer : versionGroupServiceImpl.getMaintainerBySlug(slug))
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
      if (group == null)
      {
         group = versionGroupServiceImpl.getBySlug(slug);
      }
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

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getQuery()
   {
      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append(slug);
      queryBuilder.append("/");
      if (!getProjectVersions().isEmpty())
      {
         queryBuilder.append("?");

         for (int i = 0; i < getProjectVersions().size(); i++)
         {
            SelectableHProject projectVersion = getProjectVersions().get(i);
            if (projectVersion.isSelected())
            {
               if (i != 0)
               {
                  queryBuilder.append("&");
               }
               queryBuilder.append("slugParam=");
               queryBuilder.append(projectVersion.getProjectIteration().getProject().getSlug());
               queryBuilder.append(":");
               queryBuilder.append(projectVersion.getProjectIteration().getSlug());
            }
         }
      }
      return queryBuilder.toString();
   }
}
