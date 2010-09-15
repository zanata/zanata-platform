package net.openl10n.flies.rest.dto.deprecated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;

@XmlRootElement(name = "documents", namespace = Namespaces.FLIES)
@XmlType(name = "documentsType", namespace = Namespaces.FLIES)
@Deprecated
public class Documents
{

   private List<Document> documents;

   @XmlElement(name = "document", namespace = Namespaces.FLIES)
   public List<Document> getDocuments()
   {
      if (documents == null)
         documents = new ArrayList<Document>();
      return documents;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
