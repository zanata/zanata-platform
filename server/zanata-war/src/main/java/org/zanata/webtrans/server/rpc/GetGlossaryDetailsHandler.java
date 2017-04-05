package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.exception.ZanataServiceException;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossarySearchService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Named("webtrans.gwt.GetGlossaryDetailsHandler")
@RequestScoped
@ActionHandlerFor(GetGlossaryDetailsAction.class)
public class GetGlossaryDetailsHandler extends
        AbstractActionHandler<GetGlossaryDetailsAction, GetGlossaryDetailsResult> {

    @Inject
    private ZanataIdentity identity;
    @Inject
    private GlossarySearchService glossarySearchService;

    @Override
    public GetGlossaryDetailsResult execute(GetGlossaryDetailsAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        ArrayList<GlossaryDetails> items;
        try {
            items = glossarySearchService.lookupDetails(
                    action.getWorkspaceId().getLocaleId(),
                    action.getSourceIdList());
        } catch (ZanataServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }

        return new GetGlossaryDetailsResult(items);
    }

    @Override
    public void rollback(GetGlossaryDetailsAction action,
            GetGlossaryDetailsResult result, ExecutionContext context)
            throws ActionException {
    }
}
