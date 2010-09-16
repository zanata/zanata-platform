package net.openl10n.flies.rest.dto.extensions.gettext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Holds gettext file headers for a source document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlType(name = "poHeaderExtension", propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-header")
@JsonTypeName(value = "po-header")
public class PoHeader implements ExtensionValue<AbstractResourceMeta>
{

   public static final String ID = "gettext";
   public static final String VERSION = "1.0";

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

   @XmlElement(name = "comment", required = true)
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   @XmlElementWrapper(name = "entries", required = true)
   @XmlElement(name = "entry")
   public List<HeaderEntry> getEntries()
   {
      if (entries == null)
         entries = new ArrayList<HeaderEntry>();
      return entries;
   }

}
