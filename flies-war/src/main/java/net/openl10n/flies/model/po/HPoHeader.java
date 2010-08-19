package net.openl10n.flies.model.po;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import net.openl10n.flies.model.HDocument;

import org.hibernate.annotations.NaturalId;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PoHeader
 */
@Entity
public class HPoHeader extends AbstractPoHeader
{

   private static final long serialVersionUID = 1L;

   private HDocument document;

   public void setDocument(HDocument document)
   {
      this.document = document;
   }

   @OneToOne
   @JoinColumn(name = "document_id", /* nullable=false, */unique = true)
   @NaturalId
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
      return "HPoHeader(" + super.toString() + ")";
   }

}
