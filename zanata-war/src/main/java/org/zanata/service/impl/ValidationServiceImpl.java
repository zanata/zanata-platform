/**
 * 
 */
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationObject;
import org.zanata.webtrans.shared.validation.ValidationFactory;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("validationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ValidationServiceImpl implements ValidationService
{
   @In
   private ZanataMessages zanataMessages;
   
   @In
   private ProjectDAO projectDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   private static final String DESC_KEY = ".desc";

   @Override
   public Map<ValidationId, ValidationObject> getValidationObject(String projectSlug)
   {
      Map<ValidationId, ValidationObject> validationMap = ValidationFactory.getAllValidationObject();
      Set<String> enabledValidations = new HashSet<String>();

      if (!StringUtils.isEmpty(projectSlug))
      {
         HProject project = projectDAO.getBySlug(projectSlug);
         enabledValidations = project.getCustomizedValidations();
      }

      for (Map.Entry<ValidationId, ValidationObject> entry : validationMap.entrySet())
      {
         ValidationInfo actionInfo = entry.getValue().getValidationInfo();

         entry.getValue().getValidationInfo().setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         if (enabledValidations.contains(actionInfo.getId().name()))
         {
            actionInfo.setEnabled(true);
         }
      }
      return validationMap;
   }

   @Override
   public Map<ValidationId, ValidationObject> getValidationObject(String projectSlug, String versionSlug)
   {
      Map<ValidationId, ValidationObject> validationMap = ValidationFactory.getAllValidationObject();
      Set<String> enabledValidations = new HashSet<String>();

      if (!StringUtils.isEmpty(projectSlug) && !StringUtils.isEmpty(versionSlug))
      {
         HProjectIteration version = projectIterationDAO.getBySlug(projectSlug, versionSlug);
         enabledValidations = version.getCustomizedValidations();

         // Inherits validations from project if version has no defined
         // validations
         if (enabledValidations.isEmpty())
         {
            enabledValidations = version.getProject().getCustomizedValidations();
         }
      }

      for (Map.Entry<ValidationId, ValidationObject> entry : validationMap.entrySet())
      {
         ValidationInfo actionInfo = entry.getValue().getValidationInfo();

         entry.getValue().getValidationInfo().setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         if (enabledValidations.contains(actionInfo.getId().name()))
         {
            actionInfo.setEnabled(true);
         }
      }
      return validationMap;
   }

   @Override
   public Comparator<ValidationObject> getObjectComparator()
   {
      return ValidationFactory.ObjectComparator;
   }
}
