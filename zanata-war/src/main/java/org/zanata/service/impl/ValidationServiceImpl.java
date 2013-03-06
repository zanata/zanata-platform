/**
 * 
 */
package org.zanata.service.impl;

import java.io.IOException;
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
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.GWTI18N;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.validation.ValidationFactory;
import org.zanata.webtrans.shared.validation.ValidationMessageResolver;

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

   private ValidationMessageResolver validationMessageResolverImpl;

   private ValidationFactory validationFactory;

   private static final String DESC_KEY = ".desc";


   private ValidationFactory getValidationFactory()
   {
      if (validationFactory == null)
      {
         validationFactory = new ValidationFactory(getMessageResolver());
      }
      return validationFactory;
   }

   public ValidationMessageResolver getMessageResolver()
   {
      try
      {
         validationMessageResolverImpl = new ValidationMessageResolverImpl(GWTI18N.create(ValidationMessages.class));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return validationMessageResolverImpl;
   }

   @Override
   public Collection<ValidationAction> getValidationAction(String projectSlug)
   {
      Collection<ValidationAction> validationList = getValidationFactory().getAllValidationActions().values();
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
      Collection<ValidationAction> validationList = null;

      if (!StringUtils.isEmpty(projectSlug) && !StringUtils.isEmpty(versionSlug))
      {
         HProjectIteration version = projectIterationDAO.getBySlug(projectSlug, versionSlug);

         validationList = getValidationObject(version);
      }

      return validationList;
   }

   @Override
   public Collection<ValidationAction> getValidationObject(HProjectIteration version)
   {
      Collection<ValidationAction> validationList = getValidationFactory().getAllValidationActions().values();

      Set<String> enabledValidations = new HashSet<String>();

      if (version != null)
      {
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
                  ValidationAction validation = getValidationFactory().getValidationAction(validationId);

                  validation.validate(textFlow.getContents().get(0), target.getContents().get(0));
                  if (validation.hasError())
                  {
                     System.out.println("==================" + validation.getError());
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
