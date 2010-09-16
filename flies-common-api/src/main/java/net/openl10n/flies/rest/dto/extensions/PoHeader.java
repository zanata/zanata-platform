package net.openl10n.flies.rest.dto.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Holds gettext file headers for a source document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlType(name = "poHeaderExtension", namespace = PoHeader.NAMESPACE, propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-header", namespace = PoHeader.NAMESPACE)
@JsonTypeName(value = "po-header")
public class PoHeader implements ExtensionValue<AbstractResourceMeta>
{

   public static final String ID = "gettext";
   public static final String VERSION = "1.0";
   public static final String NAMESPACE = "http://flies.openl10n.net/api/gettext/";

   private String comment;
   private List<HeaderEntry> entries;

   public PoHeader()
   {
   }

   public PoHeader(String comment, HeaderEntry... entries)
   {
      this();
      setComment(comment);
      for (int i = 0; i < entries.length; i++)
      {
         getEntries().add(entries[i]);
      }
   }

   @XmlElement(name = "comment", namespace = NAMESPACE, required = true)
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   @XmlElementWrapper(name = "entries", namespace = NAMESPACE, required = true)
   @XmlElement(name = "entry", namespace = NAMESPACE)
   public List<HeaderEntry> getEntries()
   {
      if (entries == null)
         entries = new ArrayList<HeaderEntry>();
      return entries;
   }

}
