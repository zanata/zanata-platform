package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.zanata.common.EntityStatus;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.MediaTypes.Format;

/**
 * Representation of the data within a Project resource
 * 
 * @author asgeirf
 * 
 */
@XmlType(name = "projectType", propOrder = { "name", "description", "links", "iterations", "status" })
@XmlRootElement(name = "project")
@JsonPropertyOrder( { "id", "type", "name", "description", "links", "iterations", "status" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class Project implements Serializable, HasCollectionSample<Project>, HasMediaType
{

   private String id;
   private String name;
   private ProjectType type = ProjectType.IterationProject;
   private String description;
   private EntityStatus status = EntityStatus.ACTIVE;

   private Links links;

   private List<ProjectIteration> iterations;

   public Project()
   {
   }

   public Project(String id, String name, ProjectType type)
   {
      this.id = id;
      this.name = name;
      this.type = type;
   }

   public Project(String id, String name, ProjectType type, String description)
   {
      this(id, name, type);
      this.description = description;
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

   @XmlAttribute(name = "type", required = true)
   public ProjectType getType()
   {
      return type;
   }

   public void setType(ProjectType type)
   {
      this.type = type;
   }

   @NotEmpty
   @Length(max = 80)
   @XmlElement(name = "name", required = true)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Length(max = 80)
   @XmlElement(name = "description", required = false)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @XmlElement(name = "link", namespace = Namespaces.ZANATA_API)
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

   @XmlElementWrapper(name = "project-iterations")
   @XmlElementRef
   public List<ProjectIteration> getIterations()
   {
      return iterations;
   }

   public void setIterations(List<ProjectIteration> iterations)
   {
      this.iterations = iterations;
   }

   public List<ProjectIteration> getIterations(boolean createIfNull)
   {
      if (createIfNull && iterations == null)
         iterations = new ArrayList<ProjectIteration>();
      return getIterations();
   }
   
   @XmlElement(name = "status", required = false)
   public EntityStatus getStatus()
   {
      return status;
   }

   public void setStatus(EntityStatus status)
   {
      this.status = status;
   }

   @Override
   public Project createSample()
   {
      Project entity = new Project();
      entity.setId("sample-project");
      entity.setName("Sample Project");
      entity.setDescription("Sample Project Description");
      entity.setType(ProjectType.IterationProject);
      entity.getIterations(true).addAll(new ProjectIteration().createSamples());
      return entity;
   }

   @Override
   public Collection<Project> createSamples()
   {
      Collection<Project> entities = new ArrayList<Project>();
      entities.add(createSample());
      Project p2 = createSample();
      p2.setId("another-project");
      p2.setName("Another Sample Project");
      p2.setDescription("Another Sample Project Description");
      entities.add(p2);
      return entities;
   }

   @Override
   public String getMediaType(Format format)
   {
      return MediaTypes.APPLICATION_ZANATA_PROJECT + format;
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
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((iterations == null) ? 0 : iterations.hashCode());
      result = prime * result + ((links == null) ? 0 : links.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      result = prime * result + ((status == null) ? 0 : status.hashCode());
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
      if (!(obj instanceof Project))
      {
         return false;
      }
      Project other = (Project) obj;
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
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
      if (iterations == null)
      {
         if (other.iterations != null)
         {
            return false;
         }
      }
      else if (!iterations.equals(other.iterations))
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
      if(status != other.status)
      {
         return false;
      }
      return true;
   }

}
