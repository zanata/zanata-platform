package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCount;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCountResult;

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

      ZanataIdentity.instance().checkLoggedIn();

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
