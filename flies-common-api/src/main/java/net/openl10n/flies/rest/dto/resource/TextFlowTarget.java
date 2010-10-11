package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowTargetExtension;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.NotEmpty;

@XmlType(name = "textFlowTargetType", propOrder = { "description", "translator", "content", "extensions" })
@XmlRootElement(name = "text-flow-target")
@JsonPropertyOrder( { "resId", "state", "translator", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class TextFlowTarget implements Serializable, Extensible<TextFlowTargetExtension>
{

   private static final long serialVersionUID = 1L;
   private String resId;
   private ContentState state = ContentState.New;
   private Person translator;
   private String content;
   private String description;
   private ExtensionSet<TextFlowTargetExtension> extensions;

   public TextFlowTarget()
   {
   }

   public TextFlowTarget(String resId)
   {
      this.resId = resId;
   }

   @XmlElementRef
   public Person getTranslator()
   {
      return translator;
   }

   public void setTranslator(Person translator)
   {
      this.translator = translator;
   }

   @XmlAttribute(name = "state", required = true)
   public ContentState getState()
   {
      return state;
   }

   public void setState(ContentState state)
   {
      this.state = state;
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

   /**
    * Optional descriptive text to identify the TextFlowTarget, eg an
    * abbreviated version of the source text being translated. This can be used
    * for a more readable XML serialisation.
    * 
    * @return
    */
   @XmlElement(name = "description", required = false)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlElement
   public ExtensionSet<TextFlowTargetExtension> getExtensions()
   {
      return extensions;
   }

   @JsonIgnore
   public ExtensionSet<TextFlowTargetExtension> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TextFlowTargetExtension>();
      return extensions;
   }

   public void setExtensions(ExtensionSet<TextFlowTargetExtension> extensions)
   {
      this.extensions = extensions;
   }

   @XmlAttribute(name = "res-id", required = true)
   @NotEmpty
   public String getResId()
   {
      return resId;
   }

   public void setResId(String resId)
   {
      this.resId = resId;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
