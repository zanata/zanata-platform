package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.MediaTypes.Format;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@XmlType(name = "updateProjectIterationType", propOrder = { "id", "replaceList" })
@XmlRootElement(name = "updateProjectIteration")
@JsonPropertyOrder({ "id", "replaceList" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class UpdateProjectIteration implements Serializable, HasCollectionSample<UpdateProjectIteration>, HasMediaType
{
   private static final long serialVersionUID = 1L;
   private String id;
   private List<Replace> replaceList = new ArrayList<Replace>();

   public UpdateProjectIteration()
   {
   }

   public UpdateProjectIteration(String id, ProjectType projectType)
   {
      this.id = id;

      if (projectType != null)
      {
         replaceList.add(new Replace("defaultType", projectType.toString()));
      }
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

   @XmlElementWrapper(name = "replace-list", namespace = Namespaces.ZANATA_OLD)
   @XmlElementRef(namespace = Namespaces.ZANATA_OLD)
   public List<Replace> getReplaceList()
   {
      return replaceList;
   }

   public void setReplaceList(List<Replace> replaceList)
   {
      this.replaceList = replaceList;
   }

   @Override
   public UpdateProjectIteration createSample()
   {
      return new UpdateProjectIteration("sample-iteration", ProjectType.Gettext);
   }

   @Override
   public Collection<UpdateProjectIteration> createSamples()
   {
      Collection<UpdateProjectIteration> entities = new ArrayList<UpdateProjectIteration>();
      entities.add(createSample());
      UpdateProjectIteration p2 = new UpdateProjectIteration("sample-iteration", ProjectType.Gettext);
      entities.add(p2);
      return entities;
   }

   @Override
   public String getMediaType(Format format)
   {
      return MediaTypes.APPLICATION_ZANATA_XML + format;
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
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((replaceList == null) ? 0 : replaceList.hashCode());
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
      if (!(obj instanceof UpdateProjectIteration))
      {
         return false;
      }
      UpdateProjectIteration other = (UpdateProjectIteration) obj;

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
      if (replaceList == null)
      {
         if (other.replaceList != null)
         {
            return false;
         }
      }
      else if (!replaceList.equals(other.replaceList))
      {
         return false;
      }
      return true;
   }

}
