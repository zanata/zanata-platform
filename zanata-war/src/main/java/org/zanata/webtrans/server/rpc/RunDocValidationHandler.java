package org.zanata.webtrans.server.rpc;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.RunDocValidationAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationResult;

@Name("webtrans.gwt.RunDocValidationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RunDocValidationAction.class)
public class RunDocValidationHandler extends
        AbstractActionHandler<RunDocValidationAction, RunDocValidationResult> {
    @In
    private ValidationService validationServiceImpl;

    @Override
    public RunDocValidationResult execute(RunDocValidationAction action,
            ExecutionContext context) throws ActionException {
        Map<DocumentId, Boolean> result = new HashMap<DocumentId, Boolean>();

        for (DocumentId documentId : action.getDocIds()) {
            boolean hasError =
                    validationServiceImpl.runDocValidations(documentId.getId(),
                            action.getValidationIds(), action.getWorkspaceId()
                                    .getLocaleId());
            result.put(documentId, hasError);
        }

        return new RunDocValidationResult(result, action.getWorkspaceId()
                .getLocaleId());
    }

    @Override
    public void rollback(RunDocValidationAction action,
            RunDocValidationResult result, ExecutionContext context)
            throws ActionException {
    }

}
