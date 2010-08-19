package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.DocumentInfo;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;
import org.fedorahosted.flies.webtrans.shared.rpc.GetDocumentList;
import org.fedorahosted.flies.webtrans.shared.rpc.GetDocumentListResult;
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