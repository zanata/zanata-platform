/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionGroupServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class VersionGroupServiceImpl implements VersionGroupService
{
   @In
   private VersionGroupDAO versionGroupDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @Override
   public List<HIterationGroup> getAllActiveVersionGroupsOrIsMaintainer()
   {
      List<HIterationGroup> activeVersions = versionGroupDAO.getAllActiveVersionGroups();
      List<HIterationGroup> obsoleteVersions = versionGroupDAO.getAllObsoleteVersionGroups();

      List<HIterationGroup> filteredList = new ArrayList<HIterationGroup>();
      for (HIterationGroup obsoleteGroup : obsoleteVersions)
      {
         if (authenticatedAccount != null)
         {
            if (authenticatedAccount.getPerson().isMaintainer(obsoleteGroup))
            {
               filteredList.add(obsoleteGroup);
            }
         }
      }

      activeVersions.addAll(filteredList);

      return activeVersions;
   }

   @Override
   public HProjectIteration getProjectIterationBySlug(String projectSlug, String iterationSlug)
   {
      return projectIterationDAO.getBySlug(projectSlug, iterationSlug);
   }

   @Override
   public HIterationGroup getBySlug(String slug)
   {
      return versionGroupDAO.getBySlug(slug);
   }

   @Override
   public List<HProjectIteration> searchLikeSlugOrProjectSlug(String searchTerm)
   {
      return projectIterationDAO.searchLikeSlugOrProjectSlug(searchTerm);
   }

   @Override
   public List<HPerson> getMaintainerBySlug(String slug)
   {
      return versionGroupDAO.getMaintainerBySlug(slug);
   }

   @Override
   public void makePersistent(HIterationGroup iterationGroup)
   {
      versionGroupDAO.makePersistent(iterationGroup);
   }

   @Override
   public void flush()
   {
      versionGroupDAO.flush();
   }

   @Override
   public boolean joinVersionGroup(String slug, Long projectIterationId)
   {
      HProjectIteration projectIteration = projectIterationDAO.findById(projectIterationId, false);
      HIterationGroup group = getBySlug(slug);
      if (group != null && projectIteration != null)
      {
         if (!group.getProjectIterations().contains(projectIteration))
         {
            group.addProjectIteration(projectIteration);
            versionGroupDAO.makePersistent(group);
            flush();
            return true;
         }
      }
      return false;

   }

   @Override
   public boolean leaveVersionGroup(String slug, Long projectIterationId)
   {
      HProjectIteration projectIteration = projectIterationDAO.findById(projectIterationId, false);
      HIterationGroup group = getBySlug(slug);

      if (group != null && projectIteration != null)
      {
         if (group.getProjectIterations().contains(projectIteration))
         {
            group.getProjectIterations().remove(projectIteration);
            versionGroupDAO.makePersistent(group);
            flush();
            return true;
         }
      }
      return false;
   }
}
