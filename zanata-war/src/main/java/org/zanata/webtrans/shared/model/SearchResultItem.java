package org.zanata.webtrans.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SearchResultItem implements IsSerializable
{

   private double relevanceScore;
   private double similarityPercent;

   // for GWT
   protected SearchResultItem()
   {
   }

   protected SearchResultItem(double relevanceScore, double similarityPercent)
   {
      this.relevanceScore = relevanceScore;
      this.similarityPercent = similarityPercent;
   }

   public double getRelevanceScore()
   {
      return relevanceScore;
   }

   public double getSimilarityPercent()
   {
      return similarityPercent;
   }

}
