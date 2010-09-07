package net.openl10n.flies.webtrans.server.rpc;

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.TransUnitCount;
import net.openl10n.flies.model.StatusCount;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
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

   @Override
   public GetStatusCountResult execute(GetStatusCount action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      @SuppressWarnings("unchecked")
      List<StatusCount> stats = session.createQuery("select new net.openl10n.flies.model.StatusCount(tft.state, count(tft)) " + "from HTextFlowTarget tft where tft.textFlow.document.id = :id " + "  and tft.locale.localeId = :locale " + "group by tft.state").setParameter("id", action.getDocumentId().getValue()).setParameter("locale", action.getWorkspaceId().getLocaleId()).list();

      Long totalCount = (Long) session.createQuery("select count(tf) from HTextFlow tf where tf.document.id = :id").setParameter("id", action.getDocumentId().getValue()).uniqueResult();

      TransUnitCount stat = new TransUnitCount();
      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }

      stat.set(ContentState.New, totalCount.intValue() - stat.get(ContentState.Approved) - stat.get(ContentState.NeedReview));
      @SuppressWarnings("unused")
      TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getWorkspaceId());

      return new GetStatusCountResult(action.getDocumentId(), stat);

   }

   @Override
   public void rollback(GetStatusCount action, GetStatusCountResult result, ExecutionContext context) throws ActionException
   {
   }
}
