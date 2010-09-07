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

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;
import net.openl10n.flies.rest.dto.po.PotEntryData;

@XmlType(name = "textFlowType", namespace = Namespaces.FLIES, propOrder = { "content", "extensions" })
@XmlRootElement(name = "text-flow", namespace = Namespaces.FLIES)
@XmlSeeAlso( { PotEntryData.class, TextFlowTargets.class, SimpleComment.class })
public class TextFlow implements IExtensible
{

   private String id;
   private LocaleId lang;
   private Integer revision = null;

   private String content;
   private List<Object> extensions;

   public TextFlow()
   {
   }

   public TextFlow(String id)
   {
      this.id = id;
   }

   public TextFlow(String id, LocaleId lang)
   {
      this(id);
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

   @XmlAttribute(name = "revision", required = false)
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
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

   public boolean hasTargets()
   {
      return getExtension(TextFlowTargets.class) != null;
   }

   public TextFlowTargets getTargets()
   {
      return getExtension(TextFlowTargets.class);
   }

   public boolean hasComment()
   {
      return getExtension(SimpleComment.class) != null;
   }

   /**
    * This represents a comment provided by the source (eg an extracted
    * programmer comment, or a comment in a Properties file for the default
    * locale). It is not expected to be modified by flies.
    */
   public SimpleComment getComment()
   {
      return getExtension(SimpleComment.class);
   }

   public SimpleComment getOrAddComment()
   {
      return getOrAddExtension(SimpleComment.class);
   }

   public TextFlowTarget getTarget(LocaleId localeId)
   {
      TextFlowTargets targets = getTargets();
      if (targets == null)
         return null;
      for (TextFlowTarget t : targets.getTargets())
      {
         if (localeId.equals(t.getLang()))
            return t;
      }
      return null;
   }

   public void addTarget(TextFlowTarget target)
   {
      TextFlowTargets targets = getTargets();
      if (targets == null)
      {
         targets = new TextFlowTargets();
         getExtensions().add(targets);
      }
      targets.getTargets().add(target);
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
