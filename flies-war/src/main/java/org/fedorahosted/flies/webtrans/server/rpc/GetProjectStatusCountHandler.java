package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.DocumentStatus;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCountResult;
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

         TransUnitCount stat = documentDAO.getStatistics(docId.getValue(), action.getWorkspaceId().getLocaleId());

         DocumentStatus docstatus = new DocumentStatus(docId, stat);
         docliststatus.add(docstatus);
      }

      TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getWorkspaceId());

      log.info("Returning Doc Status List for {0}: {1} elements", iterationId, docliststatus.size());

      return new GetProjectStatusCountResult(docliststatus);

   }

   @Override
   public void rollback(GetProjectStatusCount action, GetProjectStatusCountResult result, ExecutionContext context) throws ActionException
   {
   }

}
