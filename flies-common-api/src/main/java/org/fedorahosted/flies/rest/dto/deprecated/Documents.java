package org.fedorahosted.flies.rest.dto.deprecated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlRootElement(name = "documents", namespace = Namespaces.FLIES)
@XmlType(name = "documentsType", namespace = Namespaces.FLIES)
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
      return Utility.toXML(this);
   }

}
