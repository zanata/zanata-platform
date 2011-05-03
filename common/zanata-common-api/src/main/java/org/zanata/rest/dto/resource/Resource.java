package org.zanata.rest.dto.resource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.zanata.rest.dto.DTOUtil;

@XmlType(name = "resourceType", propOrder = { "textFlows" })
@XmlRootElement(name = "resource")
@JsonPropertyOrder( { "name", "contentType", "lang", "extensions", "textFlows" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class Resource extends AbstractResourceMeta
{

   private static final long serialVersionUID = 1L;
   private List<TextFlow> textFlows;

   public Resource()
   {
   }

   public Resource(String name)
   {
      super(name);
   }

   @XmlElementWrapper(name = "text-flows", required = false)
   @XmlElementRef
   public List<TextFlow> getTextFlows()
   {
      if (textFlows == null)
      {
         textFlows = new ArrayList<TextFlow>();
      }
      return textFlows;
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
      int result = super.hashCodeHelper();
      result = prime * result + ((textFlows == null) ? 0 : textFlows.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!(obj instanceof Resource))
      {
         return false;
      }
      Resource other = (Resource) obj;
      if (!super.equalsHelper(other))
      {
         return false;
      }
      if (textFlows == null)
      {
         if (other.textFlows != null)
         {
            return false;
         }
      }
      else if (!textFlows.equals(other.textFlows))
      {
         return false;
      }
      return true;
   }

}
