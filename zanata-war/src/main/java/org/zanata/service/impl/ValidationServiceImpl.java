/**
 * 
 */
package org.zanata.service.impl;

import java.io.IOException;
import java.util.ArrayList;
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
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
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
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.DocValidationResultInfo;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import com.google.common.base.Stopwatch;

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
   @Logger
   private Log log;
   
   @In
   private ZanataMessages zanataMessages;
   
   @In
   private ProjectDAO projectDAO;

   @In
   private TransUnitTransformer transUnitTransformer;

   @In
   private ProjectIterationDAO projectIterationDAO;

   private ValidationFactory validationFactory;

   private static final String DESC_KEY = ".desc";


   private ValidationFactory getValidationFactory()
   {
      if (validationFactory == null)
      {
         ValidationMessages valMessages;
         try
         {
            valMessages = Gwti18nReader.create(ValidationMessages.class);
            validationFactory = new ValidationFactory(valMessages);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      return validationFactory;
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
   public Map<DocumentId, List<DocValidationResultInfo>> runValidations(Collection<HDocument> hDocs, List<ValidationId> validationIds, Long localeId)
   {
      log.info("Start docs validation");
      Stopwatch stopwatch = new Stopwatch().start();
      Map<DocumentId, List<DocValidationResultInfo>> docValidationResult = new HashMap<DocumentId, List<DocValidationResultInfo>>();
      List<ValidationAction> validationActions = getValidationFactory().getValidationActions(validationIds);

      Map<Long, DocValidationResultInfo> targetErrorList = new HashMap<Long, DocValidationResultInfo>();
      for (HDocument hDoc : hDocs)
      {
         for(HTextFlow textFlow: hDoc.getTextFlows())
         {
            HTextFlowTarget target = textFlow.getTargets().get(localeId);
            if (target != null)
            {
               for (ValidationAction validation : validationActions)
               {
                  validation.validate(textFlow.getContents().get(0), target.getContents().get(0));
                  if (validation.hasError())
                  {
                     if(targetErrorList.containsKey(target.getId()))
                     {
                        targetErrorList.get(target.getId()).getErrorMessages().addAll(validation.getError());
                     }
                     else
                     {
                        DocValidationResultInfo result = new DocValidationResultInfo(new TransUnitId(textFlow.getId()), validation.getError());
                        targetErrorList.put(target.getId(), result);
                     }
                  }
               }
            }
         }
         if (!targetErrorList.isEmpty())
         {
            docValidationResult.put(new DocumentId(hDoc.getId(), hDoc.getDocId()), new ArrayList<DocValidationResultInfo>(targetErrorList.values()));
         }
         targetErrorList.clear();
      }
      log.info("Finished docs validation in " + stopwatch);
      return docValidationResult;
   }
}
