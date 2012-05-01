package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

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

      ZanataIdentity.instance().checkLoggedIn();

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
