package org.zanata.webtrans.server.rpc;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.RunDocValidationAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationResult;

@Named("webtrans.gwt.RunDocValidationHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(RunDocValidationAction.class)
public class RunDocValidationHandler extends
        AbstractActionHandler<RunDocValidationAction, RunDocValidationResult> {
    @Inject
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
