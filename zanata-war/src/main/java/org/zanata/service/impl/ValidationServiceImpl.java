/**
 * 
 */
package org.zanata.service.impl;

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
import org.zanata.service.ValidationFactoryProvider;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
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
   private ProjectDAO projectDAO;

   @In
   private TransUnitTransformer transUnitTransformer;

   @In
   private ProjectIterationDAO projectIterationDAO;

   private ValidationFactory validationFactory;

   private ValidationFactory getValidationFactory()
   {
      if (validationFactory == null)
      {
         validationFactory = ValidationFactoryProvider.getFactoryInstance();
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
         if (enabledValidations.contains(valAction.getId().name()))
         {
            valAction.getValidationInfo().setEnabled(true);
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
         if (enabledValidations.contains(valAction.getId().name()))
         {
            valAction.getValidationInfo().setEnabled(true);
            valAction.getValidationInfo().setLocked(true);
         }
      }
      return validationList;
   }

   @Override
   public Map<DocumentId, List<TransUnitValidationResult>> runValidationsFullReport(List<HDocument> hDocs, List<ValidationId> validationIds, Long localeId)
   {
      log.info("Start full docs validation - {0}", hDocs.size());
      Stopwatch stopwatch = new Stopwatch().start();
      Map<DocumentId, List<TransUnitValidationResult>> docValidationResult = new HashMap<DocumentId, List<TransUnitValidationResult>>();
      List<ValidationAction> validationActions = getValidationFactory().getValidationActions(validationIds);

      Map<Long, TransUnitValidationResult> targetErrorList = new HashMap<Long, TransUnitValidationResult>();
      for (HDocument hDoc : hDocs)
      {
         for (HTextFlow textFlow : hDoc.getTextFlows())
         {
            HTextFlowTarget target = textFlow.getTargets().get(localeId);
            if (target != null)
            {
               for (ValidationAction validationAction : validationActions)
               {
                  validationAction.validate(textFlow.getContents().get(0), target.getContents().get(0));
                  if (validationAction.hasError())
                  {
                     if (targetErrorList.containsKey(target.getId()))
                     {
                        targetErrorList.get(target.getId()).getErrorMessages().addAll(validationAction.getError());
                     }
                     else
                     {
                        TransUnitValidationResult result = new TransUnitValidationResult(new TransUnitId(textFlow.getId()), validationAction.getError());
                        targetErrorList.put(target.getId(), result);
                     }
                  }
               }
            }
         }
         if (!targetErrorList.isEmpty())
         {
            List<TransUnitValidationResult> resultInfo = new ArrayList<TransUnitValidationResult>(targetErrorList.values());
            docValidationResult.put(new DocumentId(hDoc.getId(), hDoc.getDocId()), resultInfo);
         }
         targetErrorList.clear();
      }
      log.info("Finished full docs validation in " + stopwatch);
      return docValidationResult;
   }

   @Override
   public Map<DocumentId, Boolean> runValidations(List<HDocument> hDocs, List<ValidationId> validationIds, Long localeId)
   {
      log.info("Start {0} docs validation", hDocs.size());
      Stopwatch stopwatch = new Stopwatch().start();
      Map<DocumentId, Boolean> validationResult = new HashMap<DocumentId, Boolean>();
      List<ValidationAction> validationActions = getValidationFactory().getValidationActions(validationIds);

      for (HDocument hDoc : hDocs)
      {
         boolean hasValidationError = documentHasError(hDoc, validationActions, localeId);
         validationResult.put(new DocumentId(hDoc.getId(), hDoc.getDocId()), hasValidationError);
      }

      log.info("Finished docs validation in " + stopwatch);
      return validationResult;
   }

   private boolean documentHasError(HDocument hDoc, List<ValidationAction> validationActions, Long localeId)
   {
      for (HTextFlow textFlow : hDoc.getTextFlows())
      {
         if (textFlowTargetHasError(textFlow, validationActions, localeId))
         {
            // return true if error found, else continue
            return true;
         }
      }
      return false;
   }

   @Override
   public List<HTextFlow> filterHasErrorTexFlow(List<HTextFlow> textFlows, List<ValidationId> validationIds, Long localeId)
   {
      log.info("Start filter {0} textFlows", textFlows.size());
      Stopwatch stopwatch = new Stopwatch().start();

      List<ValidationAction> validationActions = getValidationFactory().getValidationActions(validationIds);
      List<HTextFlow> result = new ArrayList<HTextFlow>();
      
      for (HTextFlow textFlow : textFlows)
      {
         if(textFlowTargetHasError(textFlow, validationActions, localeId))
         {
            result.add(textFlow);
         }
      }
      log.info("Finished filter textFlows in " + stopwatch);
      return result;
   }

   private boolean textFlowTargetHasError(HTextFlow textFlow, List<ValidationAction> validationActions, Long localeId)
   {
      HTextFlowTarget target = textFlow.getTargets().get(localeId);
      if (target != null)
      {
         for (ValidationAction validationAction : validationActions)
         {
            validationAction.validate(textFlow.getContents().get(0), target.getContents().get(0));
            if (validationAction.hasError())
            {
               return true;
            }
         }
      }
      return false;
   }
}
