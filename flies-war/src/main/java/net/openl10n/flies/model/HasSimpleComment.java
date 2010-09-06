package net.openl10n.flies.model;

/**
 * Simple interface for Hibernate objects which hold an HSimpleComment property.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public interface HasSimpleComment
{
   public void setComment(HSimpleComment comment);
   public HSimpleComment getComment();
}
