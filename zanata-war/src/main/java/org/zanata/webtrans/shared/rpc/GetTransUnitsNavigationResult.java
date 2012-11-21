package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.Map;

import org.zanata.common.ContentState;
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

}
