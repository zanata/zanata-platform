package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SearchResultItem implements Serializable, IsSerializable
{

   private static final long serialVersionUID = -7381018377520206564L;

   private ArrayList<Long> sourceIdList = new ArrayList<Long>();
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

   public ArrayList<Long> getSourceIdList()
   {
      return sourceIdList;
   }

   public void addSourceId(Long sourceId)
   {
      this.sourceIdList.add(sourceId);
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
