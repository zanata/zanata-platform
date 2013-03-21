package org.zanata.webtrans.server.rpc;

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;
import org.zanata.webtrans.shared.rpc.RunDocValidationReportAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationReportResult;


@Name("webtrans.gwt.RunDocValidationReportHandler")
@ActionHandlerFor(RunDocValidationReportAction.class)
@Scope(ScopeType.STATELESS)
public class RunDocValidationReportHandler extends AbstractActionHandler<RunDocValidationReportAction, RunDocValidationReportResult>
{
   @In
   private ValidationService validationServiceImpl;

   @Override
   public RunDocValidationReportResult execute(RunDocValidationReportAction action, ExecutionContext context) throws ActionException
   {
      List<TransUnitValidationResult> result = validationServiceImpl.runValidationsFullReport(action.getDocumentId(), action.getValidationIds(), action.getWorkspaceId().getLocaleId());
      return new RunDocValidationReportResult(result, action.getWorkspaceId().getLocaleId());
   }

   @Override
   public void rollback(RunDocValidationReportAction action, RunDocValidationReportResult result, ExecutionContext context) throws ActionException
   {
   }

}