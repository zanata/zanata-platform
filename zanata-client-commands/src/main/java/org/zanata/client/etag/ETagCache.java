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
package org.zanata.client.etag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 * Provides a set of Etag cache entries and convenience methods for finding them.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ETagCache
{
   private Map<MultiKey, ETagCacheEntry> fileNameIndex = new LinkedHashMap<MultiKey, ETagCacheEntry>();


   public ETagCache()
   {
   }

   ETagCache( ETagCacheCollection entries )
   {
      for( ETagCacheEntry entry : entries.getCacheEntries() )
      {
         addEntry( entry );
      }
   }

   public List<ETagCacheEntry> getCacheEntries()
   {
      return new ArrayList<ETagCacheEntry>( fileNameIndex.values() );
   }

   public void addEntry( ETagCacheEntry entry )
   {
      fileNameIndex.put(new MultiKey(entry.getLocalFileName(), entry.getLanguage()), entry);
   }

   public ETagCacheEntry findEntry( String localFileName, String language )
   {
      return fileNameIndex.get( new MultiKey(localFileName, language) );
   }

   public void clear()
   {
      fileNameIndex.clear();
   }

   ETagCacheCollection asETagCacheCollection()
   {
      ETagCacheCollection col = new ETagCacheCollection();
      col.setCacheEntries( new ArrayList<ETagCacheEntry>(fileNameIndex.values()  ) );
      return col;
   }
}
