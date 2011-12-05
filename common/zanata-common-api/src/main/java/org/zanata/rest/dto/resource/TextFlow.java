package org.zanata.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.Extensible;
import org.zanata.rest.dto.LocaleIdAdapter;
import org.zanata.rest.dto.extensions.gettext.TextFlowExtension;

@XmlType(name = "textFlowType", propOrder = { "content", "extensions" })
@JsonPropertyOrder( { "id", "lang", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TextFlow implements Extensible<TextFlowExtension>, Serializable
{
   private static final long serialVersionUID = 1L;

   @NotEmpty
   @Length(max = 255)
   private String id;

   @NotNull
   private LocaleId lang;

   @NotNull
   private String content;

   private ExtensionSet<TextFlowExtension> extensions;

   private Integer revision;

   /**
    * This constructor sets the lang value to en-US
    * 
    */

   public TextFlow()
   {
      this(null, null, null);
   }

   /**
    * This constructor sets the lang value to en-US
    * 
    */

   public TextFlow(String id)
   {
      this(id, LocaleId.EN_US, null);
   }

   /**
    * 
    * @param id Resource Id value
    * @param lang Locale value
    */

   public TextFlow(String id, LocaleId lang)
   {
      this(id, lang, null);
   }

   public TextFlow(String id, LocaleId lang, String content)
   {
      this.id = id;
      this.lang = lang;
      this.content = content;
   }

   @XmlAttribute(name = "id", required = true)
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   @XmlAttribute(name = "lang", namespace = Namespaces.XML, required = false)
   public LocaleId getLang()
   {
      return lang;
   }

   public void setLang(LocaleId lang)
   {
      this.lang = lang;
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

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement(name = "extension")
   public ExtensionSet<TextFlowExtension> getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet<TextFlowExtension> extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet<TextFlowExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TextFlowExtension>();
      return extensions;
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
      result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((lang == null) ? 0 : lang.hashCode());
      result = prime * result + ((revision == null) ? 0 : revision.hashCode());
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
      if (!(obj instanceof TextFlow))
      {
         return false;
      }
      TextFlow other = (TextFlow) obj;
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
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (lang == null)
      {
         if (other.lang != null)
         {
            return false;
         }
      }
      else if (!lang.equals(other.lang))
      {
         return false;
      }
      if (revision == null)
      {
         if (other.revision != null)
         {
            return false;
         }
      }
      else if (!revision.equals(other.revision))
      {
         return false;
      }
      return true;
   }

   @XmlAttribute()
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer i)
   {
      revision = i;
   }
}
