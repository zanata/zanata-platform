package net.openl10n.flies.rest.dto.resource;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;

import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.ExtensionValue;

@XmlRootElement(name = "extension-set")
public class ExtensionSet<T> extends HashSet<ExtensionValue<T>> 
{

   private static final long serialVersionUID = 8077674295531213159L;

   public <Y extends ExtensionValue<T>> Y findByType(Class<Y> clz)
   {
      for (ExtensionValue<T> e : this)
      {
         if (clz.isInstance(e))
            return clz.cast(e);
      }
      return null;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
