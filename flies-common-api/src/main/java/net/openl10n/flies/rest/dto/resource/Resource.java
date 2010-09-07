package net.openl10n.flies.rest.dto.resource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@XmlType(name = "resourceType", namespace = Namespaces.FLIES, propOrder = { "textFlows" })
@XmlRootElement(name = "resource", namespace = Namespaces.FLIES)
@JsonPropertyOrder( { "name", "contentType", "lang", "extensions", "textFlows" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class Resource extends AbstractResourceMeta
{

   private List<TextFlow> textFlows;

   public Resource()
   {
   }

   public Resource(String name)
   {
      super(name);
   }

   @XmlElementWrapper(name = "text-flows", namespace = Namespaces.FLIES, required = false)
   @XmlElementRef
   public List<TextFlow> getTextFlows()
   {
      if (textFlows == null)
      {
         textFlows = new ArrayList<TextFlow>();
      }
      return textFlows;
   }


}
