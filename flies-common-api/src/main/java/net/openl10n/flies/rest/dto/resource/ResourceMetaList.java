package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.HasSample;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * 
 * This class is only used for generating the schema, as List<ResourceMeta>
 * serializes better across Json and XML.
 * 
 * @author asgeirf
 * 
 */
@XmlType(name = "resourcesListType", propOrder = { "resources" })
@XmlRootElement(name = "resources")
public class ResourceMetaList implements Serializable, HasSample<ResourceMetaList>
{

   private List<ResourceMeta> resources;

   @XmlElement(name = "resource", required = true)
   @JsonValue
   public List<ResourceMeta> getResources()
   {
      if (resources == null)
      {
         resources = new ArrayList<ResourceMeta>();
      }
      return resources;
   }

   @Override
   public ResourceMetaList createSample()
   {
      ResourceMetaList entity = new ResourceMetaList();
      entity.getResources().addAll(new ResourceMeta().createSamples());
      return entity;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
