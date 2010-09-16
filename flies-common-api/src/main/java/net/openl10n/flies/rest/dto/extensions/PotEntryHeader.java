package net.openl10n.flies.rest.dto.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.rest.dto.ExtensionValue;
import net.openl10n.flies.rest.dto.resource.TextFlow;

/**
 * Holds gettext message-level metadata for a source document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlRootElement(name = "pot-entry-header", namespace = Namespaces.FLIES)
public class PotEntryHeader implements ExtensionValue<TextFlow>
{

   public static final String ID = "gettext";
   
   private String context;
   private List<String> flags;
   private List<String> references;

   @XmlElement(name = "context", namespace = PoHeader.NAMESPACE, required = false)
   public String getContext()
   {
      return context;
   }

   public void setContext(String context)
   {
      this.context = context;
   }

   @XmlElementWrapper(name = "flags", namespace = PoHeader.NAMESPACE, required = true)
   @XmlElement(name = "flag", namespace = PoHeader.NAMESPACE)
   public List<String> getFlags()
   {
      if (flags == null)
         flags = new ArrayList<String>();
      return flags;
   }

   @XmlElementWrapper(name = "source-references", namespace = PoHeader.NAMESPACE, required = true)
   @XmlElement(name = "sourcereference", namespace = PoHeader.NAMESPACE)
   public List<String> getReferences()
   {
      if (references == null)
         references = new ArrayList<String>();
      return references;
   }

}
