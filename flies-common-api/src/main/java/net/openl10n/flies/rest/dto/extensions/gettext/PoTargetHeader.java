package net.openl10n.flies.rest.dto.extensions.gettext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Holds gettext file headers for a target document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlType(name = "poTargetHeader", propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-target-header")
@JsonTypeName(value = "po-target-header")
public class PoTargetHeader implements ExtensionValue<TranslationsResource>
{

   public static final String ID = "gettext";
   public static final String VERSION = PoHeader.VERSION;

   private String comment;
   private List<HeaderEntry> entries;

   public PoTargetHeader()
   {
   }

   public PoTargetHeader(String comment, HeaderEntry... entries)
   {
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
