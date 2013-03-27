package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationStateCache;
import org.zanata.webtrans.server.ActionHandlerFor;
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

   @Override
   public GetDocumentListResult execute(GetDocumentList action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();

      LocaleId localeId = action.getWorkspaceId().getLocaleId();
      ProjectIterationId iterationId = action.getWorkspaceId().getProjectIterationId();

      List<DocumentInfo> docs = new ArrayList<DocumentInfo>();

      List<HDocument> hDocs = getDocumentList(action);

      for (HDocument hDoc : hDocs)
      {
         DocumentId docId = new DocumentId(hDoc.getId(), hDoc.getDocId());
         TranslationStats stats = documentDAO.getStatistics(hDoc.getId(), localeId);
         HTextFlowTarget result = translationStateCacheImpl.getDocLastModifiedTextFlowTarget(hDoc.getId(), localeId);

         Date lastTranslatedDate = null;
         String lastTranslatedBy = "";

         if (result != null)
         {
            lastTranslatedDate = result.getLastChanged();
            if (result.getLastModifiedBy() != null)
            {
               lastTranslatedBy = result.getLastModifiedBy().getAccount().getUsername();
            }
         }

         HPerson person = hDoc.getLastModifiedBy();
         String lastModifiedBy = "";
         if (person != null)
         {
            lastModifiedBy = person.getAccount().getUsername();
         }

         Map<String, String> downloadExtensions = new HashMap<String, String>();

            ProjectType type = hDoc.getProjectIteration().getProjectType();
            if (type == ProjectType.Gettext || type == ProjectType.Podir)
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

            DocumentInfo doc = new DocumentInfo(docId, hDoc.getName(), hDoc.getPath(), hDoc.getLocale().getLocaleId(), stats, lastModifiedBy, hDoc.getLastChanged(), downloadExtensions, lastTranslatedBy, lastTranslatedDate);
            docs.add(doc);
         }
      }
      return new GetDocumentListResult(iterationId, docs);
   }

   @Override
   public void rollback(GetDocumentList action, GetDocumentListResult result, ExecutionContext context) throws ActionException
   {
   }

}