package org.zanata.search;

import java.util.List;

import org.zanata.common.ContentState;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
public class ActiveStates
{
   private boolean newOn;
   private boolean fuzzyOn;
   private boolean translatedOn;
   private boolean approvedOn;
   private boolean rejectedOn;

   /**
    * @return a Builder with all states on by default
    */
   public static Builder builder()
   {
      return new Builder();
   }

   public boolean hasAllStates()
   {
      return newOn && fuzzyOn && translatedOn && approvedOn && rejectedOn;
   }

   public boolean hasNoStates()
   {
      return !(newOn || fuzzyOn || translatedOn || approvedOn || rejectedOn);
   }

   public List<ContentState> asList()
   {
      List<ContentState> result = Lists.newArrayList();
      if (newOn)
      {
         result.add(ContentState.New);
      }
      if (fuzzyOn)
      {
         result.add(ContentState.NeedReview);
      }
      if (translatedOn)
      {
         result.add(ContentState.Translated);
      }
      if (approvedOn)
      {
         result.add(ContentState.Approved);
      }
      if (rejectedOn)
      {
         result.add(ContentState.Rejected);
      }
      return result;
   }

   public static class Builder
   {
      private boolean newOn;
      private boolean fuzzyOn;
      private boolean translatedOn;
      private boolean approvedOn;
      private boolean rejectedOn;

      public Builder()
      {
         allOn();
      }

      public ActiveStates build()
      {
         return new ActiveStates(newOn, fuzzyOn, translatedOn, approvedOn, rejectedOn);
      }

      public Builder allOn()
      {
         this.newOn = true;
         this.fuzzyOn = true;
         this.translatedOn = true;
         this.approvedOn = true;
         this.rejectedOn = true;
         return this;
      }

      public Builder allOff()
      {
         this.newOn = false;
         this.fuzzyOn = false;
         this.translatedOn = false;
         this.approvedOn = false;
         this.rejectedOn = false;
         return this;
      }

      public Builder fromStates(ActiveStates states)
      {
         this.newOn = states.newOn;
         this.fuzzyOn = states.fuzzyOn;
         this.translatedOn = states.translatedOn;
         this.approvedOn = states.approvedOn;
         this.rejectedOn = states.rejectedOn;
         return this;
      }

      public Builder setNewOn(boolean on)
      {
         newOn = on;
         return this;
      }

      public Builder setFuzzyOn(boolean on)
      {
         fuzzyOn = on;
         return this;
      }

      public Builder setTranslatedOn(boolean on)
      {
         translatedOn = on;
         return this;
      }

      public Builder setApprovedOn(boolean on)
      {
         approvedOn = on;
         return this;
      }

      public Builder setRejectedOn(boolean on)
      {
         rejectedOn = on;
         return this;
      }
   }
}
