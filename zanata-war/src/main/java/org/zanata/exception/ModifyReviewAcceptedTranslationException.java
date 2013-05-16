package org.zanata.exception;

import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ModifyReviewAcceptedTranslationException extends RuntimeException
{
   private String lastModifiedPerson;

   public ModifyReviewAcceptedTranslationException(String lastModifiedPerson)
   {
      super(lastModifiedPerson + " has accepted this translation in review process.");
      this.lastModifiedPerson = lastModifiedPerson;
   }

   public String getLastModifiedPerson()
   {
      return lastModifiedPerson;
   }
}
