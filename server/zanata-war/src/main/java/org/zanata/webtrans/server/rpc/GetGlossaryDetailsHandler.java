package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.util.UrlUtil;
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
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetGlossaryDetailsHandler.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private GlossaryDAO glossaryDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private UrlUtil urlUtil;

    @Override
    public GetGlossaryDetailsResult execute(GetGlossaryDetailsAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        LocaleId locale = action.getWorkspaceId().getLocaleId();
        HLocale hLocale;
        try {
            ProjectIterationId projectIterationId =
                    action.getWorkspaceId().getProjectIterationId();
            hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale,
                    projectIterationId.getProjectSlug(),
                    projectIterationId.getIterationSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }
        List<Long> sourceIds = action.getSourceIdList();
        log.info("Fetching glossary details for entry{} in locale {}",
                sourceIds, hLocale);
        List<HGlossaryTerm> srcTerms = glossaryDAO.findTermByIdList(sourceIds);
        ArrayList<GlossaryDetails> items =
                new ArrayList<GlossaryDetails>(srcTerms.size());
        for (HGlossaryTerm srcTerm : srcTerms) {
            HGlossaryEntry entry = srcTerm.getGlossaryEntry();
            HGlossaryTerm hGlossaryTerm = entry.getGlossaryTerms().get(hLocale);
            String srcContent = srcTerm.getContent();
            String qualifiedName = entry.getGlossary().getQualifiedName();
            String url = urlUtil.glossaryUrl(qualifiedName, srcContent,
                    hLocale.getLocaleId());
            items.add(new GlossaryDetails(entry.getId(), srcContent,
                    hGlossaryTerm.getContent(), entry.getDescription(),
                    entry.getPos(), hGlossaryTerm.getComment(),
                    entry.getSourceRef(), entry.getSrcLocale().getLocaleId(),
                    hLocale.getLocaleId(), url, hGlossaryTerm.getVersionNum(),
                    hGlossaryTerm.getLastChanged()));
        }
        return new GetGlossaryDetailsResult(items);
    }

    @Override
    public void rollback(GetGlossaryDetailsAction action,
            GetGlossaryDetailsResult result, ExecutionContext context)
            throws ActionException {
    }
}
