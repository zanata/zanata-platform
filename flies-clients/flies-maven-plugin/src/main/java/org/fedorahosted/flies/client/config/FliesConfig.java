package org.fedorahosted.flies.client.config;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

/**
 * Representation of the root node of Flies configuration
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
@XmlType(name = "configType")
@XmlRootElement(name = "config")
public class FliesConfig implements Serializable
{

   private List<DocSet> docSets = new ArrayList<DocSet>();

   private URL url;

   public FliesConfig()
   {
   }


   @XmlElement(name = "docset", namespace = Namespaces.FLIES_CONFIG)
   public List<DocSet> getDocSets()
   {
      return docSets;
   }

   public void setDocSets(List<DocSet> docSets)
   {
      this.docSets = docSets;
   }

   @XmlAttribute
   public URL getUrl()
   {
      return url;
   }

   public void setUrl(URL flies)
   {
      this.url = flies;
   }

}
