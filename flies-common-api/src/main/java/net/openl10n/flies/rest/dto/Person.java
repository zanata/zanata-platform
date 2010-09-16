package net.openl10n.flies.rest.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name = "personType")
@XmlRootElement(name = "person")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder( { "email", "name", "links" })
@JsonWriteNullProperties(false)
public class Person implements Serializable, HasSample<Person>
{

   private String email;
   private String name;

   private Links links;

   public Person()
   {
   }

   public Person(String email, String name)
   {
      this.email = email;
      this.name = name;
   }

   @XmlAttribute(name = "email", required = true)
   @Email
   @NotNull
   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   @XmlAttribute(name = "name", required = true)
   @NotEmpty
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
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
   public Person createSample()
   {
      return new Person("me@example.com", "Mr. Example");
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
