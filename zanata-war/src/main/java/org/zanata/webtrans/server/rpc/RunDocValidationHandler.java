package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HDocument;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.RunDocValidationAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationResult;

@Name("webtrans.gwt.RunDocValidationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RunDocValidationAction.class)
public class RunDocValidationHandler extends AbstractActionHandler<RunDocValidationAction, RunDocValidationResult>
{
   @In
   private ValidationService validationServiceImpl;

   @In
   private DocumentDAO documentDAO;
   
   @In
   private LocaleDAO localeDAO;

   @Override
   public RunDocValidationResult execute(RunDocValidationAction action, ExecutionContext context) throws ActionException
   {
      HDocument hDoc = documentDAO.findById(action.getDocId().getId(), false);
      
      boolean hasError = validationServiceImpl.runValidations(hDoc, action.getValidationIds(), action.getWorkspaceId().getLocaleId());
      return new RunDocValidationResult(action.getDocId(), hasError, action.getWorkspaceId().getLocaleId());
   }

   @Override
   public void rollback(RunDocValidationAction action, RunDocValidationResult result, ExecutionContext context) throws ActionException
   {
   }

}