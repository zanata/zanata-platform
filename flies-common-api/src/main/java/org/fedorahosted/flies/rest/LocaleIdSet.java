package org.fedorahosted.flies.rest;

import org.fedorahosted.flies.common.LocaleId;

public class LocaleIdSet extends ElemSet<LocaleId>
{

   public LocaleIdSet(String values)
   {
      super(values);
   }

   @Override
   protected LocaleId valueOfElem(String value)
   {
      return new LocaleId(value);
   }

}
