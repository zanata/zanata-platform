package org.zanata.rest.dto.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonValue;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.HasSample;

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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((resources == null) ? 0 : resources.hashCode());
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
      if (!(obj instanceof ResourceMetaList))
      {
         return false;
      }
      ResourceMetaList other = (ResourceMetaList) obj;
      if (resources == null)
      {
         if (other.resources != null)
         {
            return false;
         }
      }
      else if (!resources.equals(other.resources))
      {
         return false;
      }
      return true;
   }

}
