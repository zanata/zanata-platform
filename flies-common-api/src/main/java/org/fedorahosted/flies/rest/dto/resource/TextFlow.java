package org.fedorahosted.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.LocaleIdAdapter;
import org.fedorahosted.flies.rest.dto.DTOUtil;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name = "textFlowType", namespace = Namespaces.FLIES, propOrder = { "content", "extensions" })
@XmlRootElement(name = "text-flow", namespace = Namespaces.FLIES)
@JsonPropertyOrder( { "id", "lang", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TextFlow implements Serializable
{

   @NotEmpty
   @Length(max = 255)
   private String id;

   @NotNull
   private LocaleId lang;

   @NotNull
   private String content;

   private ExtensionSet extensions;

   /**
    * This constructor sets the lang value to en-US
    * 
    * @param id Resource Id value
    */
   public TextFlow()
   {
      this.lang = LocaleId.EN_US;
   }

   public TextFlow(String id, LocaleId lang)
   {
      this.id = id;
      this.lang = lang;
   }

   public TextFlow(String id, LocaleId lang, String content)
   {
      this(id, lang);
      this.content = content;
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

   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   @XmlAttribute(name = "lang", namespace = Namespaces.XML, required = false)
   public LocaleId getLang()
   {
      return lang;
   }

   public void setLang(LocaleId lang)
   {
      this.lang = lang;
   }

   @XmlElement(name = "content", namespace = Namespaces.FLIES, required = true)
   public String getContent()
   {
      if (content == null)
         return "";
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

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

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
