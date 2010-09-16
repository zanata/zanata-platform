package net.openl10n.flies.rest.dto.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Holds gettext file headers for a target document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlType(name = "poTargetHeader", namespace = PoHeader.NAMESPACE, propOrder = { "comment", "entries" })
@XmlRootElement(name = "po-target-header", namespace = PoHeader.NAMESPACE)
@JsonTypeName(value = "po-target-header")
public class PoTargetHeader implements ExtensionValue<TranslationsResource>
{

   public static final String ID = "gettext";
   public static final String VERSION = PoHeader.VERSION;
   public static final String NAMESPACE = PoHeader.NAMESPACE;

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

   @XmlElement(name = "comment", namespace = PoHeader.NAMESPACE, required = true)
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   @XmlElementWrapper(name = "entries", namespace = PoHeader.NAMESPACE, required = true)
   @XmlElement(name = "entry", namespace = PoHeader.NAMESPACE)
   public List<HeaderEntry> getEntries()
   {
      if (entries == null)
         entries = new ArrayList<HeaderEntry>();
      return entries;
   }

}
