package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowExtension;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name = "textFlowType", propOrder = { "content", "extensions" })
@XmlRootElement(name = "text-flow")
@JsonPropertyOrder( { "id", "lang", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TextFlow implements Extensible<TextFlowExtension>, Serializable
{
   private static final long serialVersionUID = 1L;

   @NotEmpty
   @Length(max = 255)
   private String id;

   @NotNull
   private LocaleId lang;

   @NotNull
   private String content;

   private ExtensionSet<TextFlowExtension> extensions;

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

   @XmlElement(name = "content", required = true)
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

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement
   public ExtensionSet<TextFlowExtension> getExtensions()
   {
      return extensions;
   }

   public void setExtensions(ExtensionSet<TextFlowExtension> extensions)
   {
      this.extensions = extensions;
   }

   @JsonIgnore
   public ExtensionSet<TextFlowExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TextFlowExtension>();
      return extensions;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
