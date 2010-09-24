package net.openl10n.flies.model.po;

import javax.persistence.Entity;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PoHeader
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PoHeader
 */
@Entity
public class HPoHeader extends AbstractPoHeader
{

   private static final long serialVersionUID = 1L;

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HPoHeader(" + super.toString() + ")";
   }

}
