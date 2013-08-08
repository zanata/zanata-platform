/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.webtrans.shared.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A result item returned by a translation memory search.
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TransMemoryResultItem extends SearchResultItem implements IsSerializable
{

   /**
    * Describes the type of match that is found.
    */
   public enum MatchType
   {
      // Note: The order is significant for ordering of TM results in the UI

      /* Imported from an external source (i.e. TMX) */
      Imported,
      TranslatedInternal,
      ApprovedInternal,
   }

   private ArrayList<String> sourceContents;
   private ArrayList<String> targetContents;
   private int matchCount = 0;                  // The number of occurrences for the source contents
   private MatchType matchType;
   private List<String> origins;                 // The optional origin identifiers for this result (i.e. A Trans memory name)
   private ArrayList<Long> sourceIdList = new ArrayList<Long>();


   // for GWT
   @SuppressWarnings("unused")
   private TransMemoryResultItem()
   {
   }

   /**
    * @param sourceContents
    * @param targetContents
    * @param relevanceScore
    * @param similarityPercent
    */
   public TransMemoryResultItem(ArrayList<String> sourceContents, ArrayList<String> targetContents, MatchType matchType,
                                double relevanceScore, double similarityPercent)
   {
      super(relevanceScore, similarityPercent);
      this.sourceContents = sourceContents;
      this.targetContents = targetContents;
      this.matchType = matchType;
      this.origins = new ArrayList<String>();
   }

   public List<String> getOrigins()
   {
      return origins;
   }

   public void addOrigin(String origin)
   {
      this.origins.add(origin);
   }

   // FIXME remove this
   @Deprecated
   public String getSource()
   {
      if (sourceContents.size() == 0)
      {
         return null;
      }
      return sourceContents.get(0);
   }

   public ArrayList<String> getSourceContents()
   {
      return sourceContents;
   }

   // FIXME remove this
   @Deprecated
   public String getTarget()
   {
      if (targetContents.size() == 0)
      {
         return null;
      }
      return targetContents.get(0);
   }

   public List<String> getTargetContents()
   {
      return targetContents;
   }

   public MatchType getMatchType()
   {
      return matchType;
   }

   public int getMatchCount()
   {
      return matchCount;
   }

   public void incMatchCount()
   {
      ++this.matchCount;
   }

   public ArrayList<Long> getSourceIdList()
   {
      return sourceIdList;
   }

   public void addSourceId(Long sourceId)
   {
      this.sourceIdList.add(sourceId);
   }

}
