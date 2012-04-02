package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SearchResultItem implements Serializable, IsSerializable
{

   private static final long serialVersionUID = -7381018377520206564L;

   private ArrayList<Long> sourceIdList = new ArrayList<Long>();
   private float relevanceScore;
   private int similarityPercent;

   // for GWT
   protected SearchResultItem()
   {
   }

   protected SearchResultItem(float relevanceScore, int similarityPercent)
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

   public void setRelevanceScore(float relevanceScore)
   {
      this.relevanceScore = relevanceScore;
   }

   public float getRelevanceScore()
   {
      return relevanceScore;
   }

   public int getSimilarityPercent()
   {
      return similarityPercent;
   }

   public void setSimilarityPercent(int similarityPercent)
   {
      this.similarityPercent = similarityPercent;
   }

}
