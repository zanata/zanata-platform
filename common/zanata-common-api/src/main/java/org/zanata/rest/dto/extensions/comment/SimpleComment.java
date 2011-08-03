package org.zanata.rest.dto.extensions.comment;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.zanata.common.Namespaces;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.extensions.gettext.TextFlowExtension;
import org.zanata.rest.dto.extensions.gettext.TextFlowTargetExtension;

/**
 * Holds source/target comments for a Java Properties item, extracted comment
 * for a source gettext message, or translator comment for a translated gettext
 * message.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
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

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof SimpleComment))
      {
         return false;
      }
      SimpleComment other = (SimpleComment) obj;
      if (value == null)
      {
         if (other.value != null)
         {
            return false;
         }
      }
      else if (!value.equals(other.value))
      {
         return false;
      }
      return true;
   }

}
