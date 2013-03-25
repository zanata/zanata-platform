package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.filter.SearchFilter;
import org.zanata.webtrans.shared.filter.SortBy;

public class GetDocumentList extends AbstractWorkspaceAction<GetDocumentListResult>
{
   private static final long serialVersionUID = 1L;
   
   private List<String> docIdFilters;

   private SearchFilter searchFilter;
   
   private int offset;
   
   private int count;
   
   private SortBy sortBy;

   public GetDocumentList()
   {
   }

   public GetDocumentList(List<String> docIdFilters, int offset, int count, SortBy sortBy, SearchFilter searchFilter)
   {
      this.docIdFilters = docIdFilters;
      this.searchFilter = searchFilter;
      this.offset = offset;
      this.count = count;
      this.sortBy = sortBy;
   }

   public List<String> getDocIdFilters()
   {
      return docIdFilters;
   }

   public SearchFilter getSearchFilter()
   {
      return searchFilter;
   }

   public int getOffset()
   {
      return offset;
   }

   public int getCount()
   {
      return count;
   }

   public SortBy getSortBy()
   {
      return sortBy;
   }
}
