package net.openl10n.flies.rest.dto.extensions.gettext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.openl10n.flies.rest.dto.DTOUtil;


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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((context == null) ? 0 : context.hashCode());
      result = prime * result + ((extractedComment == null) ? 0 : extractedComment.hashCode());
      result = prime * result + ((flags == null) ? 0 : flags.hashCode());
      result = prime * result + ((references == null) ? 0 : references.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof PotEntryHeader))
      {
         return false;
      }
      PotEntryHeader other = (PotEntryHeader) obj;
      if (context == null)
      {
         if (other.context != null)
         {
            return false;
         }
      }
      else if (!context.equals(other.context))
      {
         return false;
      }
      if (extractedComment == null)
      {
         if (other.extractedComment != null)
         {
            return false;
         }
      }
      else if (!extractedComment.equals(other.extractedComment))
      {
         return false;
      }
      if (flags == null)
      {
         if (other.flags != null)
         {
            return false;
         }
      }
      else if (!flags.equals(other.flags))
      {
         return false;
      }
      if (references == null)
      {
         if (other.references != null)
         {
            return false;
         }
      }
      else if (!references.equals(other.references))
      {
         return false;
      }
      return true;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}
