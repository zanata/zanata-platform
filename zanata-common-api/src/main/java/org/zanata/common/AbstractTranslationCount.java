package org.zanata.common;

import java.io.Serializable;

public abstract class AbstractTranslationCount implements Serializable
{

   private static final long serialVersionUID = 1L;

   private int approved;
   private int needReview;
   private int untranslated;
   private int saved;

   public AbstractTranslationCount()
   {
   }

   protected AbstractTranslationCount(int approved, int needReview, int untranslated, int saved)
   {
      this.approved = approved;
      this.needReview = needReview;
      this.untranslated = untranslated;
      this.saved = saved;
   }

   protected AbstractTranslationCount(int approved, int needReview, int untranslated)
   {
      this(approved, needReview, untranslated, 0);
   }

   public void increment(ContentState state, int count)
   {
      set(state, get(state) + count);
   }

   public void decrement(ContentState state, int count)
   {
      set(state, get(state) - count);
   }

   public void set(ContentState state, int value)
   {
      switch (state)
      {
      case Approved:
         approved = value;
         break;
      case NeedReview:
         needReview = value;
         break;
      case New:
         untranslated = value;
         break;
      case Saved:
         saved = value;
         break;
      default:
         throw new RuntimeException("not implemented for state " + state.name());
      }
   }

   public int get(ContentState state)
   {
      switch (state)
      {
      case Approved:
         return approved;
      case NeedReview:
         return needReview;
      case New:
         return untranslated;
      case Saved:
         return saved;
      default:
         throw new RuntimeException("not implemented for state " + state.name());
      }
   }

   protected void add(AbstractTranslationCount other)
   {
      this.approved += other.approved;
      this.needReview += other.needReview;
      this.untranslated += other.untranslated;
      this.saved += other.saved;
   }

   protected void set(AbstractTranslationCount other)
   {
      this.approved = other.approved;
      this.needReview = other.needReview;
      this.untranslated = other.untranslated;
      this.saved = other.saved;
   }

   public int getTotal()
   {
      return approved + needReview + untranslated + saved;
   }

   public int getApproved()
   {
      return approved;
   }

   public int getNeedReview()
   {
      return needReview;
   }

   public int getUntranslated()
   {
      return untranslated;
   }

   public int getSaved()
   {
      return saved;
   }

   public int getNotApproved()
   {
      return untranslated + needReview;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null)
         return false;
      if (obj instanceof AbstractTranslationCount)
      {
         AbstractTranslationCount o = (AbstractTranslationCount) obj;
         return (approved == o.approved && needReview == o.needReview && untranslated == o.untranslated && saved == o.saved);
      }
      return false;
   }

}