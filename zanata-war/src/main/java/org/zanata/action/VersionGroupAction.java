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
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.zanata.common.EntityStatus;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;

@Name("versionGroupAction")
@Scope(ScopeType.PAGE)
public class VersionGroupAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private VersionGroupService versionGroupServiceImpl;

   @Logger
   Log log;

   private List<HIterationGroup> allVersionGroups;
   private List<HProjectIteration> searchResults;

   private HIterationGroup group;

   private String searchTerm;
   private String groupNameFilter;

   private boolean showActiveGroups = true;
   private boolean showObsoleteGroups = false;

   public void loadAllActiveGroupsOrIsMaintainer()
   {
      allVersionGroups = versionGroupServiceImpl.getAllActiveVersionGroupsOrIsMaintainer();
   }

   public void init(String slug)
   {
      group = versionGroupServiceImpl.getBySlug(slug);
   }

   public String getSearchTerm()
   {
      return searchTerm;
   }

   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }

   public List<HProjectIteration> getSearchResults()
   {
      return searchResults;
   }

   public boolean isVersionInGroup(Long projectIterationId)
   {
      for (HProjectIteration iteration : group.getProjectIterations())
      {
         if (iteration.getId().equals(projectIterationId))
         {
            return true;
         }
      }
      return false;
   }

   @Transactional
   @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
   public void joinVersionGroup(Long projectIterationId)
   {
      versionGroupServiceImpl.joinVersionGroup(group.getSlug(), projectIterationId);
   }

   @Transactional
   public void leaveVersionGroup(Long projectIterationId)
   {
      versionGroupServiceImpl.leaveVersionGroup(group.getSlug(), projectIterationId);
   }

   public void searchProjectAndVersion()
   {
      try
      {
         this.searchResults = versionGroupServiceImpl.searchLikeSlugOrProjectSlug(this.searchTerm);
      }
      catch (ParseException e)
      {
         return;
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
}
