package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

@Name("webtrans.gwt.GetTransMemoryDetailsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransMemoryDetailsAction.class)
@Slf4j
public class GetTransMemoryDetailsHandler
        extends
        AbstractActionHandler<GetTransMemoryDetailsAction, TransMemoryDetailsList> {
    @In
    private TextFlowDAO textFlowDAO;

    @In
    private LocaleService localeServiceImpl;

    @In
    private ZanataIdentity identity;

    @In
    private TranslationMemoryService translationMemoryServiceImpl;

    @Override
    public TransMemoryDetailsList execute(GetTransMemoryDetailsAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        LocaleId locale = action.getWorkspaceId().getLocaleId();
        HLocale hLocale;
        try {
            hLocale =
                    localeServiceImpl
                            .validateLocaleByProjectIteration(locale, action
                                    .getWorkspaceId().getProjectIterationId()
                                    .getProjectSlug(), action.getWorkspaceId()
                                    .getProjectIterationId().getIterationSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }

        ArrayList<Long> textFlowIds = action.getTransUnitIdList();
        log.info("Fetching TM details for TFs {} in locale {}", textFlowIds,
                locale);
        List<HTextFlow> textFlows = textFlowDAO.findByIdList(textFlowIds);
        ArrayList<TransMemoryDetails> items =
                new ArrayList<TransMemoryDetails>(textFlows.size());

        for (HTextFlow tf : textFlows) {
            TransMemoryDetails memoryDetails =
                    translationMemoryServiceImpl.getTransMemoryDetail(hLocale,
                            tf);
            items.add(memoryDetails);
        }

        log.info("Returning {} TM details", items.size());
        return new TransMemoryDetailsList(items);
    }

    @Override
    public void rollback(GetTransMemoryDetailsAction action,
            TransMemoryDetailsList result, ExecutionContext context)
            throws ActionException {
    }
}
