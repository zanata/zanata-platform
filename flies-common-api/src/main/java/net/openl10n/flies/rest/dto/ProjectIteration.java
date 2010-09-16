package net.openl10n.flies.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.MediaTypes.Format;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

@XmlType(name = "projectIterationType", propOrder = { "name", "description", "links" })
@XmlRootElement(name = "project-iteration")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
@JsonPropertyOrder( { "id", "name", "description", "links" })
public class ProjectIteration implements Serializable, HasCollectionSample<ProjectIteration>, HasMediaType
{

   private String id;
   private String name;
   private String description;
   private Links links;

   public ProjectIteration()
   {
   }

   public ProjectIteration(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public ProjectIteration(String id, String name, String description)
   {
      this(id, name);
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

   /**
    * Set of links managed by this resource
    * 
    * This field is ignored in PUT/POST operations
    * 
    * @return set of Links managed by this resource
    */
   @XmlElement(name = "link", required = false)
   public Links getLinks()
   {
      return links;
   }

   public void setLinks(Links links)
   {
      this.links = links;
   }

   public Links getLinks(boolean createIfNull)
   {
      if (createIfNull && links == null)
         links = new Links();
      return links;
   }

   @Override
   public ProjectIteration createSample()
   {
      ProjectIteration entity = new ProjectIteration("sample-iteration", "Sample Iteration", "Description of Sample Iteration");
      return entity;
   }

   @Override
   public Collection<ProjectIteration> createSamples()
   {
      Collection<ProjectIteration> entities = new ArrayList<ProjectIteration>();
      entities.add(createSample());
      ProjectIteration entity = new ProjectIteration("another-iteration", "Another Iteration", "Description of Another Iteration");
      entities.add(entity);
      return entities;
   }

   @Override
   public String getMediaType(Format format)
   {
      return MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION + format;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
