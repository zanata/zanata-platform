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
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
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
   private TransUnitTransformer transUnitTransformer;

   @In
   private ProjectIterationDAO projectIterationDAO;

   private static final String DESC_KEY = ".desc";

   @Override
   public Collection<ValidationAction> getValidationAction(String projectSlug)
   {
      Collection<ValidationAction> validationList = ValidationFactory.getAllValidationActions(null).values();
      Set<String> enabledValidations = new HashSet<String>();

      if (!StringUtils.isEmpty(projectSlug))
      {
         HProject project = projectDAO.getBySlug(projectSlug);
         enabledValidations = project.getCustomizedValidations();
      }

      for (ValidationAction valAction : validationList)
      {
         ValidationInfo actionInfo = valAction.getValidationInfo();

         actionInfo.setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         if (enabledValidations.contains(actionInfo.getId().name()))
         {
            actionInfo.setEnabled(true);
         }
      }
     
      return validationList;
   }
   
   @Override
   public Collection<ValidationAction> getValidationAction(String projectSlug, String versionSlug)
   {
      Collection<ValidationAction> validationList = ValidationFactory.getAllValidationActions(null).values();
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

      for (ValidationAction valAction : validationList)
      {
         ValidationInfo actionInfo = valAction.getValidationInfo();

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
   public Map<DocumentId, Set<TransUnit>> runValidations(Collection<HDocument> hDocs, List<ValidationId> validationIds, Long localeId)
   {
      Map<DocumentId, Set<TransUnit>> docValidationResult = new HashMap<DocumentId, Set<TransUnit>>();

      for (HDocument hDoc : hDocs)
      {
         Set<TransUnit> errorList = new HashSet<TransUnit>();

         for(HTextFlow textFlow: hDoc.getTextFlows())
         {
            HTextFlowTarget target = textFlow.getTargets().get(localeId);
            if (target != null)
            {
               for (ValidationId validationId : validationIds)
               {
                  ValidationAction validation = ValidationFactory.getValidationAction(validationId);

                  validation.validate(textFlow.getContents().get(0), target.getContents().get(0));
                  if (validation.hasError())
                  {
                     errorList.add(transUnitTransformer.transform(textFlow, target));
                  }
               }
            }
         }
         if (!errorList.isEmpty())
         {
            docValidationResult.put(new DocumentId(hDoc.getId(), hDoc.getDocId()), errorList);
         }
      }
      return docValidationResult;
   }
}
