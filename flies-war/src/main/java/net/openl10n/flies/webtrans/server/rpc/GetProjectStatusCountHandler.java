package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.TranslationStats;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.DocumentStatus;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCount;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCountResult;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetProjectStatusCountHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetProjectStatusCount.class)
public class GetProjectStatusCountHandler extends AbstractActionHandler<GetProjectStatusCount, GetProjectStatusCountResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   ProjectIterationDAO projectIterationDAO;
   @In
   DocumentDAO documentDAO;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public GetProjectStatusCountResult execute(GetProjectStatusCount action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      ProjectIterationId iterationId = action.getWorkspaceId().getProjectIterationId();
      log.info("Fetching Doc Status List for {0}", iterationId);
      ArrayList<DocumentStatus> docliststatus = new ArrayList<DocumentStatus>();
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(iterationId.getProjectSlug(), iterationId.getIterationSlug());

      Collection<HDocument> hDocs = hProjectIteration.getDocuments().values();
      for (HDocument hDoc : hDocs)
      {
         DocumentId docId = new DocumentId(hDoc.getId());

         TranslationStats stat = documentDAO.getStatistics(docId.getValue(), action.getWorkspaceId().getLocaleId());

         DocumentStatus docstatus = new DocumentStatus(docId, stat);
         docliststatus.add(docstatus);
      }

      // TranslationWorkspace workspace =
      // translationWorkspaceManager.getWorkspace(action.getWorkspaceId());

      log.info("Returning Doc Status List for {0}: {1} elements", iterationId, docliststatus.size());

      return new GetProjectStatusCountResult(docliststatus);

   }

   @Override
   public void rollback(GetProjectStatusCount action, GetProjectStatusCountResult result, ExecutionContext context) throws ActionException
   {
   }

}
