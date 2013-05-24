/**
 * 
 */
package org.zanata.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "contentStateType")
public enum ContentState
{
   // translation life cycle order:
   // New -> NeedReview || Rejected -> Translated -> Approved (Translated in non-review project will automatically go to Approved)
   New, NeedReview, Translated, Approved, Rejected;

   public static final Collection<ContentState> DRAFT_STATES = Collections.unmodifiableCollection(new HashSet<ContentState>(Arrays.asList(NeedReview, Rejected)));
   public static final Collection<ContentState> TRANSLATED_STATES = Collections.unmodifiableCollection(new HashSet<ContentState>(Arrays.asList(Approved, Translated)));

   public boolean isTranslated()
   {
      return TRANSLATED_STATES.contains(this);
   }

   public boolean isRejectedOrFuzzy()
   {
      return DRAFT_STATES.contains(this);
   }

   public boolean isUntranslated()
   {
      return this == New;
   }

   public boolean isApproved()
   {
      return this == Approved;
   }
}