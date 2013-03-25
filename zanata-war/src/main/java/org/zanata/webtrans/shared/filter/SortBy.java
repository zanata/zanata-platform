package org.zanata.webtrans.shared.filter;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SortBy implements IsSerializable
{
   public final static SortBy SORT_BY_PATH_ASC = new SortBy("path", true);
   public static final SortBy SORT_BY_PATH_DESC = new SortBy("path", false);

   public String fieldName;
   private boolean asc = true;

   public SortBy()
   {
   }

   public SortBy(String fieldName, boolean asc)
   {
      this.fieldName = fieldName;
      this.asc = asc;
   }

   public String getFieldName()
   {
      return fieldName;
   }

   public boolean isAsc()
   {
      return asc;
   }
}