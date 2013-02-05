/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationObject;

@Name("validationOptionsAction")
@Scope(ScopeType.PAGE)
public class ValidationOptionsAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   private List<String> definedList;
   private List<String> selectedList;

   private Map<String, String> availableItems;

   @Out(required = false)
   private Map<String, String> customizedValidations;

   private Map<String, ValidationObject> globalList;

   @Out(required = false)
   private Boolean overrideValidations;

   private boolean setting;

   @Logger
   Log log;

   @In
   ProjectDAO projectDAO;

   private String versionSlug;

   private String projectSlug;

   @In
   private ValidationService validationServiceImpl;

   @Create
   public void onCreate()
   {
      availableItems = new TreeMap<String, String>();
      customizedValidations = new TreeMap<String, String>();
      globalList = new TreeMap<String, ValidationObject>();
      overrideValidations = null;
   }

   public void initForProject()
   {
      if (overrideValidations == null)
      {
         definedList = new ArrayList<String>();
         selectedList = new ArrayList<String>();

         Map<ValidationId, ValidationObject> validationMap = validationServiceImpl.getValidationObject(projectSlug);

         for (Map.Entry<ValidationId, ValidationObject> entry : validationMap.entrySet())
         {
            ValidationInfo info = entry.getValue().getValidationInfo();
            if (info.isEnabled())
            {
               customizedValidations.put(info.getId().getDisplayName(), info.getId().name());
            }
            else
            {
               availableItems.put(info.getId().getDisplayName(), info.getId().name());
            }
            globalList.put(info.getId().name(), entry.getValue());
         }
      }
   }

   public void toDefinedList()
   {
      if (!selectedList.isEmpty())
      {
         for (String var : selectedList)
         {
            ValidationObject valObj = globalList.get(var);

            // check and remove any exclusive validations
            for (ValidationObject mutualObj : valObj.getExclusiveValidations())
            {
               ValidationId validationId = mutualObj.getValidationInfo().getId();
               if (customizedValidations.containsKey(validationId.getDisplayName()))
               {
                  customizedValidations.remove(validationId.getDisplayName());
                  availableItems.put(validationId.getDisplayName(), validationId.name());
               }
            }
               
            customizedValidations.put(valObj.getValidationInfo().getId().getDisplayName(), var);
            availableItems.remove(valObj.getValidationInfo().getId().getDisplayName());
         }
      }
   }

   public void removeFromDefinedList()
   {
      if (!definedList.isEmpty())
      {
         for (String var : definedList)
         {
            ValidationInfo info = globalList.get(var).getValidationInfo();
            customizedValidations.remove(info.getId().getDisplayName());
            availableItems.put(info.getId().getDisplayName(), var);
         }
      }
   }

   public List<String> getDefinedList()
   {
      return definedList;
   }

   public void setDefinedList(List<String> definedList)
   {
      this.definedList = definedList;
   }

   public List<String> getSelectedList()
   {
      return selectedList;
   }

   public void setSelectedList(List<String> selectedList)
   {
      this.selectedList = selectedList;
   }

   public Map<String, String> getAvailableItems()
   {
      return availableItems;
   }

   public Map<String, String> getCustomizedValidations()
   {
      return customizedValidations;
   }

   public boolean getSetting()
   {
      if (overrideValidations == null)
      {
         HProject project = projectDAO.getBySlug(projectSlug);
         setting = project.getOverrideValidations();

         overrideValidations = new Boolean(setting);
      }
      return setting;
   }

   public void setSetting(boolean setting)
   {
      this.setting = setting;
      overrideValidations = new Boolean(setting);
   }

   public String getVersionSlug()
   {
      return versionSlug;
   }

   public void setVersionSlug(String versionSlug)
   {
      this.versionSlug = versionSlug;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }
}
