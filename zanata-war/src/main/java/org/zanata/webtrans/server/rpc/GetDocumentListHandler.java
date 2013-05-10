package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationStateCache;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

@Name("webtrans.gwt.GetDocsListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDocumentList.class)
public class GetDocumentListHandler extends AbstractActionHandler<GetDocumentList, GetDocumentListResult>
{
   @In
   private ZanataIdentity identity;

   @In
   private DocumentDAO documentDAO;

   @In
   private TranslationStateCache translationStateCacheImpl;

   @In
   private TranslationFileService translationFileServiceImpl;
   
   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @Override
   public GetDocumentListResult execute(GetDocumentList action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();

      ProjectIterationId iterationId = action.getWorkspaceId().getProjectIterationId();

      List<DocumentInfo> docs = new ArrayList<DocumentInfo>();

      List<HDocument> hDocs = getDocumentList(action);

      for (HDocument hDoc : hDocs)
      {
         HPerson person = hDoc.getLastModifiedBy();
         String lastModifiedBy = "";
         if (person != null)
         {
            lastModifiedBy = person.getAccount().getUsername();
         }

         Map<String, String> downloadExtensions = new HashMap<String, String>();

         HProjectIteration projectIteration = hDoc.getProjectIteration();
         ProjectType type = projectIteration.getProjectType();
         if (type == null)
         {
            type = projectIteration.getProject().getDefaultProjectType();
         }

         if (type == null)
         {
            // no .po download link
         }
         else if (type == ProjectType.Gettext || type == ProjectType.Podir)
         {
            downloadExtensions.put(".po", "po?docId=" + hDoc.getDocId());
         }
         else
         {
            downloadExtensions.put("offline .po", "offlinepo?docId=" + hDoc.getDocId());
         }

         if (translationFileServiceImpl.hasPersistedDocument(iterationId.getProjectSlug(), iterationId.getIterationSlug(), hDoc.getPath(), hDoc.getName()))
         {
            String extension = "." + translationFileServiceImpl.getFileExtension(iterationId.getProjectSlug(), iterationId.getIterationSlug(), hDoc.getPath(), hDoc.getName());
            downloadExtensions.put(extension, "baked?docId=" + hDoc.getDocId());
         }

         DocumentInfo doc = new DocumentInfo(new DocumentId(hDoc.getId(), hDoc.getDocId()), hDoc.getName(), hDoc.getPath(), hDoc.getLocale().getLocaleId(), null, new AuditInfo(hDoc.getLastChanged(), lastModifiedBy), downloadExtensions, null);
         docs.add(doc);
      }
      return new GetDocumentListResult(iterationId, docs);
   }

   @Override
   public void rollback(GetDocumentList action, GetDocumentListResult result, ExecutionContext context) throws ActionException
   {
   }
   
   private List<HDocument> getDocumentList(GetDocumentList action)
   {
      ProjectIterationId iterationId = action.getWorkspaceId().getProjectIterationId();

      if (hasDocIdFilters(action))
      {
         return documentDAO.getByProjectIterationAndDocIdList(iterationId.getProjectSlug(), iterationId.getIterationSlug(), action.getDocIdFilters());
      }
      else
      {
         return documentDAO.getAllByProjectIteration(iterationId.getProjectSlug(), iterationId.getIterationSlug());
      }
   }

   private boolean hasDocIdFilters(GetDocumentList action)
   {
      return (action.getDocIdFilters() != null && !action.getDocIdFilters().isEmpty());
   }

}