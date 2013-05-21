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
   New, NeedReview, Approved, Translated, Rejected;

   public static final Collection<ContentState> DRAFT_STATES = Collections.unmodifiableCollection(new HashSet<ContentState>(Arrays.asList(NeedReview, Rejected)));
   public static final Collection<ContentState> TRANSLATED_STATES = Collections.unmodifiableCollection(new HashSet<ContentState>(Arrays.asList(Translated, Approved)));

   public static boolean isTranslated(ContentState state)
   {
      return TRANSLATED_STATES.contains(state);
   }

   public static boolean isDraft(ContentState state)
   {
      return DRAFT_STATES.contains(state);
   }

   public static boolean isUntranslated(ContentState state)
   {
      return state == New;
   }

   public static boolean isApproved(ContentState state)
   {
      return state == Approved;
   }
}