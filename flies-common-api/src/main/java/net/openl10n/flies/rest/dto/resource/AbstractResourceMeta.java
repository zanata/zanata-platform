package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.common.ResourceType;
import net.openl10n.flies.rest.dto.ContentTypeAdapter;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;
import net.openl10n.flies.rest.dto.extensions.gettext.AbstractResourceMetaExtension;

import org.codehaus.jackson.annotate.JsonIgnore;

@XmlType(name = "abstractResourceMetaType", propOrder = { "name", "extensions" })
public abstract class AbstractResourceMeta implements Serializable, Extensible<AbstractResourceMetaExtension>
{
   private static final long serialVersionUID = 1L;

   private String name;

   private ContentType contentType = ContentType.TextPlain;

   private ResourceType type = ResourceType.FILE;

   private LocaleId lang = LocaleId.EN_US;

   private ExtensionSet<AbstractResourceMetaExtension> extensions;

   // TODO add Links for Resource, ResourceMeta and TranslationsResource

   public AbstractResourceMeta()
   {
   }

   public AbstractResourceMeta(String name)
   {
      this.name = name;
   }

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement(name = "extension")
   public ExtensionSet<AbstractResourceMetaExtension> getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet<AbstractResourceMetaExtension> extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet<AbstractResourceMetaExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<AbstractResourceMetaExtension>();
      return extensions;
   }

   @XmlAttribute(name = "type", required = true)
   public ResourceType getType()
   {
      return type;
   }

   public void setType(ResourceType type)
   {
      this.type = type;
   }

   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   @XmlAttribute(name = "lang", namespace = Namespaces.XML, required = true)
   public LocaleId getLang()
   {
      return lang;
   }

   public void setLang(LocaleId lang)
   {
      this.lang = lang;
   }

   @XmlJavaTypeAdapter(type = ContentType.class, value = ContentTypeAdapter.class)
   @XmlAttribute(name = "content-type", required = true)
   public ContentType getContentType()
   {
      return contentType;
   }

   public void setContentType(ContentType contentType)
   {
      this.contentType = contentType;
   }

   @XmlElement(name = "name", required = true)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   /**
    * Helper method for equals in subclasses.This abstract class does not
    * implement equals or hashCode, because a Resource should not be equal to a
    * ResourceMeta.
    */
   protected int hashCodeHelper()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
      result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
      result = prime * result + ((lang == null) ? 0 : lang.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   /**
    * Helper method for equals in subclasses.This abstract class does not
    * implement equals or hashCode, because a Resource should not be equal to a
    * ResourceMeta.
    */
   protected boolean equalsHelper(AbstractResourceMeta other)
   {
      if (this == other)
      {
         return true;
      }
      if (other == null)
      {
         return false;
      }
      if (contentType == null)
      {
         if (other.contentType != null)
         {
            return false;
         }
      }
      else if (!contentType.equals(other.contentType))
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
      if (name == null)
      {
         if (other.name != null)
         {
            return false;
         }
      }
      else if (!name.equals(other.name))
      {
         return false;
      }
      if (type != other.type)
      {
         return false;
      }
      return true;
   }

}
