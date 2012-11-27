package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.Map;

import org.zanata.common.ContentState;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;


public class GetTransUnitsNavigationResult implements IsSerializable
{

   private ArrayList<Long> idIndexList;
   private Map<Long, ContentState> transIdStateList;


   @SuppressWarnings("unused")
   private GetTransUnitsNavigationResult()
   {
   }

   public GetTransUnitsNavigationResult(ArrayList<Long> idIndexList, Map<Long, ContentState> transIdStateList)
   {
      this.idIndexList = idIndexList;
      this.transIdStateList = transIdStateList;
   }

   public ArrayList<Long> getIdIndexList()
   {
      return idIndexList;
   }

   public Map<Long, ContentState> getTransIdStateList()
   {
      return transIdStateList;
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("idIndexList", idIndexList).
            add("transIdStateList", transIdStateList).
            toString();
   }
}
