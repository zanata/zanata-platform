/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.OpenBitSet;
import org.jboss.seam.Component;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.service.TranslationStateCache;

import com.google.common.base.Stopwatch;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate Search filter for text flows translated for a locale.
 * This filter performs its own caching of DocIdSet (per IndexReader).
 */
@Slf4j
// see also CachingWrapperFilter (Lucene and Hibernate versions)
// TODO consider using BloomFilter; might save memory
public class TranslatedTextFlowFilter extends Filter
{
   private static final long serialVersionUID = 1L;
   private final Map<IndexReader, OpenBitSet> map = new WeakHashMap<IndexReader, OpenBitSet>();
//   Map<IndexReader, OpenBitSet> map = new MapMaker().weakKeys().makeMap();
   private final LocaleId locale;

   public TranslatedTextFlowFilter(LocaleId locale)
   {
      this.locale = locale;
      log.debug("Created TranslatedFilter for locale {}", locale);
   }

   @Override
   public DocIdSet getDocIdSet(IndexReader reader) throws IOException
   {
//      TextFlowDAO textFlowDAO = (TextFlowDAO) Component.getInstance("textFlowDAO");
      TranslationStateCache translationStateCache = getTranslationStateCache();
      OpenBitSet docIdSet;
      synchronized (map)
      {
         docIdSet = map.get(reader);
         if (docIdSet == null)
         {
            log.info("Creating docIdSet for locale {}, IndexReader {}", locale, reader);
            Stopwatch stopwatch = new Stopwatch().start();
            docIdSet = new OpenBitSet(reader.maxDoc());
            map.put(reader, docIdSet);
            log.debug("Loading translatedTextFlowBitSet for locale {}", locale);
//            OpenBitSet translatedTextFlowBitSet = textFlowDAO.findIdsWithTranslations(locale);
            OpenBitSet translatedTextFlowBitSet = translationStateCache.getTranslatedTextFlowIds(locale);
            log.debug("Loaded translatedTextFlowBitSet; populating docIdSet for locale {}", locale);
            setTranslatedBits(docIdSet, reader, translatedTextFlowBitSet);
            log.info("Finished loading docIdSet for locale {} in {}", locale, stopwatch);
         }
         else
         {
            // assume DocIdSet is up to date
            // alternatively we could update DocIdSet at this point, based on queued translation state changes...
            log.debug("Using cached docIdSet for locale {}, IndexReader {}", locale, reader);
         }
      }

      return docIdSet;
   }

   protected TranslationStateCache getTranslationStateCache()
   {
      return (TranslationStateCache) Component.getInstance("translationStateCacheImpl");
   }

   private void setTranslatedBits(OpenBitSet docIdSet, IndexReader reader, OpenBitSet translatedTextFlowBitSet) throws IOException
   {
      for( long id=0; (id = translatedTextFlowBitSet.nextSetBit(id)) >= 0; id++ )
      {
         Term term = new Term("id", Long.toString(id)); // bit is the same as the text flow id
         TermDocs termDocs = reader.termDocs(term);
         while( termDocs.next() ) // Should only be one
         {
            docIdSet.set(termDocs.doc());
         }
      }
   }

   public void onTranslationStateChange(Long textFlowId, ContentState newState)
   {
      // update any cached DocIdSets
      synchronized (map)
      {
         try
         {
            Set<IndexReader> readerSet = map.keySet();
            for (IndexReader reader :  readerSet)
            {
               boolean indexReaderClosed = reader.getRefCount() <= 0;
               if (indexReaderClosed)
               {
                  log.warn("IndexReader for locale {} is closed; removing cached DocIdSet", locale);
                  map.remove(reader);
               }
               OpenBitSet docIdSet = map.get(reader);
               if (docIdSet != null)
               {
                  // TODO should we do this now, or save I/O and queue until getDocIdSet() is called?
                  // if queue is implemented:
                  // if queue > 50 elements, clear queue and cache instead

                  Term term = new Term("id", Long.toString(textFlowId));
                  log.debug("Searching for TextFlow {} in IndexReader {}", textFlowId, reader);
                  TermDocs termDocs = reader.termDocs(term);
                  while( termDocs.next() ) // Should only be one
                  {
                     if (newState == ContentState.Approved)
                     {
                        log.debug("Marking TextFlow {} as translated in locale {}", textFlowId, locale);
                        docIdSet.set(termDocs.doc());
                     }
                     else
                     {
                        log.debug("Marking TextFlow {} as untranslated in locale {}", textFlowId, locale);
                        docIdSet.clear(termDocs.doc());
                     }
                  }
               }
            }
         }
         catch (AlreadyClosedException e)
         {
            log.warn("Unable to use IndexReader for locale {}; discarding all DocIdSets", locale, e);
            map.clear();
         }
         catch (IOException e)
         {
            log.warn("Unable to use IndexReader for locale {}; discarding all DocIdSets", locale, e);
            map.clear();
         }
      }
   }
}
