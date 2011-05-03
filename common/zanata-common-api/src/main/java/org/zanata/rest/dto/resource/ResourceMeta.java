package org.zanata.rest.dto.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.HasCollectionSample;

@XmlType(name = "resourceMetaType")
@XmlRootElement(name = "resource-meta")
@JsonPropertyOrder( { "name", "contentType", "lang", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class ResourceMeta extends AbstractResourceMeta implements HasCollectionSample<ResourceMeta>
{
   private static final long serialVersionUID = 1L;

   public ResourceMeta()
   {
   }

   public ResourceMeta(String name)
   {
      super(name);
   }

   @Override
   public ResourceMeta createSample()
   {
      ResourceMeta entity = new ResourceMeta();
      entity.setContentType(ContentType.TextPlain);
      entity.setName("readme.txt");
      entity.setLang(LocaleId.EN);
      entity.setType(ResourceType.FILE);
      // TODO add sample extension
      return entity;
   }

   @Override
   public Collection<ResourceMeta> createSamples()
   {
      List<ResourceMeta> elems = new ArrayList<ResourceMeta>(2);
      elems.add(createSample());
      ResourceMeta sample2 = createSample();
      sample2.setName("license.txt");
      elems.add(sample2);
      return elems;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      return super.hashCodeHelper();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof ResourceMeta))
         return false;
      else
         return super.equalsHelper((AbstractResourceMeta) obj);
   }

}
