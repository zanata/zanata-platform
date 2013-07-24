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
package org.zanata.service;

import java.util.List;

import org.zanata.common.UserActionType;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonActivity;
import org.zanata.model.HProjectIteration;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface PersonActivityService
{
   /**
    * Get user lastest activity on given action
    * 
    * @param personId
    * @param versionId
    * @param action
    * @return List<HPersonActivity>
    */
   HPersonActivity getPersonLastestActivity(Long personId, Long versionId, UserActionType action);
   
   /**
    * Get all user activities
    * @param personId
    * @param versionId
    * @param offset
    * @param count
    * @return List<HPersonActivity>
    */
   List<HPersonActivity> getAllPersonActivities(Long personId, Long versionId, int offset, int count);
   
   
   /**
    * Insert or update user activity, record roll up in hourly basis
    * 
    * @param personId
    * @param versionId
    * @param action
    */
   void insertOrUpdateActivity(HPerson person, HProjectIteration projectIteration, UserActionType action);
}
