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

import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.EhcacheWrapper;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.service.TranslationStateCache;

import com.google.common.cache.CacheLoader;

import net.sf.ehcache.CacheManager;

/**
 * Default Implementation of the Translation State Cache.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationStateCacheImpl")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class TranslationStateCacheImpl implements TranslationStateCache
{
   private static final String BASE = TranslationStateCacheImpl.class.getName();
   private static final String CACHE_NAME = BASE+".filterCache";
   private static final String TRANSLATED_TEXT_FLOW_CACHE_NAME = BASE+".translatedTextFlowCache";

   @In
   private TextFlowDAO textFlowDAO;
   private CacheManager cacheManager;
   private CacheWrapper<LocaleId, TranslatedTextFlowFilter> filterCache;
   private CacheWrapper<LocaleId, OpenBitSet> translatedTextFlowCache;
   private CacheLoader<LocaleId, TranslatedTextFlowFilter> filterLoader;
   private CacheLoader<LocaleId, OpenBitSet> bitsetLoader;

   public TranslationStateCacheImpl()
   {
      // constructor for Seam
      this.filterLoader = new FilterLoader();
      this.bitsetLoader = new BitsetLoader();
   }

   public TranslationStateCacheImpl(
         CacheLoader<LocaleId, TranslatedTextFlowFilter> filterLoader,
         CacheLoader<LocaleId, OpenBitSet> bitsetLoader)
   {
      // constructor for testing
      this.filterLoader = filterLoader;
      this.bitsetLoader = bitsetLoader;
   }
   
   @Create
   public void create()
   {
      cacheManager = CacheManager.create();
      filterCache = EhcacheWrapper.create(CACHE_NAME, cacheManager, filterLoader);
      translatedTextFlowCache = EhcacheWrapper.create(TRANSLATED_TEXT_FLOW_CACHE_NAME, cacheManager, bitsetLoader);
   }

   @Destroy
   public void destroy()
   {
      cacheManager.shutdown();
   }

   @Override
   public OpenBitSet getTranslatedTextFlowIds(final LocaleId localeId)
   {
      return translatedTextFlowCache.getWithLoader(localeId);
   }

   @Override
   public void textFlowStateUpdated(Long textFlowId, LocaleId localeId, ContentState newState)
   {
      updateTranslatedTextFlowCache(textFlowId, localeId, newState);
      updateFilterCache(textFlowId, localeId, newState);
   }

   @Override
   public Filter getFilter(LocaleId locale)
   {
      return filterCache.getWithLoader(locale);
   }

   private void updateFilterCache(Long textFlowId, LocaleId localeId, ContentState newState)
   {
      TranslatedTextFlowFilter filter = filterCache.get(localeId);
      if (filter != null)
      {
         filter.onTranslationStateChange(textFlowId, newState);
      }
   }

   private void updateTranslatedTextFlowCache(Long textFlowId, LocaleId localeId, ContentState newState)
   {
      OpenBitSet bitSet = translatedTextFlowCache.get(localeId);
      if( bitSet != null )
      {
         boolean translated = newState == ContentState.Approved;
         if( translated )
         {
            bitSet.set( textFlowId );
         }
         else
         {
            bitSet.clear( textFlowId );
         }
      }
   }

   private final class BitsetLoader extends CacheLoader<LocaleId, OpenBitSet>
   {
      @Override
      public OpenBitSet load(LocaleId localeId) throws Exception
      {
         return textFlowDAO.findIdsWithTranslations(localeId);
      }
   }

   private final class FilterLoader extends CacheLoader<LocaleId, TranslatedTextFlowFilter>
   {
      @Override
      public TranslatedTextFlowFilter load(LocaleId localeId) throws Exception
      {
         return new TranslatedTextFlowFilter(localeId);
      }
   }

}
