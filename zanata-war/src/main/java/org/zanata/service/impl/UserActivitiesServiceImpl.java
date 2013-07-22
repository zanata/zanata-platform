/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.zanata.common.UserActionType;
import org.zanata.dao.PersonActivitiesDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonActivities;
import org.zanata.model.HProjectIteration;
import org.zanata.service.UserActivitiesService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class UserActivitiesServiceImpl implements UserActivitiesService
{
   @In
   private PersonActivitiesDAO personActivitiesDAO;
   
   @In
   private PersonDAO personDAO;
   
   @In
   private ProjectIterationDAO projectIterationDAO;
   
   
   @Override
   public HPersonActivities getUserActivity(Long personId, Long versionId, UserActionType action)
   {
      return personActivitiesDAO.findUserActivity(personId, versionId, action);
   }
   
   @Override
   public List<HPersonActivities> getAllUserActivities(Long personId, Long versionId, int offset, int count)
   {
      return personActivitiesDAO.findAllUserActivities(personId, versionId, offset, count);
   }


   @Override
   public void insertOrUpdateActivity(Long personId, Long versionId, UserActionType action)
   {
      HPersonActivities activity = personActivitiesDAO.findUserActivity(personId, versionId, action);
      
      if(activity != null)
      {
         activity.setLastChanged(new Date());
      }
      else
      {
         HPerson person = personDAO.findById(personId, false);
         HProjectIteration projectIteration = projectIterationDAO.findById(versionId, false);
         
         activity = new HPersonActivities(person, projectIteration, action);
      }
      personActivitiesDAO.makePersistent(activity);
      personActivitiesDAO.flush();
   }

}
