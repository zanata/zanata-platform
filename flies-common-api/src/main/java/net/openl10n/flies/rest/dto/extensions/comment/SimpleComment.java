package net.openl10n.flies.rest.dto.extensions.comment;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowExtension;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowTargetExtension;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Holds source/target comments for a Java Properties item, extracted comment
 * for a source gettext message, or translator comment for a translated gettext
 * message.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 * @param <T>
 */
@XmlType(name = "simpleCommentExtension", propOrder = {})
@XmlRootElement(name = "comment")
@JsonTypeName(value = "comment")
public class SimpleComment implements TextFlowExtension, TextFlowTargetExtension
{

   public static final String ID = "comment";
   
   private String value;

   public SimpleComment()
   {
   }

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
