package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TranslationMemoryGlossaryItem implements Serializable
{

   private static final long serialVersionUID = -7381018377520206564L;

   private String source;
   private String target;
   private String query;

   private ArrayList<Long> sourceIdList = new ArrayList<Long>();
   private float relevanceScore;
   private int similarityPercent;

   // for GWT
   @SuppressWarnings("unused")
   private TranslationMemoryGlossaryItem()
   {
   }

   public TranslationMemoryGlossaryItem(String source, String memory, String query, float relevanceScore, int similarityPercent)
   {
      this.source = source;
      this.target = memory;
      this.query = query;
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

   public void setTarget(String target)
   {
      this.target = target;
   }

   public String getTarget()
   {
      return target;
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

   public void setSource(String source)
   {
      this.source = source;
   }

   public String getSource()
   {
      return source;
   }

   public String getQuery()
   {
      return query;
   }
}
