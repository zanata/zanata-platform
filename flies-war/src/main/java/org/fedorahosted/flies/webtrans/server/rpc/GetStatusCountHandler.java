package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.model.StatusCount;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.rpc.GetStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetStatusCountResult;
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

      List<StatusCount> stats = session.createQuery("select new org.fedorahosted.flies.model.StatusCount(tft.state, count(tft)) " + "from HTextFlowTarget tft where tft.textFlow.document.id = :id " + "  and tft.locale = :locale " + "group by tft.state").setParameter("id", action.getDocumentId().getValue()).setParameter("locale", action.getWorkspaceId().getLocaleId()).list();

      Long totalCount = (Long) session.createQuery("select count(tf) from HTextFlow tf where tf.document.id = :id").setParameter("id", action.getDocumentId().getValue()).uniqueResult();

      TransUnitCount stat = new TransUnitCount();
      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }

      stat.set(ContentState.New, totalCount.intValue() - stat.get(ContentState.Approved) - stat.get(ContentState.NeedReview));
      TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getWorkspaceId());

      return new GetStatusCountResult(action.getDocumentId(), stat);

   }

   @Override
   public void rollback(GetStatusCount action, GetStatusCountResult result, ExecutionContext context) throws ActionException
   {
   }
}
