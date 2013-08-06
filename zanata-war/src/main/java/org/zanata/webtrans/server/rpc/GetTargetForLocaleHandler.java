package org.zanata.webtrans.server.rpc;

import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TextFlowTarget;
import org.zanata.webtrans.shared.model.TextFlowTargetId;
import org.zanata.webtrans.shared.rpc.GetTargetForLocale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocaleResult;

@Name("webtrans.gwt.GetTargetForLocaleHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTargetForLocale.class)
@Slf4j
public class GetTargetForLocaleHandler extends AbstractActionHandler<GetTargetForLocale, GetTargetForLocaleResult> {

    @In
    private ZanataIdentity identity;

    @In
    private TextFlowTargetDAO textFlowTargetDAO;

    @In
    private TextFlowDAO textFlowDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private ProjectDAO projectDAO;

    @In
    private LocaleDAO localeDAO;

    @In
    private TransUnitTransformer transUnitTransformer;

    private ProjectIterationId iterationId;

    private String projectSlug;

    @Override
    public GetTargetForLocaleResult execute(GetTargetForLocale action, ExecutionContext context) throws ActionException {
        try {
            identity.checkLoggedIn();

            iterationId = action.getWorkspaceId().getProjectIterationId();
            projectSlug = iterationId.getProjectSlug();

            HTextFlowTarget hTextFlowTarget = textFlowTargetDAO.getTextFlowTarget(action.getSourceTransUnitId().getId(), action.getLocale().getId().getLocaleId());
            if (hTextFlowTarget == null) {
                return new GetTargetForLocaleResult(null);
            } else {
                TextFlowTarget textFlowTarget = new TextFlowTarget(new TextFlowTargetId(hTextFlowTarget.getId()), action.getLocale(), hTextFlowTarget.getContents().get(0));
                return new GetTargetForLocaleResult(textFlowTarget);
            }
        } catch (Exception e) {
           log.error("Exception when fetching target: ", e);
           return new GetTargetForLocaleResult(null);
        }
    }

    @Override
    public void rollback(GetTargetForLocale action, GetTargetForLocaleResult result, ExecutionContext context) throws ActionException {
    }
}