package net.openl10n.flies.rest.dto.deprecated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.ContentTypeAdapter;
import net.openl10n.flies.rest.dto.DTOUtil;
import net.openl10n.flies.rest.dto.Links;
import net.openl10n.flies.rest.dto.LocaleIdAdapter;
import net.openl10n.flies.rest.dto.po.PoHeader;
import net.openl10n.flies.rest.dto.po.PoTargetHeaders;

@XmlRootElement(name = "document", namespace = Namespaces.FLIES)
@XmlType(name = "documentType", namespace = Namespaces.FLIES, propOrder = { "textFlows", "extensions", "links" })
@XmlSeeAlso( { PoHeader.class, PoTargetHeaders.class })
@Deprecated
public class Document implements IExtensible
{

   /**
    * An opaque id, which is the canonical id of the Document
    */
   private String id;

   /**
    * Just the filename without the path
    */
   private String name;

   private Links links;

   /**
    * Pathname (slash-separated) for the parent folder, which may be empty for
    * files which are in the root
    */
   private String path;
   private ContentType contentType;
   private Integer revision = null;
   private LocaleId lang = LocaleId.EN_US;

   private List<TextFlow> textFlows;
   private List<Object> extensions;

   protected Document()
   {
      super();
   }

   public Document(String fullPath, ContentType contentType)
   {
      int lastSepChar = fullPath.lastIndexOf('/');
      switch (lastSepChar)
      {
      case -1:
         this.path = "";
         this.name = fullPath;
         break;
      case 0:
         this.path = "/";
         this.name = fullPath.substring(1);
         break;
      default:
         this.path = fullPath.substring(0, lastSepChar + 1);
         this.name = fullPath.substring(lastSepChar + 1);
      }
      this.contentType = contentType;
      this.id = fullPath;
   }

   public Document(String id, String name, String path, ContentType contentType)
   {
      this.id = id;
      this.name = name;
      this.path = path;
      this.contentType = contentType;
   }

   public Document(String id, String name, String path, ContentType contentType, Integer revision, LocaleId lang)
   {
      this(id, name, path, contentType);
      this.revision = revision;
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

   @XmlAttribute(name = "name", required = true)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @XmlAttribute(name = "path", required = false)
   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   @XmlElement(name = "link", namespace = Namespaces.FLIES, required = false)
   public Links getLinks()
   {
      if (links == null)
         links = new Links();
      return links;
   }

   /**
    * Holds the current version in GET requests
    * 
    * If used in add/update operations, this field should hold the the value of
    * (current version + 1) or the operation will fail.
    * 
    * @return
    */
   @XmlAttribute(name = "revision", required = false)
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
   }

   @XmlJavaTypeAdapter(type = ContentType.class, value = ContentTypeAdapter.class)
   @XmlAttribute(name = "content-type", required = true)
   public ContentType getContentType()
   {
      return contentType;
   }

   public void setContentType(ContentType contentType)
   {
      this.contentType = contentType;
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

   @XmlElementWrapper(name = "text-flows", namespace = Namespaces.FLIES, required = false)
   @XmlElements( { @XmlElement(name = "text-flow", type = TextFlow.class, namespace = Namespaces.FLIES) })
   public List<TextFlow> getTextFlows()
   {
      if (textFlows == null)
         textFlows = new ArrayList<TextFlow>();
      return textFlows;
   }

   @Override
   @XmlAnyElement(lax = true)
   public List<Object> getExtensions()
   {
      return extensions;
   }

   public List<Object> getExtensions(boolean create)
   {
      if (extensions == null)
         extensions = new ArrayList<Object>();
      return extensions;
   }

   public void setExtensions(List<Object> extensions)
   {
      this.extensions = extensions;
   }

   public boolean hasExtensions()
   {
      return extensions != null;
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
            getExtensions(true).add(ext);
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
