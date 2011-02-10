package net.openl10n.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.TranslationStats;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.rpc.GetStatusCount;
import net.openl10n.flies.webtrans.shared.rpc.GetStatusCountResult;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetStatusCountHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetStatusCount.class)
public class GetStatusCountHandler extends AbstractActionHandler<GetStatusCount, GetStatusCountResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   DocumentDAO documentDAO;

   @Override
   public GetStatusCountResult execute(GetStatusCount action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      Long docId = action.getDocumentId().getValue();
      LocaleId localeId = action.getWorkspaceId().getLocaleId();

      TranslationStats transStats = documentDAO.getStatistics(docId, localeId);
      return new GetStatusCountResult(action.getDocumentId(), transStats);
   }

   @Override
   public void rollback(GetStatusCount action, GetStatusCountResult result, ExecutionContext context) throws ActionException
   {
   }
}
