package org.fedorahosted.flies.model.po;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.fedorahosted.flies.model.AbstractFliesEntity;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HSimpleComment;
import org.hibernate.annotations.Type;

/**
 * 
 * @author sflaniga@redhat.com
 * @see org.fedorahosted.flies.rest.dto.po.PoHeader
 */
@MappedSuperclass
public abstract class AbstractPoHeader extends AbstractFliesEntity
{

   private HSimpleComment comment;
   private String entries;

   public abstract void setDocument(HDocument document);

   @Transient
   public abstract HDocument getDocument();

   public void setComment(HSimpleComment comment)
   {
      this.comment = comment;
   }

   @OneToOne(optional = true, cascade = CascadeType.ALL)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getComment()
   {
      return comment;
   }

   // stored in the format used by java.util.Properties.store(Writer)
   // see PoUtility.headerEntriesToString
   public void setEntries(String entries)
   {
      this.entries = entries;
   }

   // see PoUtility.stringToHeaderEntries
   @Type(type = "text")
   public String getEntries()
   {
      return entries;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "document:" + getDocument() + "comment:" + getComment() + "entries:" + getEntries() + "";
   }
}
