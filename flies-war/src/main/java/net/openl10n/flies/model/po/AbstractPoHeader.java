package net.openl10n.flies.model.po;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import net.openl10n.flies.model.AbstractFliesEntity;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HSimpleComment;

import org.hibernate.annotations.Type;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PoHeader
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PoHeader
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader
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
