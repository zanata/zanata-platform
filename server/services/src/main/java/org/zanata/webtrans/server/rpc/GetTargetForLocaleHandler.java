package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TextFlowTarget;
import org.zanata.webtrans.shared.model.TextFlowTargetId;
import org.zanata.webtrans.shared.rpc.GetTargetForLocale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocaleResult;

@Named("webtrans.gwt.GetTargetForLocaleHandler")
@RequestScoped
@ActionHandlerFor(GetTargetForLocale.class)
public class GetTargetForLocaleHandler extends
        AbstractActionHandler<GetTargetForLocale, GetTargetForLocaleResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetTargetForLocaleHandler.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Override
    public GetTargetForLocaleResult execute(GetTargetForLocale action,
            ExecutionContext context) throws ActionException {
        try {
            identity.checkLoggedIn();
            HTextFlowTarget hTextFlowTarget = textFlowTargetDAO
                    .getTextFlowTarget(action.getSourceTransUnitId().getId(),
                            action.getLocale().getId().getLocaleId());
            if (hTextFlowTarget == null) {
                return new GetTargetForLocaleResult(null);
            } else {
                String displayName =
                        retrieveDisplayName(hTextFlowTarget.getLocale());
                TextFlowTarget textFlowTarget = new TextFlowTarget(
                        new TextFlowTargetId(hTextFlowTarget.getId()),
                        action.getLocale(),
                        hTextFlowTarget.getContents().get(0), displayName);
                return new GetTargetForLocaleResult(textFlowTarget);
            }
        } catch (Exception e) {
            log.error("Exception when fetching target: ", e);
            return new GetTargetForLocaleResult(null);
        }
    }

    public String retrieveDisplayName(HLocale hLocale) {
        return hLocale.retrieveDisplayName();
    }

    @Override
    public void rollback(GetTargetForLocale action,
            GetTargetForLocaleResult result, ExecutionContext context)
            throws ActionException {
    }
}
