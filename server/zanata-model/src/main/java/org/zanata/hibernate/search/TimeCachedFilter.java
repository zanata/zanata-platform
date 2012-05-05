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

package org.zanata.hibernate.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import lombok.extern.slf4j.Slf4j;

/**
 * Parent class for Filters which cache docid sets for a time
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Slf4j
public abstract class TimeCachedFilter extends Filter
{
   private static final long serialVersionUID = 1L;

   /**
    * Cached DocIdSet
    */
   //TODO use a soft reference to save RAM?
   private DocIdSet docIdSet;
   /**
    * Expiry time for docIdSet as a Java timestamp (ms)
    */
   private long expiryTime = Long.MIN_VALUE; // force fetch on first use
   private final long validityPeriodInMs;
   
   public TimeCachedFilter(long validityPeriodInMs)
   {
      this.validityPeriodInMs = validityPeriodInMs;
   }

   private void updateExpiry()
   {
      expiryTime = System.currentTimeMillis() + validityPeriodInMs;
   }

   private boolean stillFresh()
   {
      return System.currentTimeMillis() < expiryTime;
   }

   protected abstract DocIdSet fetchDocIdSet(IndexReader reader) throws IOException;

   @Override
   public DocIdSet getDocIdSet(IndexReader reader) throws IOException
   {
      if (!stillFresh())
      {
         log.debug("cache entry too old in {}", this);
         docIdSet = fetchDocIdSet(reader);
         updateExpiry();
      }
      else
      {
         log.debug("cache hit in {}", this);
      }
      return docIdSet;
   }
   
}
