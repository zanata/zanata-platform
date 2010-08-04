package org.fedorahosted.flies.rest.dto.resource;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.DTOUtil;

@XmlRootElement(name = "extension-set", namespace = Namespaces.FLIES)
public class ExtensionSet extends HashSet<Extension>
{

   private static final long serialVersionUID = 8077674295531213159L;

   public Extension findById(String id)
   {
      for (Extension e : this)
      {
         if (e.getId().equals(id))
            return e;
      }
      return null;
   }

   public <T extends Extension> T findByType(Class<T> clz)
   {
      for (Extension e : this)
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
