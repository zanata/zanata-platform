package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowTargetExtension;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.NotEmpty;

@XmlType(name = "textFlowTargetType", propOrder = { "description", "translator", "content", "extensions" })
@XmlRootElement(name = "text-flow-target")
@JsonPropertyOrder( { "resId", "state", "translator", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TextFlowTarget implements Serializable, Extensible<TextFlowTargetExtension>
{

   private static final long serialVersionUID = 1L;
   private String resId;
   private ContentState state = ContentState.New;
   private Person translator;
   private String content;
   private String description;
   private ExtensionSet<TextFlowTargetExtension> extensions;

   public TextFlowTarget()
   {
   }

   public TextFlowTarget(String resId)
   {
      this.resId = resId;
   }

   @XmlElementRef
   public Person getTranslator()
   {
      return translator;
   }

   public void setTranslator(Person translator)
   {
      this.translator = translator;
   }

   @XmlAttribute(name = "state", required = true)
   public ContentState getState()
   {
      return state;
   }

   public void setState(ContentState state)
   {
      this.state = state;
   }

   @XmlElement(name = "content", required = true)
   public String getContent()
   {
      if (content == null)
         return "";
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   /**
    * Optional descriptive text to identify the TextFlowTarget, eg an
    * abbreviated version of the source text being translated. This can be used
    * for a more readable XML serialisation.
    * 
    * @return
    */
   @XmlElement(name = "description", required = false)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement(name = "extension")
   public ExtensionSet<TextFlowTargetExtension> getExtensions()
   {
      return extensions;
   }

   @JsonIgnore
   public ExtensionSet<TextFlowTargetExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TextFlowTargetExtension>();
      return extensions;
   }

   public void setExtensions(ExtensionSet<TextFlowTargetExtension> extensions)
   {
      this.extensions = extensions;
   }

   @XmlAttribute(name = "res-id", required = true)
   @NotEmpty
   public String getResId()
   {
      return resId;
   }

   public void setResId(String resId)
   {
      this.resId = resId;
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
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
      result = prime * result + ((resId == null) ? 0 : resId.hashCode());
      result = prime * result + ((state == null) ? 0 : state.hashCode());
      result = prime * result + ((translator == null) ? 0 : translator.hashCode());
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
      if (!(obj instanceof TextFlowTarget))
      {
         return false;
      }
      TextFlowTarget other = (TextFlowTarget) obj;
      if (content == null)
      {
         if (other.content != null)
         {
            return false;
         }
      }
      else if (!content.equals(other.content))
      {
         return false;
      }
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
      {
         return false;
      }
      if (extensions == null)
      {
         if (other.extensions != null)
         {
            return false;
         }
      }
      else if (!extensions.equals(other.extensions))
      {
         return false;
      }
      if (resId == null)
      {
         if (other.resId != null)
         {
            return false;
         }
      }
      else if (!resId.equals(other.resId))
      {
         return false;
      }
      if (state != other.state)
      {
         return false;
      }
      if (translator == null)
      {
         if (other.translator != null)
         {
            return false;
         }
      }
      else if (!translator.equals(other.translator))
      {
         return false;
      }
      return true;
   }

}
