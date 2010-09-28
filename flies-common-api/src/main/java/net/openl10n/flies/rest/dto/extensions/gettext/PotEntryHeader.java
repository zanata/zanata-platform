package net.openl10n.flies.rest.dto.extensions.gettext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Holds gettext message-level metadata for a source document.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@XmlRootElement(name = "pot-entry-header")
public class PotEntryHeader implements TextFlowExtension
{

   public static final String ID = "gettext";
   
   private String context;
   private List<String> flags;
   private List<String> references;
   private String extractedComment;

   @XmlElement(name = "context", required = false)
   public String getContext()
   {
      return context;
   }

   public void setContext(String context)
   {
      this.context = context;
   }

   @XmlElement(name = "extractedComment", required = false)
   public String getExtractedComment()
   {
      return extractedComment;
   }

   public void setExtractedComment(String comment)
   {
      this.extractedComment = comment;
   }

   @XmlElementWrapper(name = "flags", required = true)
   @XmlElement(name = "flag")
   public List<String> getFlags()
   {
      if (flags == null)
         flags = new ArrayList<String>();
      return flags;
   }

   @XmlElementWrapper(name = "source-references", required = true)
   @XmlElement(name = "sourcereference")
   public List<String> getReferences()
   {
      if (references == null)
         references = new ArrayList<String>();
      return references;
   }

}
