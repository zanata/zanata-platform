package net.openl10n.flies.rest.dto.extensions;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.resource.Extension;

import org.codehaus.jackson.annotate.JsonTypeName;

@XmlType(name = "simpleCommentExtension", namespace = PoHeader.NAMESPACE, propOrder = {})
@XmlRootElement(name = "comment", namespace = PoHeader.NAMESPACE)
@JsonTypeName(value = "comment")
public class SimpleComment<T extends Commentable> implements ExtensionValue<T>
{

   public static final String ID = "comment";
   
   private String value;

   public SimpleComment(String value)
   {
      this.value = value;
   }

   @XmlElement(name = "value", required = true)
   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   @XmlAttribute(name = "space", namespace = Namespaces.XML)
   public String getSpace()
   {
      return "preserve";
   }

   public void setSpace(String space)
   {
   }

}
