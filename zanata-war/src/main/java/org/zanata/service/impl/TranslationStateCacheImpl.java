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

import java.util.List;

import org.apache.lucene.util.OpenBitSet;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.service.TranslationStateCache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

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

   private final String TRANSLATED_TEXT_FLOW_CACHE_NAME = "org.zanata.service.impl.TranslatedTextFlowCache";

   @In
   private TextFlowDAO textFlowDAO;

   private CacheManager cacheManager;

   private Cache translatedTextFlowCache;

   @Create
   public void initialize()
   {
      cacheManager = CacheManager.create();
      cacheManager.addCacheIfAbsent(TRANSLATED_TEXT_FLOW_CACHE_NAME);
      translatedTextFlowCache = cacheManager.getCache(TRANSLATED_TEXT_FLOW_CACHE_NAME);
   }

   @Destroy
   public void finalize()
   {
      cacheManager.shutdown();
   }

   @Override
   public OpenBitSet getTranslatedTextFlowIds(LocaleId localeId)
   {
      if( translatedTextFlowCache.get(localeId) == null )
      {
         OpenBitSet bitSet = textFlowDAO.findIdsWithTranslations(localeId);
         translatedTextFlowCache.put( new Element(localeId, bitSet) );
      }

      return (OpenBitSet)translatedTextFlowCache.get( localeId ).getValue();
   }

   @Override
   public void textFlowStateUpdated(Long textFlowId, LocaleId localeId, ContentState newState)
   {
      final Element element = translatedTextFlowCache.get(localeId);
      if( element != null )
      {
         OpenBitSet bitSet = (OpenBitSet)element.getValue();

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

}
