package org.zanata.webtrans.shared.filter;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchFilter implements IsSerializable
{
   public String phase;
   private boolean caseSensetive;
   private boolean exact;

   public SearchFilter()
   {
   }

   public SearchFilter(String phase, boolean caseSensetive, boolean exact)
   {
      this.phase = phase;
      this.caseSensetive = caseSensetive;
      this.exact = exact;
   }

   public String getPhase()
   {
      return phase;
   }

   public boolean isCaseSensetive()
   {
      return caseSensetive;
   }

   public boolean isExact()
   {
      return exact;
   }
}