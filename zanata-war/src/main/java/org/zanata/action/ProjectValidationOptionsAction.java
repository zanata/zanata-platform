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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationObject;

@Name("projectValidationOptionsAction")
@Scope(ScopeType.PAGE)
public class ProjectValidationOptionsAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Out(required = false)
   private Boolean overrideValidations;

   @Logger
   private Log log;

   @In
   private ValidationService validationServiceImpl;

   @In(required = false)
   private ProjectHome projectHome;

   private Map<String, Boolean> selectedValidations;

   private String projectSlug;

   public List<ValidationObject> getValidationList()
   {
      List<ValidationObject> result = new ArrayList<ValidationObject>();
      Map<ValidationId, ValidationObject> validationMap = validationServiceImpl.getValidationObject(projectSlug);
      
      for (Map.Entry<ValidationId, ValidationObject> entry : validationMap.entrySet())
      {
         result.add(entry.getValue());
      }

      Collections.sort(result, validationServiceImpl.getObjectComparator());
      return result;
   }

   @Out(required = false)
   public Set<String> getCustomizedValidations()
   {
      Set<String> customizedValidationSet = new HashSet<String>();
      for (Map.Entry<String, Boolean> entry : getSelectedValidations().entrySet())
      {
         if (entry.getValue() == Boolean.TRUE)
         {
            customizedValidationSet.add(entry.getKey());
         }
      }
      return customizedValidationSet;
   }

   public boolean getOverrideValidations()
   {
      if (overrideValidations == null)
      {
         overrideValidations = projectHome.getInstance().getOverrideValidations();
      }
      return overrideValidations;
   }

   public void setOverrideValidations(boolean overrideValidations)
   {
      this.overrideValidations = overrideValidations;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public Map<String, Boolean> getSelectedValidations()
   {
      if(selectedValidations == null)
      {
         selectedValidations = new HashMap<String, Boolean>();
         for (String val : projectHome.getInstance().getCustomizedValidations())
         {
            selectedValidations.put(val, true);
         }
      }
      return selectedValidations;
   }

   public void setSelectedValidations(Map<String, Boolean> selectedValidations)
   {
      this.selectedValidations = selectedValidations;
   }
}
