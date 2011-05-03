package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.Person;

import net.customware.gwt.dispatch.shared.Result;


public class GetTranslatorListResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ArrayList<Person> translatorlist;

   @SuppressWarnings("unused")
   private GetTranslatorListResult()
   {
   }

   public GetTranslatorListResult(ArrayList<Person> translatorlist)
   {
      this.translatorlist = translatorlist;
   }

   public ArrayList<Person> getTranslatorList()
   {
      return translatorlist;
   }
}
