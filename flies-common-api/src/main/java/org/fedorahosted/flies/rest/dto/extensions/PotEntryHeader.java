package org.fedorahosted.flies.rest.dto.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.fedorahosted.flies.rest.dto.resource.Extension;

public class PotEntryHeader extends Extension
{

   public static final String ID = "gettext-entry-header";
   public static final String VERSION = PoHeader.VERSION;
   public static final String NAMESPACE = PoHeader.NAMESPACE;

   private String context;
   private String extractedComment;
   private List<String> flags;
   private List<String> references;

   public PotEntryHeader()
   {
      super(ID, VERSION);
   }

   @XmlElement(name = "context", namespace = PoHeader.NAMESPACE, required = false)
   public String getContext()
   {
      return context;
   }

   public void setContext(String context)
   {
      this.context = context;
   }

   @XmlElement(name = "extracted-comment", namespace = PoHeader.NAMESPACE, required = false)
   public String getExtractedComment()
   {
      return extractedComment;
   }

   public void setExtractedComment(String extractedComment)
   {
      this.extractedComment = extractedComment;
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
