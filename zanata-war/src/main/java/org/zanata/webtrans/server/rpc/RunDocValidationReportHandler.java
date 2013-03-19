package org.zanata.webtrans.server.rpc;

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;
import org.zanata.webtrans.shared.rpc.RunDocValidationReportAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationReportResult;

@Name("webtrans.gwt.RunDocValidationReportHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RunDocValidationReportAction.class)
public class RunDocValidationReportHandler extends AbstractActionHandler<RunDocValidationReportAction, RunDocValidationReportResult>
{
   @In
   private ValidationService validationServiceImpl;

   @In
   private DocumentDAO documentDAO;

   @Override
   public RunDocValidationReportResult execute(RunDocValidationReportAction action, ExecutionContext context) throws ActionException
   {
      HDocument hDoc = documentDAO.findById(action.getDocumentId(), false);

      List<TransUnitValidationResult> result = validationServiceImpl.runValidationsFullReport(hDoc, action.getValidationIds(), action.getWorkspaceId().getLocaleId());
      return new RunDocValidationReportResult(result, action.getWorkspaceId().getLocaleId());
   }

   @Override
   public void rollback(RunDocValidationReportAction action, RunDocValidationReportResult result, ExecutionContext context) throws ActionException
   {
   }

}