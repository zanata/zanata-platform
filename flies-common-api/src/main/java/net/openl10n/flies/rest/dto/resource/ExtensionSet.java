package net.openl10n.flies.rest.dto.resource;

import java.util.HashSet;


import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.ExtensionValue;


public class ExtensionSet<T extends ExtensionValue> extends HashSet<T>
{

   private static final long serialVersionUID = 1L;

   public <E extends T> E findByType(Class<E> clz)
   {
      for (T e : this)
      {
         if (clz.isInstance(e))
            return (E) clz.cast(e);
      }
      return null;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
