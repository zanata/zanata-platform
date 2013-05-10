package org.zanata.webtrans.shared.rpc;

import java.util.List;

public class GetDocumentList extends AbstractWorkspaceAction<GetDocumentListResult>
{
   private static final long serialVersionUID = 1L;
   
   private List<String> docIdFilters;

   public GetDocumentList()
   {
   }

   public GetDocumentList(List<String> docIdFilters)
   {
      this.docIdFilters = docIdFilters;
   }

   public List<String> getDocIdFilters()
   {
      return docIdFilters;
   }
}
