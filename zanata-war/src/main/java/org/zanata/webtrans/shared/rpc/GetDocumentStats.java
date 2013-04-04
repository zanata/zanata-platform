package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.DocumentId;

public class GetDocumentStats extends AbstractWorkspaceAction<GetDocumentStatsResult>
{
   private static final long serialVersionUID = 1L;
   
   private List<DocumentId> docIds;

   private GetDocumentStats()
   {
   }

   public GetDocumentStats(List<DocumentId> docIds)
   {
      this.docIds = docIds;
   }

   public List<DocumentId> getDocIds()
   {
      return docIds;
   }
}
