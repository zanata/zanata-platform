package net.openl10n.flies.rest;

import net.openl10n.flies.common.LocaleId;

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
