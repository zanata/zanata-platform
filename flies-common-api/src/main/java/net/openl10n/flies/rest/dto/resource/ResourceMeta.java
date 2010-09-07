package net.openl10n.flies.rest.dto.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.common.ResourceType;
import net.openl10n.flies.rest.dto.HasCollectionSample;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@XmlType(name = "resourceMetaType", namespace = Namespaces.FLIES)
@XmlRootElement(name = "resource-meta", namespace = Namespaces.FLIES)
@JsonPropertyOrder( { "name", "contentType", "lang", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class ResourceMeta extends AbstractResourceMeta implements HasCollectionSample<ResourceMeta>
{

   public ResourceMeta()
   {
   }

   public ResourceMeta(String resId)
   {
      super(resId);
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

}
