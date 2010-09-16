package net.openl10n.flies.model.po;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HLocale;

import org.hibernate.annotations.NaturalId;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PoTargetHeader
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader
 */
@Entity
public class HPoTargetHeader extends AbstractPoHeader
{

   private static final long serialVersionUID = 1L;

   private HLocale targetLanguage;
   private HDocument document;

   public void setTargetLanguage(HLocale targetLanguage)
   {
      this.targetLanguage = targetLanguage;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "targetLanguage", nullable = false)
   public HLocale getTargetLanguage()
   {
      return targetLanguage;
   }

   public void setDocument(HDocument document)
   {
      this.document = document;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   public HDocument getDocument()
   {
      return document;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HPoTargetHeader(" + super.toString() + "lang:" + getTargetLanguage() + ")";
   }

}
