package net.openl10n.flies.rest.dto.deprecated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;

@XmlRootElement(name = "simple-comments", namespace = Namespaces.FLIES)
@Deprecated
public class SimpleComments
{

   private List<SimpleComment> comments;

   @XmlElement(name = "comment", namespace = Namespaces.FLIES)
   public List<SimpleComment> getComments()
   {
      if (comments == null)
         comments = new ArrayList<SimpleComment>();
      return comments;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
