package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.HasSample;
import net.openl10n.flies.rest.dto.Link;
import net.openl10n.flies.rest.dto.Links;
import net.openl10n.flies.rest.dto.extensions.gettext.TranslationsResourceExtension;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

/**
 * Represents the translation of a document into a single locale.
 */
@XmlType(name = "translationsResourceType", propOrder = { "links", "extensions", "textFlowTargets" })
@XmlRootElement(name = "translations")
@JsonPropertyOrder( { "links", "extensions", "textFlowTargets" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TranslationsResource implements Serializable, HasSample<TranslationsResource>, Extensible<TranslationsResourceExtension>
{
   private static final long serialVersionUID = 1L;
   private ExtensionSet<TranslationsResourceExtension> extensions;
   private Links links;
   private List<TextFlowTarget> textFlowTargets;

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement(name = "extension")
   public ExtensionSet<TranslationsResourceExtension> getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet<TranslationsResourceExtension> extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet<TranslationsResourceExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TranslationsResourceExtension>();
      return extensions;
   }

   @XmlElementWrapper(name = "targets", required = false)
   @XmlElementRef
   public List<TextFlowTarget> getTextFlowTargets()
   {
      if (textFlowTargets == null)
      {
         textFlowTargets = new ArrayList<TextFlowTarget>();
      }
      return textFlowTargets;
   }


   @XmlElementRef(type = Link.class)
   public Links getLinks()
   {
      return links;
   }

   public void setLinks(Links links)
   {
      this.links = links;
   }

   @JsonIgnore
   public Links getLinks(boolean createIfNull)
   {
      if (createIfNull && links == null)
         links = new Links();
      return links;
   }

   @Override
   public TranslationsResource createSample()
   {
      return new TranslationsResource();
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
      result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
      result = prime * result + ((links == null) ? 0 : links.hashCode());
      result = prime * result + ((textFlowTargets == null) ? 0 : textFlowTargets.hashCode());
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
      if (!(obj instanceof TranslationsResource))
      {
         return false;
      }
      TranslationsResource other = (TranslationsResource) obj;
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
      if (links == null)
      {
         if (other.links != null)
         {
            return false;
         }
      }
      else if (!links.equals(other.links))
      {
         return false;
      }
      if (textFlowTargets == null)
      {
         if (other.textFlowTargets != null)
         {
            return false;
         }
      }
      else if (!textFlowTargets.equals(other.textFlowTargets))
      {
         return false;
      }
      return true;
   }

}
