package org.zanata.webtrans.shared.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.zanata.common.ContentState;
import com.google.common.collect.ImmutableSet;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ContentStateConstraints
{
   private static final Collection<ContentState> ALL_STATES = EnumSet.allOf(ContentState.class);
   public static final ContentStateConstraints ALL = new ContentStateConstraints(ALL_STATES);

   private Set<ContentState> accepted;

   private ContentStateConstraints(Collection<ContentState> accepted)
   {
      this.accepted = ImmutableSet.copyOf(accepted);
   }

   public boolean isFilterTranslated()
   {
      return accepted.contains(ContentState.Translated);
   }

   public boolean isFilterFuzzy()
   {
      return accepted.contains(ContentState.NeedReview);
   }

   public boolean isFilterUntranslated()
   {
      return accepted.contains(ContentState.Translated);
   }

   public boolean isFilterIncomplete()
   {
      return accepted.contains(ContentState.New) && accepted.containsAll(ContentState.DRAFT_STATES);
   }

   public boolean isFilterComplete()
   {
      return accepted.containsAll(ContentState.TRANSLATED_STATES);
   }

   public Set<ContentState> getAccepted()
   {
      return accepted;
   }

   // factory methods
   public static ContentStateConstraints all()
   {
      return ALL;
   }

   public static ContentStateConstraints accept(ContentState first, ContentState... rest)
   {
      return new ContentStateConstraints(EnumSet.of(first, rest));
   }

   public static ContentStateConstraints allBut(ContentState first, ContentState... rest)
   {
      EnumSet<ContentState> result = EnumSet.copyOf(ALL_STATES);
      EnumSet<ContentState> exclude = EnumSet.of(first, rest);
      result.removeAll(exclude);
      return new ContentStateConstraints(result);
   }

   /**
    * This method is not scalable. We don't want to maintain a list of boolean and its order to build constraint.
    * Eventually we may want ot change client code to use other factory methods.
    * @param untranslated untranslated
    * @param fuzzy fuzzy
    * @param translated translated
    * @param approved approved
    * @param rejected rejected
    * @return a content state constraint
    */
   public static ContentStateConstraints fromBoolean(boolean untranslated, boolean fuzzy, boolean translated, boolean approved, boolean rejected)
   {
      if (untranslated == fuzzy && fuzzy == translated && translated == approved && approved == rejected)
      {
         return ALL;
      }
      EnumSet<ContentState> result = EnumSet.noneOf(ContentState.class);
      addIfTrue(result, untranslated, ContentState.New);
      addIfTrue(result, fuzzy, ContentState.NeedReview);
      addIfTrue(result, translated, ContentState.Translated);
      addIfTrue(result, approved, ContentState.Approved);
      addIfTrue(result, rejected, ContentState.Rejected);
      return new ContentStateConstraints(result);
   }

   private static void addIfTrue(EnumSet<ContentState> states, boolean condition, ContentState state)
   {
      if (condition)
      {
         states.add(state);
      }
   }
}
