package org.zanata.webtrans.shared.rpc;

import java.util.List;


public class GetDocumentList extends AbstractWorkspaceAction<GetDocumentListResult>
{

   private static final long serialVersionUID = 1L;

   private List<String> filters;

   public GetDocumentList()
   {
   }

   public GetDocumentList(List<String> filters)
   {
      this.filters = filters;
   }

   public List<String> getFilters()
   {
      return filters;
   }

   public void setFilters(List<String> filters)
   {
      this.filters = filters;
   }

}
