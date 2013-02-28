/**
 * 
 */
package org.zanata.service.impl;

import java.util.Collection;
import java.util.HashMap;
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
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
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
   public List<ValidationObject> getValidationObject(String projectSlug)
   {
      List<ValidationObject> validationList = ValidationFactory.getAllValidationObject();
      Set<String> enabledValidations = new HashSet<String>();

      if (!StringUtils.isEmpty(projectSlug))
      {
         HProject project = projectDAO.getBySlug(projectSlug);
         enabledValidations = project.getCustomizedValidations();
      }

      for (ValidationObject  valObj: validationList)
      {
         ValidationInfo actionInfo = valObj.getValidationInfo();

         actionInfo.setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         if (enabledValidations.contains(actionInfo.getId().name()))
         {
            actionInfo.setEnabled(true);
         }
      }
     
      return validationList;
   }
   
   @Override
   public List<ValidationObject> getValidationObject(String projectSlug, String versionSlug)
   {
      List<ValidationObject> validationList = ValidationFactory.getAllValidationObject();
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

      for (ValidationObject valObj : validationList)
      {
         ValidationInfo actionInfo = valObj.getValidationInfo();

         actionInfo.setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         if (enabledValidations.contains(actionInfo.getId().name()))
         {
            actionInfo.setEnabled(true);
            actionInfo.setLocked(true);
         }
      }
      return validationList;
   }

   /**
    * Run validation check on HTextFlow and HTextFlowTarget with specific locale
    * from list of HDocuments against validations rules
    * 
    * @param hDocs
    * @param validations
    * @param localeId
    */
   public void runValidations(Collection<HDocument> hDocs, List<ValidationObject> validations, Long localeId)
   {
      Map<String, Boolean> docValidationResult = new HashMap<String, Boolean>();

      for (HDocument hDoc : hDocs)
      {
         for(HTextFlow textFlow: hDoc.getTextFlows())
         {
            HTextFlowTarget target = textFlow.getTargets().get(localeId);
            if (target != null)
            {
               for (ValidationObject validation : validations)
               {
                  validation.validate(textFlow.getContents().get(0), target.getContents().get(0));
                  if (validation.hasError())
                  {

                  }
               }
            }
         }
      }
   }
}
