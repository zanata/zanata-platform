package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

public class TransMemoryDetailsList implements Result
{

   private ArrayList<TransMemoryDetails> items;

   @SuppressWarnings("unused")
   private TransMemoryDetailsList()
   {
      this(null);
   }

   public TransMemoryDetailsList(ArrayList<TransMemoryDetails> items)
   {
      this.items = items;
   }

   public ArrayList<TransMemoryDetails> getItems()
   {
      return items;
   }

}
