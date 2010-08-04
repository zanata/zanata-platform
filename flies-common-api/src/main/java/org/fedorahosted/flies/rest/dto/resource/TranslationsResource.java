package org.fedorahosted.flies.rest.dto.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.HasSample;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Links;
import org.fedorahosted.flies.rest.dto.DTOUtil;

@XmlType(name = "translationsResourceType", namespace = Namespaces.FLIES, propOrder = { "links", "extensions", "textFlowTargets" })
@XmlRootElement(name = "translations", namespace = Namespaces.FLIES)
@JsonPropertyOrder( { "links", "extensions", "textFlowTargets" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TranslationsResource implements Serializable, HasSample<TranslationsResource>
{

   private ExtensionSet extensions;
   private Links links;
   private List<TextFlowTarget> textFlowTargets;

   @XmlElementWrapper(name = "extensions", namespace = Namespaces.FLIES, required = false)
   @XmlAnyElement(lax = true)
   public ExtensionSet getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet();
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
