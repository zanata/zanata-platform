package net.openl10n.flies.rest.dto.deprecated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;

@XmlType(name = "textFlowTargetType", namespace = Namespaces.FLIES, propOrder = { "content", "extensions" })
@XmlRootElement(name = "text-flow-target", namespace = Namespaces.FLIES)
@XmlSeeAlso( { SimpleComment.class })
@Deprecated
public class TextFlowTarget implements IExtensible
{

   private LocaleId lang;
   private List<Object> extensions;

   private String id;
   private Integer resourceRevision;
   private ContentState state = ContentState.New;
   private String content;

   public TextFlowTarget()
   {
      // TODO Auto-generated constructor stub
   }

   public TextFlowTarget(TextFlow resource)
   {
      this.id = resource.getId();
      this.resourceRevision = resource.getRevision();
   }

   public TextFlowTarget(TextFlow resource, LocaleId lang)
   {
      this(resource);
      this.lang = lang;
   }

   public boolean hasComment()
   {
      return getExtension(SimpleComment.class) != null;
   }

   /**
    * This represents a comment entered by a translator, whether in flies or
    * imported from a PO/Properties file.
    */
   public SimpleComment getComment()
   {
      return getExtension(SimpleComment.class);
   }

   public SimpleComment getOrAddComment()
   {
      return getOrAddExtension(SimpleComment.class);
   }

   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   @XmlAttribute(name = "lang", namespace = Namespaces.XML, required = true)
   public LocaleId getLang()
   {
      return lang;
   }

   public void setLang(LocaleId lang)
   {
      this.lang = lang;
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

   @XmlAttribute(name = "resourceRevision", required = false)
   public Integer getResourceRevision()
   {
      return resourceRevision;
   }

   public void setResourceRevision(Integer resourceRevision)
   {
      this.resourceRevision = resourceRevision;
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

   @XmlAnyElement(lax = true)
   public List<Object> getExtensions()
   {
      if (extensions == null)
         extensions = new ArrayList<Object>();
      return extensions;
   }

   @Override
   public <T> T getExtension(Class<T> clz)
   {
      if (extensions == null)
         return null;
      for (Object o : extensions)
      {
         if (clz.isInstance(o))
            return clz.cast(o);
      }
      return null;
   }

   @Override
   public <T> T getOrAddExtension(Class<T> clz)
   {
      T ext = getExtension(clz);
      if (ext == null)
      {
         try
         {
            ext = clz.newInstance();
            getExtensions().add(ext);
         }
         catch (Throwable e)
         {
            throw new RuntimeException("unable to create instance", e);
         }
      }
      return ext;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
