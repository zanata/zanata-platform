package org.zanata.common;

import java.io.Serializable;

public final class TransUnitCount implements Serializable
{

   private static final long serialVersionUID = 1L;

   private int approved;
   private int needReview;
   private int untranslated;

   public TransUnitCount()
   {
   }

   public TransUnitCount(int approved, int needReview, int untranslated)
   {
      this.approved = approved;
      this.needReview = needReview;
      this.untranslated = untranslated;
   }

   public void increment(ContentState state)
   {
      increment(state, 1);
   }

   public void increment(ContentState state, int count)
   {
      set(state, get(state) + count);
   }

   public void decrement(ContentState state)
   {
      decrement(state, 1);
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
      default:
         throw new RuntimeException("not implemented for state " + state.name());
      }
   }

   public void add(TransUnitCount other)
   {
      this.approved += other.approved;
      this.needReview += other.needReview;
      this.untranslated += other.untranslated;
   }

   public void set(TransUnitCount other)
   {
      this.approved = other.approved;
      this.needReview = other.needReview;
      this.untranslated = other.untranslated;
   }

   public int getTotal()
   {
      return approved + needReview + untranslated;
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

   public int getNotApproved()
   {
      return untranslated + needReview;
   }

}