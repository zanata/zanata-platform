package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.HasSample;
import net.openl10n.flies.rest.dto.Link;
import net.openl10n.flies.rest.dto.Links;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeader;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

/**
 * Represents the translation of a document into a single locale.
 */
@XmlType(name = "translationsResourceType", namespace = Namespaces.FLIES, propOrder = { "links", "extensions", "textFlowTargets" })
@XmlRootElement(name = "translations", namespace = Namespaces.FLIES)
@JsonPropertyOrder( { "links", "extensions", "textFlowTargets" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
@XmlSeeAlso({ PoTargetHeader.class })
public class TranslationsResource implements Serializable, HasSample<TranslationsResource>
{

   private ExtensionSet<TranslationsResource> extensions;
   private Links links;
   private List<TextFlowTarget> textFlowTargets;

   @XmlElementWrapper(name = "extensions", namespace = Namespaces.FLIES, required = false)
   @XmlAnyElement(lax = true)
   public ExtensionSet<TranslationsResource> getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet<TranslationsResource> extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet<TranslationsResource> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TranslationsResource>();
      return extensions;
   }

   @XmlElementWrapper(name = "targets", namespace = Namespaces.FLIES, required = false)
   @XmlElementRef
   public List<TextFlowTarget> getTextFlowTargets()
   {
      return textFlowTargets;
   }

   @JsonIgnore
   public List<TextFlowTarget> getTextFlowTargets(boolean createIfNull)
   {
      if (createIfNull && textFlowTargets == null)
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

}
