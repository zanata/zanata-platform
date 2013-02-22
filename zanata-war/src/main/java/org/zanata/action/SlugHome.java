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

import static org.zanata.common.EntityStatus.OBSOLETE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.security.Identity;
import org.zanata.common.EntityStatus;
import org.zanata.rest.dto.ProjectType;

/**
 * This implementation uses a field 'slug' to refer to the id of the object.
 * 
 * @author asgeirf
 */
public abstract class SlugHome<E> extends EntityHome<E>
{

   private static final long serialVersionUID = 1L;

   private List<SelectItem> statusList = new ArrayList<SelectItem>();
   private List<SelectItem> projectTypeList = new ArrayList<SelectItem>();

   @SuppressWarnings("unchecked")
   @Override
   protected E loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (E) session.createCriteria(getEntityClass()).add(getNaturalId()).uniqueResult();
   }

   public abstract NaturalIdentifier getNaturalId();

   @Override
   public abstract boolean isIdDefined();

   @Override
   public abstract Object getId();

   public List<SelectItem> getStatusList()
   {
      if (statusList.isEmpty())
      {
         for (EntityStatus status : EntityStatus.values())
         {
            if (status == OBSOLETE)
            {
               if (Identity.instance().hasPermission(getInstance(), "mark-obsolete"))
               {
                  SelectItem option = new SelectItem(status, status.name());
                  statusList.add(option);
               }
            }
            else
            {
               // no restriction on other status
               SelectItem option = new SelectItem(status, status.name());
               statusList.add(option);
            }
         }
      }
      return statusList;
   }

   public List<SelectItem> getProjectTypeList()
   {
      if (projectTypeList.isEmpty())
      {
         projectTypeList.add(new SelectItem(null, getMessages().get("jsf.projectType.NoSelection")));
         for (ProjectType projectType : ProjectType.values())
         {
            SelectItem option = new SelectItem(projectType, projectType.name());
            projectTypeList.add(option);
         }
      }
      Collections.sort(projectTypeList, new Comparator<SelectItem>()
      {
         @Override
         public int compare(SelectItem o1, SelectItem o2)
         {
            if (o1.getValue() == null)
            {
               return -1;
            }
            if (o2.getValue() == null)
            {
               return 1;
            }
            return o1.getLabel().compareTo(o2.getLabel());
         }
      });
      return projectTypeList;
   }

}
