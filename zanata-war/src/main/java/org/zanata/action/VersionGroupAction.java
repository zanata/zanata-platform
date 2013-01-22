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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionGroupService.SelectableHProject;

@Name("versionGroupAction")
@Scope(ScopeType.PAGE)
public class VersionGroupAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private VersionGroupService versionGroupServiceImpl;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @RequestParameter
   private String[] slugParam;

   @Logger
   Log log;

   private List<HIterationGroup> allVersionGroups;

   private List<SelectableHProject> searchResults;

   private HIterationGroup group;

   private String searchTerm = "";
   private String groupNameFilter;

   private boolean showActiveGroups = true;
   private boolean showObsoleteGroups = false;
   private boolean selectAll = false;

   public boolean isParamExists()
   {
      return slugParam != null && slugParam.length != 0;
   }

   public void loadAllActiveGroupsOrIsMaintainer()
   {
      allVersionGroups = versionGroupServiceImpl.getAllActiveVersionGroupsOrIsMaintainer();
   }

   public void init(String slug)
   {
      group = versionGroupServiceImpl.getBySlug(slug);
   }

   public void addSelected()
   {
      for (SelectableHProject selectableVersion : getSearchResults())
      {
         if (selectableVersion.isSelected())
         {
            joinVersionGroup(selectableVersion.getProjectIteration().getId());
         }
      }
   }

   public void selectAll()
   {
      for (SelectableHProject selectableVersion : getSearchResults())
      {
         if (!isVersionInGroup(selectableVersion.getProjectIteration().getId()))
         {
            selectableVersion.setSelected(selectAll);
         }
      }
   }

   /**
    * Run search on unique project version if projectSlug, iterationSlug exits
    * else search versions available
    */
   public void executePreSearch()
   {
      if (isParamExists())
      {
         for (String param : slugParam)
         {
            String[] paramSet = param.split(":");

            if (paramSet.length == 2)
            {
               HProjectIteration projectVersion = versionGroupServiceImpl.getProjectIterationBySlug(paramSet[0],  paramSet[1]);
               if (projectVersion != null)
               {
                  getSearchResults().add(new SelectableHProject(projectVersion, true));
               }
            }
         }
      }
      else
      {
         searchProjectAndVersion();
      }

   }

   public String getSearchTerm()
   {
      return searchTerm;
   }

   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }

   public List<SelectableHProject> getSearchResults()
   {
      if (searchResults == null)
      {
         searchResults = new ArrayList<SelectableHProject>();
      }
      return searchResults;
   }

   public boolean isVersionInGroup(Long projectIterationId)
   {
     return versionGroupServiceImpl.isVersionInGroup(group, projectIterationId);
   }

   @Transactional
   @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
   private void joinVersionGroup(Long projectIterationId)
   {
      versionGroupServiceImpl.joinVersionGroup(group.getSlug(), projectIterationId);
   }

   @Transactional
   @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
   public void leaveVersionGroup(Long projectIterationId)
   {
      versionGroupServiceImpl.leaveVersionGroup(group.getSlug(), projectIterationId);
      searchProjectAndVersion();
   }

   public void searchProjectAndVersion()
   {
      getSearchResults().clear();
      List<HProjectIteration> result = versionGroupServiceImpl.searchLikeSlugOrProjectSlug(this.searchTerm);
      for (HProjectIteration version : result)
      {
         getSearchResults().add(new SelectableHProject(version, false));
      }
   }

   public boolean filterGroupByName(Object groupObject)
   {
      final HIterationGroup group = (HIterationGroup) groupObject;

      if (this.groupNameFilter != null && this.groupNameFilter.length() > 0)
      {
         return group.getName().toLowerCase().contains(this.groupNameFilter.toLowerCase());
      }
      else
      {
         return true;
      }
   }

   public boolean filterGroupByStatus(Object groupObject)
   {
      final HIterationGroup group = (HIterationGroup) groupObject;
      if (isShowActiveGroups() && isShowObsoleteGroups())
      {
         return true;
      }

      if (group.getStatus() == EntityStatus.OBSOLETE)
      {
         return isShowObsoleteGroups();
      }
      else if (group.getStatus() == EntityStatus.ACTIVE)
      {
         return isShowActiveGroups();
      }
      return false;
   }

   public boolean isUserProjectMaintainer()
   {
      return authenticatedAccount != null && authenticatedAccount.getPerson().isMaintainerOfProjects();
   }

   public String getGroupNameFilter()
   {
      return groupNameFilter;
   }

   public void setGroupNameFilter(String groupNameFilter)
   {
      this.groupNameFilter = groupNameFilter;
   }

   public List<HIterationGroup> getAllVersionGroups()
   {
      return allVersionGroups;
   }

   public void setAllVersionGroups(List<HIterationGroup> allVersionGroups)
   {
      this.allVersionGroups = allVersionGroups;
   }

   public boolean isShowActiveGroups()
   {
      return showActiveGroups;
   }

   public void setShowActiveGroups(boolean showActiveGroups)
   {
      this.showActiveGroups = showActiveGroups;
   }

   public boolean isShowObsoleteGroups()
   {
      return showObsoleteGroups;
   }

   public void setShowObsoleteGroups(boolean showObsoleteGroups)
   {
      this.showObsoleteGroups = showObsoleteGroups;
   }

   public boolean isSelectAll()
   {
      return selectAll;
   }

   public void setSelectAll(boolean selectAll)
   {
      this.selectAll = selectAll;
   }
}
