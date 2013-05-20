package org.zanata.common;

public class TransUnitWords extends AbstractTranslationCount
{

   private static final long serialVersionUID = 849734798480292025L;

   public TransUnitWords()
   {
   }

   public TransUnitWords(int approved, int needReview, int untranslated, int saved)
   {
      super(approved, needReview, untranslated, saved);
   }

   public TransUnitWords(int approved, int needReview, int untranslated)
   {
      super(approved, needReview, untranslated);
   }

   public void add(TransUnitWords other)
   {
      super.add(other);
   }

   public void set(TransUnitWords other)
   {
      super.set(other);
   }

   public int getPer()
   {
      int total = getTotal();
      if (total <= 0)
      {
         return 0;
      }
      else
      {
         double per = 100 * getApproved() / total;
         return (int) Math.ceil(per);
      }
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null)
         return false;
      if (obj instanceof TransUnitWords)
      {
         return super.equals(obj);
      }
      return false;
   }
}
