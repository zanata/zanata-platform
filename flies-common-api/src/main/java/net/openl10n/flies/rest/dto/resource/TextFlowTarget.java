package net.openl10n.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Extensible;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.Commentable;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.NotEmpty;

@XmlType(name = "textFlowTargetType", propOrder = { "translator", "content", "extensions" })
@XmlRootElement(name = "text-flow-target")
@JsonPropertyOrder( { "resId", "state", "translator", "content", "extensions" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
@XmlSeeAlso({ SimpleComment.class })
public class TextFlowTarget implements Serializable, Commentable, Extensible<TextFlowTarget>
{

   private String resId;
   private ContentState state = ContentState.New;
   private Person translator;
   private String content;
   private ExtensionSet<TextFlowTarget> extensions;

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

   @XmlElementWrapper(name = "extensions", required = false)
   @XmlAnyElement(lax = true)
   public ExtensionSet<TextFlowTarget> getExtensions()
   {
      return extensions;
   }

   @JsonIgnore
   public ExtensionSet<TextFlowTarget> getExtensions(boolean createIfNull)
   {
      if (createIfNull && extensions == null)
         extensions = new ExtensionSet<TextFlowTarget>();
      return extensions;
   }

   public void setExtensions(ExtensionSet<TextFlowTarget> extensions)
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
