package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.DocumentInfo;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;
import net.openl10n.flies.webtrans.shared.rpc.GetDocumentList;
import net.openl10n.flies.webtrans.shared.rpc.GetDocumentListResult;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetDocsListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDocumentList.class)
public class GetDocumentListHandler extends AbstractActionHandler<GetDocumentList, GetDocumentListResult>
{

   @Logger
   Log log;

   @In
   ProjectIterationDAO projectIterationDAO;

   @Override
   public GetDocumentListResult execute(GetDocumentList action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      ProjectIterationId iterationId = action.getProjectIterationId();
      ArrayList<DocumentInfo> docs = new ArrayList<DocumentInfo>();
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(iterationId.getProjectSlug(), iterationId.getIterationSlug());
      Collection<HDocument> hDocs = hProjectIteration.getDocuments().values();
      for (HDocument hDoc : hDocs)
      {
         DocumentId docId = new DocumentId(hDoc.getId());
         DocumentInfo doc = new DocumentInfo(docId, hDoc.getName(), hDoc.getPath());
         docs.add(doc);
      }
      return new GetDocumentListResult(iterationId, docs);
   }

   @Override
   public void rollback(GetDocumentList action, GetDocumentListResult result, ExecutionContext context) throws ActionException
   {
   }

}