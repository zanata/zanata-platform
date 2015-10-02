package org.zanata.webtrans.server.rpc;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.model.HLocale;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Named("webtrans.gwt.GetLocaleListHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(GetLocaleList.class)
public class GetLocaleListHandler extends
        AbstractActionHandler<GetLocaleList, GetLocaleListResult> {
    @Inject
    private ZanataIdentity identity;

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public GetLocaleListResult execute(GetLocaleList action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();

        ProjectIterationId iterationId =
                action.getWorkspaceId().getProjectIterationId();
        String projectSlug = iterationId.getProjectSlug();

        List<HLocale> hLocales =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, iterationId.getIterationSlug());

        List<Locale> locales = Lists.newArrayList();
        for (HLocale hLocale : hLocales) {
            Locale locale =
                    new Locale(new IdForLocale(hLocale.getId(),
                            hLocale.getLocaleId()),
                            hLocale.retrieveDisplayName());
            locales.add(locale);
        }
        return new GetLocaleListResult(locales);
    }

    @Override
    public void rollback(GetLocaleList action, GetLocaleListResult result,
            ExecutionContext context) throws ActionException {
    }
}
